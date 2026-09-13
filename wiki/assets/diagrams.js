/* Ashgrid illustrations: scalar VoxelSpace conversion and positive-X traversal.
   Bounded JavaScript figures explain the Java contracts; they do not call the library. */
(function () {
  'use strict';
  const NS = 'http://www.w3.org/2000/svg';
  const mounted = new WeakMap();
  let sequence = 0;
  const format = value => Number(value.toFixed(3)).toString();

  function append(parent, name, attributes = {}, text) {
    const node = document.createElementNS(NS, name);
    Object.entries(attributes).forEach(([key, value]) => node.setAttribute(key, value));
    if (text !== undefined) node.textContent = text;
    parent.appendChild(node);
    return node;
  }
  function line(parent, x1, y1, x2, y2, className) {
    return append(parent, 'line', { x1, y1, x2, y2, class: className });
  }
  function base(svg, id, title, description) {
    svg.replaceChildren();
    append(svg, 'title', { id: `${id}-title` }, title);
    append(svg, 'desc', { id: `${id}-description` }, description);
    svg.setAttribute('aria-labelledby', `${id}-title ${id}-description`);
  }
  function bind(node, event, listener, cleanups) {
    node.addEventListener(event, listener);
    cleanups.push(() => node.removeEventListener(event, listener));
  }
  function control(id, key, label, min, max, step, value, unit) {
    return `<div class="diagram-control">
      <div class="diagram-control-label mb-[9px] flex items-baseline justify-between gap-2">
        <label class="text-xs leading-normal text-foreground" for="${id}-${key}">${label}</label>
        <output class="whitespace-nowrap font-mono text-[11px] text-muted tabular-nums" for="${id}-${key}" data-value="${key}"></output>
      </div>
      <input id="${id}-${key}" class="m-0 block h-[18px] w-full cursor-pointer p-0 accent-accent" type="range" min="${min}" max="${max}" step="${step}" value="${value}" data-control="${key}" data-unit="${unit}">
      <div class="diagram-range-labels mt-[5px] flex justify-between text-[10px] leading-[1.2] text-muted" aria-hidden="true"><span>${min} ${unit}</span><span>${max} ${unit}</span></div>
    </div>`;
  }
  function shell(host, title, controls, caption, extra = '') {
    host.innerHTML = `<figure class="diagram-component mx-0 my-[26px] overflow-hidden rounded-xl border border-line bg-surface font-sans text-[13px] leading-normal text-foreground print:break-inside-avoid">
      <div class="diagram-toolbar flex items-center justify-between gap-3 border-b border-line px-[18px] py-3.5 max-[620px]:flex-wrap max-[620px]:p-3">
        <strong class="text-xs font-medium">${title}</strong>
        <span class="diagram-plane-label whitespace-nowrap text-[11px] text-muted">Interactive illustration · X axis</span>
      </div>
      <svg class="diagram-scene block h-auto w-full overflow-visible bg-[var(--diagram-plot)]" viewBox="0 0 840 240" role="img"></svg>
      <div class="diagram-readout grid gap-2 border-y border-line px-5 py-[15px] max-[620px]:p-3.5" aria-live="polite" aria-atomic="true">
        <output class="block font-mono text-xs leading-[1.7] tabular-nums" data-result></output>
        <span class="text-[11px] leading-[1.7] text-muted" data-detail></span>
      </div>
      <div class="diagram-controls grid grid-cols-3 items-center gap-[26px] px-5 pt-[19px] pb-[15px] max-[620px]:grid-cols-1 max-[620px]:gap-[18px] max-[620px]:px-3.5 print:hidden">${controls}</div>
      <div class="px-5 pb-3 max-[620px]:px-3.5 print:hidden"><button type="button" class="diagram-reset cursor-pointer appearance-none rounded-md border border-line bg-transparent px-3 py-[7px] text-[11px] leading-[1.4] text-muted hover:bg-[var(--diagram-muted-surface)] hover:text-foreground" data-reset>Reset</button></div>
      ${extra}
      <figcaption class="diagram-caption px-5 pt-px pb-[18px] text-[11px] leading-[1.7] text-muted max-[620px]:px-3.5 max-[620px]:pb-4">${caption}</figcaption>
    </figure>`;
  }
  function controls(host, state, defaults, draw, cleanups) {
    host.querySelectorAll('[data-control]').forEach(input => {
      bind(input, 'input', () => {
        state[input.dataset.control] = Number(input.value);
        draw();
      }, cleanups);
    });
    bind(host.querySelector('[data-reset]'), 'click', () => {
      Object.assign(state, defaults);
      host.querySelectorAll('[data-control]').forEach(input => { input.value = state[input.dataset.control]; });
      draw();
    }, cleanups);
  }
  function controlReadouts(host, state) {
    host.querySelectorAll('[data-control]').forEach(input => {
      const text = `${format(state[input.dataset.control])} ${input.dataset.unit}`;
      host.querySelector(`[data-value="${input.dataset.control}"]`).textContent = text;
      input.setAttribute('aria-valuetext', text);
    });
  }

  function coordinateFigure(host) {
    const id = `wiki-coordinates-${++sequence}`;
    const cleanups = [];
    const defaults = { position: 13.9, origin: 10, scale: 2 };
    const state = { ...defaults };
    shell(host, 'Map a world position to one cell',
      control(id, 'position', 'Position X', -4, 16, 0.1, state.position, 'units') +
      control(id, 'origin', 'Origin X', -4, 12, 1, state.origin, 'units') +
      control(id, 'scale', 'Cell edge length', 0.5, 4, 0.5, state.scale, 'units'),
      'Scalar illustration of floor((position − origin) / scale), using the same scale and origin meaning as VoxelSpace. Cell indices have no unit; positions and cell bounds use your world units. The selected cell stays centered as you move. Y and Z follow the same independent conversion; no rotation is applied.');
    const svg = host.querySelector('svg');
    function draw() {
      const cell = Math.floor((state.position - state.origin) / state.scale);
      const low = state.origin + cell * state.scale;
      const high = low + state.scale;
      const first = cell - 2;
      const left = state.origin + first * state.scale;
      const project = x => 70 + (x - left) / state.scale * 140;
      base(svg, id, 'World X position and its floor-selected cell',
        `Position X ${format(state.position)} world units, origin X ${format(state.origin)}, cell edge length ${format(state.scale)}. ` +
        `Cell X is ${cell}. Its world interval is [${format(low)}, ${format(high)}), including the lower bound and excluding the upper bound.`);
      for (let offset = 0; offset < 5; offset++) {
        const index = first + offset;
        append(svg, 'rect', { x: 70 + offset * 140, y: 102, width: 140, height: 72,
          class: `diagram-obstacle${index === cell ? ' diagram-obstacle-hit' : ''}` });
        append(svg, 'text', { x: 140 + offset * 140, y: 136, class: 'diagram-obstacle-label', 'text-anchor': 'middle' }, `cell ${index}`);
        if (index === cell) append(svg, 'text', { x: 140 + offset * 140, y: 157, class: 'diagram-obstacle-caption', 'text-anchor': 'middle' }, 'selected');
      }
      for (let offset = 0; offset <= 5; offset++) {
        const x = 70 + offset * 140;
        line(svg, x, 174, x, 182, 'diagram-axis diagram-stroke-x');
        append(svg, 'text', { x, y: 202, class: 'diagram-tick-label', 'text-anchor': 'middle' }, format(left + offset * state.scale));
      }
      const point = project(state.position);
      line(svg, point, 70, point, 101, 'diagram-projections');
      append(svg, 'circle', { cx: point, cy: 70, r: 6, class: 'diagram-point' });
      append(svg, 'text', { x: point, y: 47, class: 'diagram-point-label', 'text-anchor': 'middle' }, `Position X = ${format(state.position)}`);
      append(svg, 'text', { x: 770, y: 229, class: 'diagram-axis-label diagram-fill-x', 'text-anchor': 'end' }, 'X · world units');
      host.querySelector('[data-result]').textContent = `cellX = floor((${format(state.position)} − ${format(state.origin)}) / ${format(state.scale)}) = ${cell}`;
      host.querySelector('[data-detail]').textContent = `Cell ${cell} covers [${format(low)}, ${format(high)}) world units. Lower bound included; upper bound excluded. Center X = ${format(low + state.scale / 2)}.`;
      controlReadouts(host, state);
    }
    controls(host, state, defaults, draw, cleanups);
    draw();
    return () => cleanups.forEach(cleanup => cleanup());
  }

  // Positive X only. The finite interval [0, length) excludes a cell entered at length.
  function positiveXVisits(start, length) {
    const visits = [];
    if (length === 0) return visits;
    let cell = Math.floor(start);
    let enter = 0;
    while (enter < length) {
      const exit = Math.min(length, cell + 1 - start);
      visits.push({ cell, enter, exit });
      if (exit === length) break;
      enter = exit;
      cell++;
    }
    return visits;
  }
  function raycastFigure(host) {
    const id = `wiki-raycast-${++sequence}`;
    const cleanups = [];
    const defaults = { start: 0.5, length: 2.5 };
    const state = { ...defaults };
    shell(host, 'Trace cell intervals along positive X',
      control(id, 'start', 'Ray origin X', -2, 2, 0.25, state.start, 'cells') +
      control(id, 'length', 'Distance limit tMax', 0, 6, 0.25, state.length, 'cells') +
      '<p class="m-0 text-xs leading-[1.7] text-muted">Direction: <code>(1, 0, 0)</code><br>Fixed Y = 0.5, Z = 0.5</p>',
      'Axis-aligned illustration of positive-X traversal in unit-grid coordinates. The filled start is included when tMax > 0; the open endpoint is excluded. At tMax = 0 there are no callbacks. The list shows cell entry and exit distances from the ray origin. This figure does not simulate full 3D DDA, negative-direction boundary contacts, clipping, or block collision shapes.',
      '<div class="mx-5 mb-5 overflow-x-auto rounded-[6px] border border-line max-[620px]:mx-3.5"><table class="w-full border-collapse text-left text-[12px]"><caption class="px-3 py-2 text-left text-[11px] text-muted">Callback order · entry and exit distances in cells</caption><thead><tr><th scope="col" class="border-b border-line px-3 py-2">Cell (X, Y, Z)</th><th scope="col" class="border-b border-line px-3 py-2">tEnter</th><th scope="col" class="border-b border-line px-3 py-2">tExit</th></tr></thead><tbody data-visits></tbody></table></div>');
    const svg = host.querySelector('svg');
    function draw() {
      const endpoint = state.start + state.length;
      const visits = positiveXVisits(state.start, state.length);
      const first = Math.floor(state.start) - 1;
      const last = Math.floor(endpoint) + 1;
      const width = 700 / (last - first + 1);
      const project = x => 70 + (x - first) * width;
      base(svg, id, 'Positive-X ray and visited unit cells',
        `Ray origin (${format(state.start)}, 0.5, 0.5), direction (1, 0, 0), distance limit ${format(state.length)} cells. ` +
        `Endpoint X ${format(endpoint)} is excluded. ${visits.length} callbacks: ` +
        visits.map(visit => `cell (${visit.cell},0,0), interval [${format(visit.enter)},${format(visit.exit)})`).join('; ') + '.');
      for (let cell = first; cell <= last; cell++) {
        const visited = visits.some(visit => visit.cell === cell);
        append(svg, 'rect', { x: project(cell), y: 102, width, height: 62,
          class: `diagram-obstacle${visited ? ' diagram-obstacle-hit' : ''}` });
        append(svg, 'text', { x: project(cell + 0.5), y: 138, class: 'diagram-obstacle-label', 'text-anchor': 'middle' }, cell);
      }
      const startX = project(state.start);
      const endX = project(endpoint);
      if (state.length > 0) {
        line(svg, startX, 82, endX, 82, 'diagram-ray');
        line(svg, startX, 82, startX, 101, 'diagram-projections');
        append(svg, 'circle', { cx: startX, cy: 82, r: 5, class: 'diagram-point' });
        append(svg, 'text', { x: startX, y: 56, class: 'diagram-point-label', 'text-anchor': 'middle' }, 'Start included');
      } else {
        append(svg, 'text', { x: 420, y: 56, class: 'diagram-point-label', 'text-anchor': 'middle' }, 'Empty interval · no callbacks');
      }
      append(svg, 'circle', { cx: endX, cy: 82, r: 5, class: 'diagram-ray-endpoint' });
      line(svg, endX, 89, endX, 178, 'diagram-projections');
      append(svg, 'text', { x: endX, y: 199, class: 'diagram-endpoint-label', 'text-anchor': 'middle' }, `End X = ${format(endpoint)} · excluded`);
      append(svg, 'text', { x: 770, y: 229, class: 'diagram-axis-label diagram-fill-x', 'text-anchor': 'end' }, 'X · unit-grid cells');
      host.querySelector('[data-result]').textContent = `originX = ${format(state.start)}, tMax = ${format(state.length)}, endpointX = ${format(endpoint)}, callbacks = ${visits.length}`;
      host.querySelector('[data-detail]').textContent = visits.length
        ? `Visited X indices: ${visits.map(visit => visit.cell).join(' → ')}. Every callback has Y = 0 and Z = 0.`
        : 'The distance interval is empty. The starting cell receives no callback.';
      host.querySelector('[data-visits]').innerHTML = visits.length ? visits.map(visit =>
        `<tr><td class="px-3 py-2 font-mono">(${visit.cell}, 0, 0)</td><td class="px-3 py-2 font-mono">${format(visit.enter)}</td><td class="px-3 py-2 font-mono">${format(visit.exit)}</td></tr>`).join('')
        : '<tr><td colspan="3" class="px-3 py-2 text-muted">No callbacks.</td></tr>';
      controlReadouts(host, state);
    }
    controls(host, state, defaults, draw, cleanups);
    draw();
    return () => cleanups.forEach(cleanup => cleanup());
  }

  function mount(root) {
    const container = root || document;
    const hosts = Array.from(container.querySelectorAll('[data-diagram]'));
    if (container.matches && container.matches('[data-diagram]')) hosts.unshift(container);
    const cleanups = hosts.map(host => {
      const previous = mounted.get(host);
      if (previous) previous();
      const create = { coordinates: coordinateFigure, raycast: raycastFigure, ...window.WikiDiagramFactories }[host.dataset.diagram];
      if (!create) return () => {};
      let disposeFigure;
      let resetButton;
      function render() {
        if (disposeFigure) disposeFigure();
        disposeFigure = create(host);
        // A fresh factory restores inputs, edited cells, work, and camera together.
        // Existing per-figure reset handlers are disposed with their factory.
        host.querySelectorAll('button[data-reset], button[data-action="reset"]').forEach(button => button.remove());
        resetButton = document.createElement('button');
        resetButton.type = 'button';
        resetButton.className = 'visual-button';
        resetButton.dataset.resetExample = '';
        resetButton.textContent = 'Reset example';
        resetButton.title = 'Restore the documented inputs, result, and view';
        const controls = host.querySelector('.visual-controls, .diagram-controls');
        controls.appendChild(resetButton);
        resetButton.addEventListener('click', reset);
      }
      function reset(event) {
        // Raster controls use event delegation; this is a documentation action.
        event.stopPropagation();
        resetButton.removeEventListener('click', reset);
        render();
        resetButton.focus({ preventScroll: true });
      }
      const cleanup = () => {
        resetButton.removeEventListener('click', reset);
        disposeFigure();
      };
      render();
      mounted.set(host, cleanup);
      return () => {
        if (mounted.get(host) === cleanup) {
          cleanup();
          mounted.delete(host);
        }
      };
    });
    return () => cleanups.forEach(cleanup => cleanup());
  }
  window.WikiDiagrams = Object.freeze({ mount });
})();
