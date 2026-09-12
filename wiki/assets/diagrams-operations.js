/* Bounded, synchronous explanations of Ashgrid's raster algorithms.
   These JavaScript models illustrate the contracts; they do not run the Java library. */
(function () {
  'use strict';
  const factories = window.WikiDiagramFactories = window.WikiDiagramFactories || {};
  let sequence = 0;
  const offsets = neighborhood => {
    const result = [];
    for (let z = -1; z <= 1; z++) for (let y = -1; y <= 1; y++) for (let x = -1; x <= 1; x++) {
      const axes = Math.abs(x) + Math.abs(y) + Math.abs(z);
      if (axes && (neighborhood === 'N26' || axes <= (neighborhood === 'N18' ? 2 : 1))) result.push([x, y, z]);
    }
    return result;
  };
  const index = (x, y, z, width, height) => (z * height + y) * width + x;
  const inside = (x, y, z, width, height, depth) => x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
  const count = cells => cells.reduce((sum, value) => sum + (value ? 1 : 0), 0);

  function controls(id, name, label, entries, value) {
    const escape = window.WikiVisuals.escape;
    return `<label for="${id}-${name}">${escape(label)} <select class="visual-select" id="${id}-${name}" data-control="${name}">${entries.map(entry => {
      const [key, title] = Array.isArray(entry) ? entry : [entry, entry];
      return `<option value="${escape(key)}"${String(key) === String(value) ? ' selected' : ''}>${escape(title)}</option>`;
    }).join('')}</select></label>`;
  }
  function button(action, label) {
    return `<button type="button" class="visual-button" data-action="${action}">${label}</button>`;
  }
  function pane(title, body) {
    return `<div class="visual-panel"><strong>${window.WikiVisuals.escape(title)}</strong>${body}</div>`;
  }
  function live(text) {
    return `<output class="visual-output" aria-live="polite" aria-atomic="true">${window.WikiVisuals.escape(text)}</output>`;
  }
  function bind(host, cleanups, change, click) {
    const { listen } = window.WikiVisuals;
    if (change) listen(host, 'change', event => {
      if (event.target.matches('[data-control]')) change(event.target.dataset.control, event.target.value);
    }, cleanups);
    if (click) listen(host, 'click', event => {
      const target = event.target.closest('[data-action]');
      if (target && host.contains(target)) click(target.dataset.action);
    }, cleanups);
  }

  factories['flood-fill'] = function (host) {
    const V = window.WikiVisuals, id = `visual-flood-${++sequence}`, cleanups = [];
    const width = 5, height = 2;
    let seed = 0, queue, seen, accepted, rejected, current, work;
    V.frame(host, {
      title: 'Follow the breadth-first queue', subtitle: '5 × 2 × 1 grid · Z = 0',
      controls: controls(id, 'seed', 'Seed', [['0', '(0, 0, 0) · left'], ['4', '(4, 0, 0) · right'], ['2', '(2, 0, 0) · wall']], seed) + button('step', 'Process one candidate') + button('reset', 'Reset'),
      caption: 'N6 uses face neighbors. This depth-1 grid has no in-bounds Z neighbors. Candidates enter the queue in +X, −X, +Y, −Y order. Each click dequeues one candidate; a wall candidate consumes work but does not call the visit callback. The seed is recorded separately from its changing queue state.'
    });
    function draw() {
      const cells = Array.from({ length: 10 }, (_, i) => {
        const wall = i % width === 2;
        const state = i === current ? 'current: last processed' : queue.includes(i) ? 'queued frontier' : accepted.has(i) ? 'visited successfully' : rejected.has(i) ? 'rejected wall' : wall ? 'wall' : 'unvisited';
        return { label: i === current ? (wall ? 'C#' : 'C') : queue.includes(i) ? (wall ? 'Q#' : 'Q') : accepted.has(i) ? 'V' : wall ? '#' : '·',
          kind: i === current ? 'selected' : queue.includes(i) ? 'frontier' : accepted.has(i) ? 'a' : wall ? 'blocked' : 'empty',
          title: `(${i % width}, ${Math.floor(i / width)}, 0): ${state}${i === seed ? '; seed' : ''}; value ${wall ? 9 : accepted.has(i) ? 7 : 0}` };
      });
      const coordinate = i => `(${i % width}, ${Math.floor(i / width)}, 0)`;
      const last = current === null ? 'No candidate processed yet.' : `Current C = ${coordinate(current)}: ${accepted.has(current) ? 'accepted; wrote 7' : 'wall rejected; kept 9'}.`;
      host.querySelector('[data-visual-body]').innerHTML = V.matrix({ columns: width, rows: height, cells, label: 'Flood-fill queue, wall, and visited cells at Z equals zero' }) +
        live(`${queue.length ? 'RUNNING' : 'COMPLETED'} · seed ${coordinate(seed)} · work ${work} · successful visits ${accepted.size} · queued ${queue.length}. ${last}`) +
        `<p class="visual-legend">C = current (last processed) · Q = queued candidate · V = earlier successful visit · # = wall (also marked in C# and Q#) · · = unvisited. Successful visits include C when accepted.</p>` +
        `<p class="visual-output">Next candidates: ${queue.length ? queue.map(coordinate).join(' → ') : 'none'}</p>`;
      host.querySelector('[data-action="step"]').disabled = queue.length === 0;
    }
    function reset() {
      queue = [seed]; seen = new Set(queue); accepted = new Set(); rejected = new Set(); current = null; work = 0;
      draw();
    }
    function step() {
      if (!queue.length) return;
      current = queue.shift(); work++;
      const x = current % width, y = Math.floor(current / width);
      if (x === 2) rejected.add(current);
      else {
        accepted.add(current);
        [[1, 0], [-1, 0], [0, 1], [0, -1]].forEach(([dx, dy]) => {
          const nx = x + dx, ny = y + dy, next = ny * width + nx;
          if (inside(nx, ny, 0, width, height, 1) && !seen.has(next)) { seen.add(next); queue.push(next); }
        });
      }
      draw();
    }
    bind(host, cleanups, (_, value) => { seed = Number(value); reset(); }, action => action === 'step' ? step() : reset());
    reset();
    return () => cleanups.forEach(cleanup => cleanup());
  };

  factories.components = function (host) {
    const V = window.WikiVisuals, id = `visual-components-${++sequence}`, cleanups = [];
    const width = 3, height = 3, depth = 2;
    const points = [[0, 0, 0], [1, 1, 0], [2, 2, 1]];
    const foreground = new Set(points.map(([x, y, z]) => index(x, y, z, width, height)));
    let neighborhood = 'N6';
    V.frame(host, {
      title: 'Change contact rules, change regions', subtitle: 'Two slices of one 3 × 3 × 2 grid',
      controls: controls(id, 'neighborhood', 'Connectivity', [['N6', 'N6 · faces'], ['N18', 'N18 · faces + edges'], ['N26', 'N26 · faces + edges + corners']], neighborhood),
      caption: 'These are the three foreground cells from the Java example. A → B changes X and Y: edge contact. B → C changes X, Y, and Z: corner contact. Numbers are output component labels, not source values; background always receives 0. Labels follow the X-fastest, then Y, then Z discovery scan.'
    });
    function draw() {
      const labels = Array(18).fill(0), adjacent = offsets(neighborhood);
      let regions = 0;
      for (let i = 0; i < labels.length; i++) {
        if (!foreground.has(i) || labels[i]) continue;
        labels[i] = ++regions;
        const queue = [i];
        for (let head = 0; head < queue.length; head++) {
          const cell = queue[head], z = Math.floor(cell / 9), y = Math.floor(cell / 3) % 3, x = cell % 3;
          adjacent.forEach(([dx, dy, dz]) => {
            const nx = x + dx, ny = y + dy, nz = z + dz, next = index(nx, ny, nz, width, height);
            if (inside(nx, ny, nz, width, height, depth) && foreground.has(next) && !labels[next]) { labels[next] = regions; queue.push(next); }
          });
        }
      }
      const panes = [0, 1].map(z => pane(`Z = ${z}`, V.matrix({ columns: width, rows: height,
        label: `Component labels at Z equals ${z} using ${neighborhood}`,
        cells: labels.slice(z * 9, z * 9 + 9).map((value, i) => ({ label: value || '·', kind: value ? ['a', 'b', 'selected'][value - 1] : 'empty', title: `(${i % 3}, ${Math.floor(i / 3)}, ${z}): ${value ? `foreground, component ${value}` : 'background, label 0'}` }))
      })));
      const componentLabels = points.map(([x, y, z]) => labels[index(x, y, z, width, height)]);
      host.querySelector('[data-visual-body]').innerHTML = `<div class="visual-panes">${panes.join('')}</div>` +
        live(`${neighborhood}: ${regions} component${regions === 1 ? '' : 's'} · A (0,0,0) → ${componentLabels[0]} · B (1,1,0) → ${componentLabels[1]} · C (2,2,1) → ${componentLabels[2]}`) +
        '<p class="visual-legend">Equal numbers = same connected region · · = background (label 0). Both panels belong to the same volume.</p>';
    }
    bind(host, cleanups, (_, value) => { neighborhood = value; draw(); });
    draw();
    return () => cleanups.forEach(cleanup => cleanup());
  };

  factories.morphology = function (host) {
    const V = window.WikiVisuals, id = `visual-morphology-${++sequence}`, cleanups = [];
    const width = 9, height = 9, depth = 7;
    let operation = 'dilate', neighborhood = 'N6', slice = 3;
    const source = Array(width * height * depth).fill(0);
    for (let z = 1; z <= 5; z++) for (let y = 2; y <= 6; y++) for (let x = 2; x <= 6; x++) {
      if (x !== 4 || y !== 4) source[index(x, y, z, width, height)] = 1;
    }
    source[index(0, 0, 3, width, height)] = 1;
    V.frame(host, {
      title: 'Watch a mask change shape', subtitle: 'Full 3D operation · one XY slice shown',
      controls: controls(id, 'operation', 'Operation', [['dilate', 'Dilate'], ['erode', 'Erode'], ['open', 'Open · erode → dilate'], ['close', 'Close · dilate → erode']], operation) +
        controls(id, 'neighborhood', 'Neighborhood', ['N6', 'N18', 'N26'], neighborhood) + controls(id, 'slice', 'Z slice', [0, 1, 2, 3, 4, 5, 6], slice),
      caption: 'The bounded 9 × 9 × 7 source contains a block with a thin tunnel and one isolated cell. Every operation runs on the entire volume, including neighbors in other Z slices. Outside the volume is background. Open and close use a separate intermediate binary mask. This illustrates ordinary bounded-grid behavior; views can alter inside() semantics.'
    });
    function pass(input, dilate) {
      const adjacent = offsets(neighborhood);
      return input.map((original, i) => {
        const z = Math.floor(i / 81), y = Math.floor(i / 9) % 9, x = i % 9;
        let on = Boolean(original);
        if (on !== dilate) {
          for (const [dx, dy, dz] of adjacent) {
            const nx = x + dx, ny = y + dy, nz = z + dz;
            const neighbor = inside(nx, ny, nz, width, height, depth) && Boolean(input[index(nx, ny, nz, width, height)]);
            if (neighbor === dilate) { on = dilate; break; }
          }
        }
        return on ? 1 : 0;
      });
    }
    function draw() {
      const result = operation === 'dilate' ? pass(source, true) : operation === 'erode' ? pass(source, false) :
        operation === 'open' ? pass(pass(source, false), true) : pass(pass(source, true), false);
      const originalSlice = source.slice(slice * 81, (slice + 1) * 81), resultSlice = result.slice(slice * 81, (slice + 1) * 81);
      const plot = (mask, title, resultPanel) => pane(title, V.matrix({ columns: width, rows: height, label: `${title} at Z equals ${slice}`,
        cells: mask.map((value, i) => ({ label: value ? '1' : '·', kind: value ? (resultPanel ? 'a' : 'selected') : 'empty', title: `(${i % 9}, ${Math.floor(i / 9)}, ${slice}): ${value ? 'foreground 1' : 'background 0'}` }))
      }));
      const explanation = { dilate: 'Source OR any selected neighbor.', erode: 'Source AND every selected neighbor.', open: 'Erode first, then dilate the binary intermediate.', close: 'Dilate first, then erode the binary intermediate.' }[operation];
      host.querySelector('[data-visual-body]').innerHTML = `<div class="visual-panes">${plot(originalSlice, 'Original mask', false)}${plot(resultSlice, `${operation[0].toUpperCase()}${operation.slice(1)} result`, true)}</div>` +
        live(`${operation} · ${neighborhood} · Z = ${slice}: foreground ${count(originalSlice)} → ${count(resultSlice)} in this slice; ${count(source)} → ${count(result)} in the whole volume. ${explanation}`) +
        '<p class="visual-legend">1 = foreground · · = background (0). Change Z to see the effect above and below the center slice.</p>';
    }
    bind(host, cleanups, (key, value) => { if (key === 'operation') operation = value; else if (key === 'neighborhood') neighborhood = value; else slice = Number(value); draw(); });
    draw();
    return () => cleanups.forEach(cleanup => cleanup());
  };

  factories['distance-map'] = function (host) {
    const V = window.WikiVisuals, id = `visual-distance-${++sequence}`, cleanups = [];
    const width = 7, height = 7;
    let foreground = new Set([24]), x = 3, y = 3;
    V.frame(host, {
      title: 'Measure cost to the nearest foreground cell', subtitle: '7 × 7 × 1 mask · Z = 0 · raw units',
      controls: controls(id, 'preset', 'Mask preset', [['center', 'One center cell'], ['two', 'Two foreground cells'], ['none', 'No foreground'], ['custom', 'Custom mask']], 'center') +
        controls(id, 'x', 'Edit X', [0, 1, 2, 3, 4, 5, 6], x) + controls(id, 'y', 'Edit Y', [0, 1, 2, 3, 4, 5, 6], y) + button('toggle', 'Toggle selected cell'),
      caption: 'Only Z = 0 exists in this depth-1 mask. Horizontal and vertical face steps cost 3; XY diagonal edge steps cost 4. A full 3D corner step changes X, Y, and Z together and costs 5; it cannot occur in this slice. Values are unnormalized chamfer path costs, not Euclidean distances. The figure calculates final values directly; it does not animate the Java forward/backward passes.'
    });
    function draw() {
      const distances = Array.from({ length: 49 }, (_, i) => {
        let result = Infinity;
        foreground.forEach(cell => {
          const dx = Math.abs(i % width - cell % width), dy = Math.abs(Math.floor(i / width) - Math.floor(cell / width));
          result = Math.min(result, 4 * Math.min(dx, dy) + 3 * Math.abs(dx - dy));
        });
        return result;
      });
      const selected = y * width + x;
      const cells = distances.map((distance, i) => ({ label: distance === Infinity ? '∞' : String(distance),
        kind: i === selected ? 'frontier' : foreground.has(i) ? 'selected' : distance <= 4 ? 'a' : 'empty',
        title: `(${i % width}, ${Math.floor(i / width)}, 0): ${foreground.has(i) ? 'foreground; ' : ''}distance ${distance}${i === selected ? '; edit selection' : ''}` }));
      const selectedDistance = distances[selected] === Infinity ? 'Infinity' : String(distances[selected]);
      host.querySelector('[data-visual-body]').innerHTML = V.matrix({ columns: width, rows: height, cells, label: 'Raw 3-4 chamfer distances to foreground on the Z equals zero slice' }) +
        live(`${foreground.size} foreground cell${foreground.size === 1 ? '' : 's'} · edit selection (${x}, ${y}, 0): ${foreground.has(selected) ? 'foreground' : 'background'}, distance ${selectedDistance}. ${foreground.size ? `Largest cost in this mask: ${Math.max(...distances)}.` : 'No foreground: every cell is Infinity.'}`) +
        '<p class="visual-legend">0 = foreground · numbers = minimum raw path cost · ∞ = no foreground. Edit X/Y chooses a cell; the button changes its foreground membership.</p>';
      host.querySelector('[data-action="toggle"]').textContent = `${foreground.has(selected) ? 'Remove foreground' : 'Add foreground'} at (${x}, ${y}, 0)`;
    }
    bind(host, cleanups, (key, value) => {
      if (key === 'x') x = Number(value);
      else if (key === 'y') y = Number(value);
      else if (value === 'center') foreground = new Set([24]);
      else if (value === 'two') foreground = new Set([15, 33]);
      else if (value === 'none') foreground = new Set();
      draw();
    }, () => {
      const selected = y * width + x;
      if (foreground.has(selected)) foreground.delete(selected); else foreground.add(selected);
      host.querySelector('[data-control="preset"]').value = 'custom';
      draw();
    });
    draw();
    return () => cleanups.forEach(cleanup => cleanup());
  };

  factories['work-budget'] = function (host) {
    const V = window.WikiVisuals, id = `visual-budget-${++sequence}`, cleanups = [];
    let budget = 3, work = 0, calls = 0, returned = 0, status = 'RUNNING';
    V.frame(host, {
      title: 'Spend a budget, then resume the same task', subtitle: 'GridOps.beginFill · 5 × 3 × 1 region',
      controls: controls(id, 'budget', 'Work budget per call', [0, 1, 3, 5, 15], budget) + button('step', 'Call step(3)') + button('cancel', 'Cancel task') + button('reset', 'Start new task'),
      caption: 'This model fills a bounded region with value 7 in X-fastest, then Y, then Z order. GridOps.beginFill writes one cell per work unit. Each click is one synchronous call; Ashgrid provides no scheduler. Zero budget does no work. Cancellation is terminal and retains existing writes; starting a new illustration resets its grid. Other operations define different work units.'
    });
    function draw() {
      const cells = Array.from({ length: 15 }, (_, i) => ({ label: i < work ? '7' : '0', kind: i < work ? 'a' : 'empty', title: `(${i % 5}, ${Math.floor(i / 5)}, 0): ${i < work ? 'written, value 7' : 'unchanged, value 0'}` }));
      const detail = status === 'COMPLETED' ? 'Output is final.' : status === 'CANCELLED' ? 'Existing writes remain. This task cannot resume; step returns 0.' : 'Output is partial. Keep this task to resume.';
      host.querySelector('[data-visual-body]').innerHTML = V.matrix({ columns: 5, rows: 3, cells, label: 'Fill-task destination, showing completed writes and unchanged cells' }) +
        `<label for="${id}-progress">Written cells: ${work} of 15</label><progress id="${id}-progress" max="15" value="${work}" style="width:100%">${work} / 15</progress>` +
        live(`${status} · workDone() = ${work} · step calls = ${calls} · last step returned ${returned}. ${detail}`) +
        '<p class="visual-legend">7 = cell already written · 0 = cell still unchanged. A work unit is not a millisecond or a Minecraft tick.</p>';
      host.querySelector('[data-action="step"]').textContent = `Call step(${budget})`;
      host.querySelector('[data-action="cancel"]').disabled = status !== 'RUNNING';
    }
    bind(host, cleanups, (_, value) => { budget = Number(value); draw(); }, action => {
      if (action === 'reset') { work = 0; calls = 0; returned = 0; status = 'RUNNING'; }
      else if (action === 'cancel' && status === 'RUNNING') status = 'CANCELLED';
      else if (action === 'step') {
        calls++; returned = status === 'RUNNING' ? Math.min(budget, 15 - work) : 0; work += returned;
        if (status === 'RUNNING' && work === 15) status = 'COMPLETED';
      }
      draw();
    });
    draw();
    return () => cleanups.forEach(cleanup => cleanup());
  };
})();
