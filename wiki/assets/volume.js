/* Perspective SVG scenes follow Ashspace's camera and shaded-face interaction.
   Model cells stay fixed while camera events repaint only the scene geometry. */
(() => {
  'use strict';
  const V = window.WikiVisuals;
  let sequence = 0;
  const radians = angle => angle * Math.PI / 180;
  const clamp = (n, min, max) => Math.min(max, Math.max(min, n));
  const dot = (a, b) => a.reduce((sum, n, axis) => sum + n * b[axis], 0);
  const subtract = (a, b) => a.map((n, axis) => n - b[axis]);
  const initialCamera = Object.freeze({ yaw: -35, pitch: 28, zoom: 1 });
  const corners = (min, size) => Array.from({ length: 8 }, (_, mask) =>
    min.map((value, axis) => value + ((mask >> axis) & 1) * size[axis]));
  const edges = Array.from({ length: 8 }, (_, mask) => [0, 1, 2]
    .filter(axis => !(mask & (1 << axis))).map(axis => [mask, mask | (1 << axis)])).flat();
  const faces = [
    { normal: [-1, 0, 0], vertices: [0, 4, 6, 2] },
    { normal: [1, 0, 0], vertices: [1, 3, 7, 5] },
    { normal: [0, -1, 0], vertices: [0, 1, 5, 4] },
    { normal: [0, 1, 0], vertices: [2, 6, 7, 3] },
    { normal: [0, 0, -1], vertices: [0, 2, 3, 1] },
    { normal: [0, 0, 1], vertices: [4, 5, 7, 6] }
  ];

  function scene({ cells, min = [0, 0, 0], size, title, camera }, width, height) {
    const yaw = radians(camera.yaw), pitch = radians(camera.pitch);
    const cy = Math.cos(yaw), sy = Math.sin(yaw), cp = Math.cos(pitch), sp = Math.sin(pitch);
    const direction = [sy * cp, sp, cy * cp], right = [cy, 0, -sy], up = [-sy * sp, cp, -cy * sp];
    const center = min.map((value, axis) => value + size[axis] / 2);
    const distance = Math.hypot(...size) * 1.7 / camera.zoom;
    const eye = center.map((value, axis) => value + direction[axis] * distance);
    const focal = Math.min(width * .94, height * 1.2);
    const project = point => {
      const relative = subtract(point, center), depth = distance - dot(relative, direction);
      return [width / 2 + dot(relative, right) * focal / depth, height * .49 - dot(relative, up) * focal / depth, depth];
    };
    const coordinates = point => project(point).slice(0, 2).map(n => n.toFixed(2)).join(',');
    const segment = (a, b, css) => {
      const start = project(a), end = project(b);
      return `<line x1="${start[0]}" y1="${start[1]}" x2="${end[0]}" y2="${end[1]}" class="${css}"/>`;
    };
    let svg = `<title>${V.escape(title)}</title><desc>Perspective 3D view. Y points up. Each cube spans one cell; empty cells are omitted. Camera azimuth ${Math.round(camera.yaw)} degrees, elevation ${Math.round(camera.pitch)} degrees, zoom ${Math.round(camera.zoom * 100)} percent. Drag or use arrow keys to orbit; scroll or use plus and minus to zoom; Home restores the camera.</desc>`;
    const groundY = min[1] - .08;
    for (let x = min[0] - 1; x <= min[0] + size[0] + 1; x++) svg += segment([x, groundY, min[2] - 1], [x, groundY, min[2] + size[2] + 1], 'volume-ground');
    for (let z = min[2] - 1; z <= min[2] + size[2] + 1; z++) svg += segment([min[0] - 1, groundY, z], [min[0] + size[0] + 1, groundY, z], 'volume-ground');
    const bounds = corners(min, size);
    svg += edges.map(([a, b]) => segment(bounds[a], bounds[b], 'volume-bound')).join('');
    const sorted = [...cells].sort((a, b) => project([b.x + .5, b.y + .5, b.z + .5])[2] - project([a.x + .5, a.y + .5, a.z + .5])[2]);
    for (const cell of sorted) {
      const vertices = corners([cell.x, cell.y, cell.z], [1, 1, 1]);
      const kind = ['selected', 'a', 'b', 'blocked'].includes(cell.kind) ? cell.kind : 'selected';
      svg += `<g class="volume-cell volume-cell-${kind}" data-volume-cell="${cell.x},${cell.y},${cell.z}"><title>${V.escape(`(${cell.x}, ${cell.y}, ${cell.z}): ${cell.title ?? cell.label ?? 'selected'}`)}</title>`;
      const visible = faces.filter(face => dot(face.normal, subtract(eye, vertices[face.vertices[0]])) > 0);
      for (const face of visible) {
        const points = face.vertices.map(i => coordinates(vertices[i])).join(' ');
        const shade = .12 + .35 * (1 - Math.max(0, dot(face.normal, [-.249, .830, .498])));
        svg += `<polygon class="volume-face" points="${points}"/><polygon points="${points}" fill="#000000" fill-opacity="${shade}" stroke="none"/>`;
      }
      if (cells.length <= 32 && cell.label && eye[1] > cell.y + 1) {
        const p = project([cell.x + .5, cell.y + 1.01, cell.z + .5]);
        svg += `<text x="${p[0]}" y="${p[1] + 4}" class="volume-value">${V.escape(cell.label)}</text>`;
      }
      svg += '</g>';
    }
    for (const [axis, vector] of [[0, [1, 0, 0]], [1, [0, 1, 0]], [2, [0, 0, 1]]]) {
      const x = 36 + dot(vector, right) * 23, y = 45 - dot(vector, up) * 23;
      svg += `<g class="volume-orientation volume-axis-${'xyz'[axis]}"><line x1="36" y1="45" x2="${x}" y2="${y}"/><text x="${x + (x < 36 ? -9 : 5)}" y="${y + 4}">${'XYZ'[axis]}</text></g>`;
    }
    return svg + `<text x="18" y="${height - 16}" class="volume-footer">${size.join(' × ')} cells · min (${min.join(', ')}) · Y up</text>`;
  }

  function render(model) {
    const id = String(++sequence), camera = model.camera;
    camera.models.set(id, model);
    // A model redraw replaces all scene nodes synchronously. Keep only live models.
    queueMicrotask(camera.refresh);
    return `<svg class="visual-volume" viewBox="0 0 720 410" tabindex="0" role="img" aria-label="${V.escape(model.title)}. Drag to orbit; arrows rotate; plus and minus zoom; Home resets the view." aria-describedby="${camera.helpId}" data-volume-scene="${id}" data-volume-count="${model.cells.length}">${scene(model, 720, 410)}</svg>`;
  }

  function controls(host, draw, cleanups) {
    const id = `volume-camera-${++sequence}`, body = host.querySelector('[data-visual-body]');
    const state = { mode: 'volume', ...initialCamera, models: new Map(), helpId: `${id}-help` };
    const toolbar = document.createElement('div');
    toolbar.className = 'volume-view-toolbar';
    toolbar.innerHTML = `<div class="volume-segmented" role="group" aria-label="Visualization view"><button type="button" data-volume-mode="volume" aria-pressed="true">3D volume</button><button type="button" data-volume-mode="slices" aria-pressed="false">2D slices</button></div><div class="visual-view-fields"></div>`;
    body.before(toolbar);
    const cameraBar = document.createElement('div');
    cameraBar.className = 'volume-camera-toolbar';
    cameraBar.innerHTML = `<p id="${state.helpId}">Drag to orbit · scroll to zoom<br>Keyboard: arrows, + / −, Home</p><div role="group" aria-label="Camera controls"><button type="button" data-camera="out" aria-label="Zoom out">−</button><button type="button" data-camera="in" aria-label="Zoom in">+</button><button type="button" data-camera="reset">Reset view</button></div>`;
    body.after(cameraBar);
    let scheduled = 0, disposed = false, drag = null;
    const on = (target, event, handler, options) => {
      target.addEventListener(event, handler, options);
      cleanups.push(() => target.removeEventListener(event, handler, options));
    };
    state.prune = () => {
      const live = new Set([...body.querySelectorAll('[data-volume-scene]')].map(svg => svg.dataset.volumeScene));
      for (const key of state.models.keys()) if (disposed || !live.has(key)) state.models.delete(key);
    };
    function repaint() {
      scheduled = 0;
      if (disposed) return;
      for (const svg of body.querySelectorAll('[data-volume-scene]')) {
        const model = state.models.get(svg.dataset.volumeScene), rect = svg.getBoundingClientRect();
        if (!model || rect.width <= 0 || rect.height <= 0) continue;
        svg.setAttribute('viewBox', `0 0 ${rect.width} ${rect.height}`);
        svg.innerHTML = scene(model, rect.width, rect.height);
      }
      state.prune();
    }
    function schedule() {
      if (!scheduled && !disposed) scheduled = requestAnimationFrame(repaint);
    }
    state.refresh = () => { state.prune(); schedule(); };
    function cameraAction(action) {
      if (action === 'reset') Object.assign(state, initialCamera);
      else state.zoom = clamp(state.zoom * (action === 'in' ? 1.15 : 1 / 1.15), .65, 1.55);
      schedule();
    }
    on(toolbar, 'click', event => {
      const button = event.target.closest('[data-volume-mode]');
      if (!button) return;
      state.mode = button.dataset.volumeMode;
      toolbar.querySelectorAll('[data-volume-mode]').forEach(item => item.setAttribute('aria-pressed', String(item === button)));
      cameraBar.hidden = state.mode !== 'volume';
      draw(); schedule();
    });
    on(cameraBar, 'click', event => {
      const action = event.target.closest('[data-camera]')?.dataset.camera;
      if (action) cameraAction(action);
    });
    on(body, 'pointerdown', event => {
      const svg = event.target.closest('.visual-volume');
      if (!svg || event.button !== 0 || !event.isPrimary) return;
      svg.focus({ preventScroll: true });
      drag = { id: event.pointerId, x: event.clientX, y: event.clientY, svg };
      svg.setPointerCapture(event.pointerId); svg.classList.add('is-dragging');
    });
    on(body, 'pointermove', event => {
      if (!drag || drag.id !== event.pointerId) return;
      state.yaw = (state.yaw - (event.clientX - drag.x) * .45) % 360;
      state.pitch = clamp(state.pitch + (event.clientY - drag.y) * .35, 12, 78);
      drag.x = event.clientX; drag.y = event.clientY; schedule();
    });
    function endDrag(event) {
      if (!drag || drag.id !== event.pointerId) return;
      const { svg, id: pointerId } = drag; drag = null;
      svg.classList.remove('is-dragging');
      if (svg.hasPointerCapture(pointerId)) svg.releasePointerCapture(pointerId);
    }
    on(body, 'pointerup', endDrag); on(body, 'pointercancel', endDrag); on(body, 'lostpointercapture', endDrag);
    on(body, 'wheel', event => {
      if (!event.target.closest('.visual-volume') || event.ctrlKey) return;
      event.preventDefault();
      const delta = event.deltaY * (event.deltaMode === 1 ? 16 : event.deltaMode === 2 ? 410 : 1);
      state.zoom = clamp(state.zoom * Math.exp(-clamp(delta, -150, 150) * .002), .65, 1.55); schedule();
    }, { passive: false });
    on(body, 'keydown', event => {
      if (!event.target.matches('.visual-volume') || event.ctrlKey || event.metaKey || event.altKey || !['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', '+', '=', '-', 'Home'].includes(event.key)) return;
      event.preventDefault();
      if (event.key === 'ArrowLeft') state.yaw -= 8;
      if (event.key === 'ArrowRight') state.yaw += 8;
      if (event.key === 'ArrowUp') state.pitch = clamp(state.pitch + 6, 12, 78);
      if (event.key === 'ArrowDown') state.pitch = clamp(state.pitch - 6, 12, 78);
      if (event.key === '+' || event.key === '=') cameraAction('in');
      if (event.key === '-') cameraAction('out');
      if (event.key === 'Home') cameraAction('reset');
      schedule();
    });
    const resize = new ResizeObserver(schedule); resize.observe(body);
    cleanups.push(() => {
      disposed = true;
      if (scheduled) cancelAnimationFrame(scheduled);
      if (drag && drag.svg.hasPointerCapture(drag.id)) drag.svg.releasePointerCapture(drag.id);
      resize.disconnect(); state.models.clear();
    });
    return state;
  }
  window.WikiVolume = Object.freeze({ render, controls });
})();
