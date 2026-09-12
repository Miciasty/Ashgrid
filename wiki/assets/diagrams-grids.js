/* Small XZ teaching models of Ashgrid contracts. These figures do not execute Java. */
(() => {
  'use strict';
  const factories = window.WikiDiagramFactories ||= {};
  let sequence = 0;
  const count = values => values.filter(Boolean).length;
  const options = (values, selected) => values.map(value => `<option value="${value}"${value === selected ? ' selected' : ''}>${value}</option>`).join('');
  const range = (start, length) => Array.from({ length }, (_, i) => start + i);
  function createRenderer(body) {
    body.innerHTML = '<div data-grid-scene></div><output class="visual-output" aria-live="polite" aria-atomic="true" data-grid-status></output>';
    const scene = body.querySelector('[data-grid-scene]');
    const status = body.querySelector('[data-grid-status]');
    return (sceneMarkup, statusMarkup) => {
      scene.innerHTML = sceneMarkup;
      status.innerHTML = statusMarkup;
    };
  }

  factories['chunk-map'] = host => {
    const { frame, escape, listen } = window.WikiVisuals;
    const cleanups = [], id = `chunk-map-${++sequence}`;
    const state = { x: -1, z: 1, size: 4 };
    frame(host, {
      title: 'A negative cell still has a non-negative local address',
      subtitle: 'XZ slice · unit-grid coordinates · Y does not affect this mapping',
      controls: `<label for="${id}-x">Cell X <select class="visual-select" id="${id}-x" data-axis="x">${options(range(-4, 8), state.x)}</select></label>
        <label for="${id}-z">Cell Z <select class="visual-select" id="${id}-z" data-axis="z">${options(range(-4, 8), state.z)}</select></label>
        <label for="${id}-size">Chunk edge <select class="visual-select" id="${id}-size" data-size>${options([2, 4], state.size)}</select> cells</label>`,
      caption: 'The heavy outlines are XZ chunk boundaries. Select a cell using X and Z; the dot marks it, and the two numbers inside each cell are local X,Z. Local coordinates always lie in [0, size). Storage chunks may use different dimensions.'
    });
    const render = createRenderer(host.querySelector('[data-visual-body]'));
    function draw() {
      const { x, z, size } = state, cx = Math.floor(x / size), cz = Math.floor(z / size);
      const lx = x - cx * size, lz = z - cz * size;
      let svg = `<svg viewBox="0 0 430 408" role="img" aria-labelledby="${id}-title ${id}-desc" style="display:block;width:100%;height:auto;max-width:500px;margin:auto"><title id="${id}-title">Signed XZ chunk map</title><desc id="${id}-desc">${escape(`Cell (${x},${z}) is in chunk (${cx},${cz}), local (${lx},${lz}). Displayed X and Z range from -4 through 3. Z increases down the image.`)}</desc>`;
      for (let row = 0; row < 8; row++) {
        for (let col = 0; col < 8; col++) {
          const wx = col - 4, wz = row - 4, selected = wx === x && wz === z;
          const same = Math.floor(wx / size) === cx && Math.floor(wz / size) === cz;
          const localX = wx - Math.floor(wx / size) * size, localZ = wz - Math.floor(wz / size) * size;
          svg += `<rect x="${52 + col * 42}" y="${32 + row * 42}" width="42" height="42" fill="${selected ? 'var(--accent)' : same ? 'var(--diagram-cube)' : 'var(--diagram-muted-surface)'}" stroke="var(--diagram-grid)"/>`;
          svg += `<text x="${73 + col * 42}" y="${57 + row * 42}" text-anchor="middle" font-size="11" fill="${selected ? 'var(--bg)' : 'var(--text)'}">${localX},${localZ}</text>`;
          if (selected) svg += `<circle cx="${87 + col * 42}" cy="${39 + row * 42}" r="3" fill="var(--bg)"/>`;
        }
      }
      for (let row = 0; row < 8; row += size) for (let col = 0; col < 8; col += size) {
        svg += `<rect x="${52 + col * 42}" y="${32 + row * 42}" width="${size * 42}" height="${size * 42}" fill="none" stroke="var(--text)" stroke-width="2"/>`;
      }
      for (let i = 0; i < 8; i++) svg += `<text x="${73 + i * 42}" y="23" text-anchor="middle" font-size="12" fill="var(--muted)">${i - 4}</text><text x="39" y="${57 + i * 42}" text-anchor="end" font-size="12" fill="var(--muted)">${i - 4}</text>`;
      svg += '<text x="389" y="23" font-size="12" fill="var(--text)">X</text><text x="20" y="390" font-size="12" fill="var(--text)">Z ↓</text></svg>';
      render(`${svg}`, `Cell (${x}, ${z}) → chunk (${cx}, ${cz}) → local (${lx}, ${lz})<br>X: floor(${x} / ${size}) = ${cx}; ${x} − (${cx} × ${size}) = ${lx}<br>Z: floor(${z} / ${size}) = ${cz}; ${z} − (${cz} × ${size}) = ${lz}`);
    }
    host.querySelectorAll('[data-axis]').forEach(input => listen(input, 'change', () => { state[input.dataset.axis] = Number(input.value); draw(); }, cleanups));
    listen(host.querySelector('[data-size]'), 'change', event => { state.size = Number(event.target.value); draw(); }, cleanups);
    draw();
    return () => cleanups.forEach(fn => fn());
  };

  factories['storage-backends'] = host => {
    const { frame, matrix, listen } = window.WikiVisuals;
    const cleanups = [], id = `storage-backends-${++sequence}`;
    let values = [], allocated = new Set();
    const chunkKey = index => `${Math.floor((index % 8) / 2)},${Math.floor(Math.floor(index / 8) / 2)}`;
    function pattern(name) {
      const selected = name === 'clustered' ? [0, 1, 8, 9] : name === 'scattered' ? [0, 2, 20, 22] : name === 'full' ? range(0, 32) : [];
      values = range(0, 32).map(index => selected.includes(index) ? 1 : 0);
      allocated = new Set(selected.map(chunkKey));
    }
    pattern('clustered');
    frame(host, {
      title: 'One mask, four allocation strategies',
      subtitle: 'Complete 8 × 1 × 4 example · XZ slice at Y = 0 · default value 0',
      controls: `<label for="${id}-pattern">New example <select class="visual-select" id="${id}-pattern" data-pattern><option value="clustered">4 clustered cells</option><option value="scattered">4 scattered cells</option><option value="empty">Empty</option><option value="full">Full</option></select></label>
        <button class="visual-button" type="button" data-clear>Write 0 to every cell</button><button class="visual-button" type="button" data-prune>Prune empty chunks</button>`,
      caption: '1 is foreground; 0 is an allocated default; · is an absent sparse slot that reads as 0. Each storage chunk is 2 × 1 × 2 (4 integer slots). Counts describe this example, not JVM heap bytes or measured performance. Choosing a new example starts with fresh storage.'
    });
    const render = createRenderer(host.querySelector('[data-visual-body]'));
    function draw(message = '') {
      const stored = count(values);
      const panels = [
        ['ArrayGrid3i', '32 allocated integer slots', values.map(value => ({ label: String(value), kind: value ? 'selected' : 'empty', title: value ? 'Stored foreground value 1' : 'Allocated default value 0' }))],
        ['BitGrid3', '32 logical boolean cells', values.map(value => ({ label: String(value), kind: value ? 'selected' : 'empty', title: value ? 'true' : 'false' }))],
        ['HashSparseGrid3i', `${stored} entries`, values.map(value => ({ label: value ? '1' : '·', kind: value ? 'selected' : 'empty', title: value ? 'Entry stores 1; has is true' : 'No entry; get returns 0; has is false' }))],
        ['ChunkedGrid3i', `${allocated.size} chunks · ${allocated.size * 4} allocated integer slots`, values.map((value, index) => ({ label: value ? '1' : allocated.has(chunkKey(index)) ? '0' : '·', kind: value ? 'selected' : allocated.has(chunkKey(index)) ? 'frontier' : 'empty', title: value ? 'Stored value 1; has is true' : allocated.has(chunkKey(index)) ? 'Allocated default 0; has is true' : 'No chunk; get returns 0; has is false' }))]
      ];
      render(`<div class="visual-panes">${panels.map(([title, metric, cells]) => `<section class="visual-panel"><h4>${title}</h4><p>${metric}</p>${matrix({ columns: 8, rows: 4, cells, label: `${title}, XZ slice at Y zero`, yLabel: 'Z' })}</section>`).join('')}</div>`, `${stored} foreground cells. Both sparse backends report storedCellCount() = ${stored}. Chunked storage reports allocatedCellCount() = ${allocated.size * 4}.${message ? ` ${message}` : ''}`);
    }
    listen(host.querySelector('[data-pattern]'), 'change', event => { pattern(event.target.value); draw('Fresh backends created.'); }, cleanups);
    listen(host.querySelector('[data-clear]'), 'click', () => { values.fill(0); draw('Hash entries were removed. Existing chunks remain allocated.'); }, cleanups);
    listen(host.querySelector('[data-prune]'), 'click', () => {
      const before = allocated.size;
      allocated = new Set([...allocated].filter(key => values.some((value, index) => value && chunkKey(index) === key)));
      draw(`pruneEmptyChunks() removed ${before - allocated.size} chunk(s).`);
    }, cleanups);
    draw();
    return () => cleanups.forEach(fn => fn());
  };

  factories['grid-views'] = host => {
    const { frame, matrix, listen } = window.WikiVisuals;
    const cleanups = [], id = `grid-views-${++sequence}`;
    let selected = 1, value = 9;
    const source = Array(24).fill(0);
    const sourceIndex = local => (Math.floor(local / 3) + 1) * 6 + (local % 3) + 1;
    range(0, 6).forEach(index => { source[sourceIndex(index)] = index + 1; });
    const readWindow = () => range(0, 6).map(index => source[sourceIndex(index)]);
    let snapshot = readWindow();
    frame(host, {
      title: 'A window stays live; a snapshot keeps its own values',
      subtitle: 'XZ slices · source Y = 60 · window and snapshot local Y = 0',
      controls: `<label for="${id}-cell">Local window cell <select class="visual-select" id="${id}-cell" data-cell>${range(0, 6).map(index => `<option value="${index}"${index === selected ? ' selected' : ''}>(${index % 3}, 0, ${Math.floor(index / 3)})</option>`).join('')}</select></label>
        <label for="${id}-value">Value <select class="visual-select" id="${id}-value" data-value>${options(range(0, 10), value)}</select></label>
        <button class="visual-button" type="button" data-write="source">Write source</button><button class="visual-button" type="button" data-write="view">Write through window</button><button class="visual-button" type="button" data-write="snapshot">Write snapshot</button><button class="visual-button" type="button" data-capture>Take new snapshot</button>`,
      caption: 'The source is HashSparseGrid3i with default 0. The 3 × 1 × 2 window starts at source (−1, 60, 0); its local origin is (0, 0, 0). The source panel shows X = −2…3 and Z = −1…2. Only this displayed source region is finite; the sparse backend itself is not. A dot marks the selected cell in each panel.'
    });
    const render = createRenderer(host.querySelector('[data-visual-body]'));
    function draw(action = 'The initial snapshot has copied all six window values.') {
      const windowValues = readWindow(), sourceCell = sourceIndex(selected);
      const cell = (number, selectedCell, title) => ({ label: `${number}${selectedCell ? '•' : ''}`, kind: selectedCell ? 'selected' : number ? 'a' : 'empty', title });
      const sourceCells = source.map((number, index) => cell(number, index === sourceCell, `Source (${index % 6 - 2},60,${Math.floor(index / 6) - 1}) = ${number}`));
      const localCells = numbers => numbers.map((number, index) => cell(number, index === selected, `Local (${index % 3},0,${Math.floor(index / 3)}) = ${number}`));
      render(`<div class="visual-panes"><section class="visual-panel"><h4>Source storage</h4><p>Signed coordinates · Y = 60</p>${matrix({ columns: 6, rows: 4, cells: sourceCells, label: 'Source XZ slice, X from minus two, Z from minus one', xLabel: 'X', yLabel: 'Z', columnStart: -2, rowStart: -1 })}</section><section class="visual-panel"><h4>Live window</h4><p>Same values through local coordinates</p>${matrix({ columns: 3, rows: 2, cells: localCells(windowValues), label: 'Live window local XZ slice', yLabel: 'Z' })}</section><section class="visual-panel"><h4>Detached snapshot</h4><p>Independent dense copy</p>${matrix({ columns: 3, rows: 2, cells: localCells(snapshot), label: 'Detached snapshot local XZ slice', yLabel: 'Z' })}</section></div>`, `Local (${selected % 3}, 0, ${Math.floor(selected / 3)}) ↔ source (${selected % 3 - 1}, 60, ${Math.floor(selected / 3)}). Source = ${source[sourceCell]}, window = ${windowValues[selected]}, snapshot = ${snapshot[selected]}. ${action}`);
    }
    listen(host.querySelector('[data-cell]'), 'change', event => { selected = Number(event.target.value); draw('Selection changed; stored values are unchanged.'); }, cleanups);
    listen(host.querySelector('[data-value]'), 'change', event => { value = Number(event.target.value); }, cleanups);
    host.querySelectorAll('[data-write]').forEach(button => listen(button, 'click', () => {
      const target = button.dataset.write;
      if (target === 'snapshot') snapshot[selected] = value; else source[sourceIndex(selected)] = value;
      draw(target === 'snapshot' ? 'Snapshot edited; source and window are unchanged.' : target === 'view' ? 'Window write reached the source; snapshot is unchanged.' : 'Source edited; the live window sees it immediately. Snapshot is unchanged.');
    }, cleanups));
    listen(host.querySelector('[data-capture]'), 'click', () => { snapshot = readWindow(); draw('New snapshot captured from the current window.'); }, cleanups);
    draw();
    return () => cleanups.forEach(fn => fn());
  };

  factories['set-operations'] = host => {
    const { frame, matrix, listen } = window.WikiVisuals;
    const cleanups = [], id = `set-operations-${++sequence}`;
    let operation = 'union', selected = 9;
    const a = range(0, 24).map(index => index % 6 >= 1 && index % 6 <= 3 && Math.floor(index / 6) >= 1 && Math.floor(index / 6) <= 2 ? 7 : 0);
    const b = range(0, 24).map(index => index % 6 >= 3 && index % 6 <= 4 && Math.floor(index / 6) <= 2 ? 9 : 0);
    const operations = { union: ['union', (av, bv) => av || bv, 'A or B'], intersect: ['intersection', (av, bv) => av && bv, 'A and B'], subtract: ['subtraction A − B', (av, bv) => av && !bv, 'A and not B'], invert: ['inversion of A', av => !av, 'not A; B is unused'] };
    frame(host, {
      title: 'See which cells survive a set operation',
      subtitle: '6 × 1 × 4 grids · XZ slice at Y = 0 · foreground predicate: value > 0',
      controls: `<label for="${id}-operation">Operation <select class="visual-select" id="${id}-operation" data-operation>${Object.entries(operations).map(([key, [label]]) => `<option value="${key}">${label}</option>`).join('')}</select></label>
        <label for="${id}-cell">Edit cell <select class="visual-select" id="${id}-cell" data-cell>${range(0, 24).map(index => `<option value="${index}"${index === selected ? ' selected' : ''}>(${index % 6}, 0, ${Math.floor(index / 6)})</option>`).join('')}</select></label>
        <button class="visual-button" type="button" data-toggle="a">Toggle A: 0 ↔ 7</button><button class="visual-button" type="button" data-toggle="b">Toggle B: 0 ↔ 9</button>`,
      caption: 'Choose an operation, then toggle the selected cell in either input. A uses label 7 and B uses label 9; the result always contains 0 or 1. Inversion is limited to these 24 cells. This illustration writes to independent result storage, with no shifted or overlapping views.'
    });
    const render = createRenderer(host.querySelector('[data-visual-body]'));
    function draw() {
      const result = a.map((av, index) => operations[operation][1](av > 0, b[index] > 0) ? 1 : 0);
      const panels = [['A', a, 'a'], [operation === 'invert' ? 'B · unused by invert' : 'B', b, 'b'], ['Result', result, 'selected']];
      render(`<div class="visual-panes">${panels.map(([title, values, kind]) => `<section class="visual-panel"><h4>${title}</h4><p>${count(values)} foreground cells</p>${matrix({ columns: 6, rows: 4, cells: values.map((value, index) => ({ label: `${value}${index === selected ? '•' : ''}`, kind: value ? kind : 'empty', title: `${title} (${index % 6},0,${Math.floor(index / 6)}) = ${value}${index === selected ? '; selected edit cell' : ''}` })), label: `${title}, XZ slice at Y zero`, yLabel: 'Z' })}</section>`).join('')}</div>`, `Rule: ${operations[operation][2]}. Selected cell (${selected % 6}, 0, ${Math.floor(selected / 6)}): A = ${a[selected]}, B = ${b[selected]}, result = ${result[selected]}. Total result: ${count(result)} foreground cells.`);
      host.querySelector('[data-toggle="b"]').disabled = operation === 'invert';
    }
    listen(host.querySelector('[data-operation]'), 'change', event => { operation = event.target.value; draw(); }, cleanups);
    listen(host.querySelector('[data-cell]'), 'change', event => { selected = Number(event.target.value); draw(); }, cleanups);
    host.querySelectorAll('[data-toggle]').forEach(button => listen(button, 'click', () => {
      const values = button.dataset.toggle === 'a' ? a : b, foreground = button.dataset.toggle === 'a' ? 7 : 9;
      values[selected] = values[selected] ? 0 : foreground;
      draw();
    }, cleanups));
    draw();
    return () => cleanups.forEach(fn => fn());
  };
})();
