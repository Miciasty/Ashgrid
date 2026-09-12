/* Bounded illustrations of Ashgrid's voxel contracts. These figures run locally
   in the browser; they do not invoke Java or inspect Minecraft world data. */
(function () {
  'use strict';
  const factories = window.WikiDiagramFactories = window.WikiDiagramFactories || {};
  let sequence = 0;
  const key = p => p.join(',');
  const point = p => `(${p.join(',')})`;
  const number = n => Number(n.toFixed(3)).toString();
  const dispose = cleanups => () => cleanups.forEach(cleanup => cleanup());

  // Coordinate labels describe screen orientation explicitly: X right, Y down.
  // Numeric labels and per-cell titles carry meaning independently of color.
  function grid({ columns = 3, rows = 3, minX = 0, minY = 0, z = 0, label, cellAt, segment }) {
    const esc = window.WikiVisuals.escape;
    const size = 48, left = 28, top = 30;
    const width = left + columns * size + 12, height = top + rows * size + 34;
    let svg = `<svg class="diagram-component diagram-scene visual-matrix" viewBox="0 0 ${width} ${height}" role="img" aria-label="${esc(label)}"><title>${esc(label)}</title>`;
    let labels = '';
    for (let row = 0; row < rows; row++) {
      for (let column = 0; column < columns; column++) {
        const x = minX + column, y = minY + row, cell = cellAt(x, y, z) || {};
        const px = left + column * size, py = top + row * size;
        const fill = cell.kind === 'selected' ? 'var(--accent)' : cell.kind === 'blocked' ? 'var(--diagram-x)' : cell.kind === 'a' ? 'var(--diagram-y)' : cell.kind === 'b' ? 'var(--diagram-z)' : 'var(--diagram-muted-surface)';
        const opacity = cell.kind && cell.kind !== 'empty' ? '.25' : '1';
        const stroke = cell.kind === 'selected' ? 'var(--accent)' : cell.kind === 'blocked' ? 'var(--diagram-x)' : 'var(--muted)';
        svg += `<g><title>${esc(`${point([x, y, z])}: ${cell.title || 'not selected'}`)}</title><rect x="${px}" y="${py}" width="${size}" height="${size}" fill="${fill}" fill-opacity="${opacity}" stroke="${stroke}" stroke-width="${cell.kind === 'selected' ? 2 : .6}"${cell.zero ? ' stroke-dasharray="4 3"' : ''}/></g>`;
        labels += `<text x="${px + size / 2}" y="${py + size / 2 + 5}" text-anchor="middle" fill="var(--text)" font-size="13" font-weight="600" paint-order="stroke" stroke="var(--diagram-plot)" stroke-width="3" stroke-linejoin="round">${esc(cell.label || '·')}</text>`;
      }
    }
    for (let column = 0; column < columns; column++) svg += `<text x="${left + (column + .5) * size}" y="19" text-anchor="middle" fill="var(--muted)" font-size="11">${minX + column}</text>`;
    for (let row = 0; row < rows; row++) svg += `<text x="15" y="${top + (row + .5) * size + 4}" text-anchor="middle" fill="var(--muted)" font-size="11">${minY + row}</text>`;
    if (segment) {
      const [a, b] = segment;
      const ax = left + (a[0] - minX) * size, ay = top + (a[1] - minY) * size;
      const bx = left + (b[0] - minX) * size, by = top + (b[1] - minY) * size;
      svg += `<line x1="${ax}" y1="${ay}" x2="${bx}" y2="${by}" stroke="var(--text)" stroke-width="2" opacity=".8"/><circle cx="${ax}" cy="${ay}" r="3" fill="var(--text)"/><circle cx="${bx}" cy="${by}" r="3" fill="var(--diagram-plot)" stroke="var(--text)"/>`;
    }
    return svg + labels + `<text x="${width / 2}" y="${height - 9}" text-anchor="middle" fill="var(--muted)" font-size="10">X → · Y ↓ · Z = ${z}</text></svg>`;
  }

  function panel(title, body, detail = '') {
    return `<section class="visual-panel"><h4>${title}</h4>${body}${detail ? `<p>${detail}</p>` : ''}</section>`;
  }

  // Same comparisons and one-axis-at-a-time stepping as DDA3DTraverser.
  // Fixed input has exactly equal components after normalization.
  function tieVisits() {
    const p = [0, 0, 0], delta = Math.sqrt(3), next = [delta / 2, delta / 2, delta / 2];
    const visits = [];
    let enter = 0, stepped = 'start';
    while (enter < 3) {
      const boundary = Math.min(...next);
      visits.push({ p: [...p], enter, exit: Math.min(boundary, 3), stepped });
      enter = boundary;
      if (enter >= 3) break;
      const axis = next[0] <= next[1] && next[0] <= next[2] ? 0 : next[1] <= next[2] ? 1 : 2;
      p[axis]++;
      next[axis] += delta;
      stepped = 'XYZ'[axis];
    }
    return visits;
  }

  factories['dda-ties'] = function (host) {
    const { frame, listen } = window.WikiVisuals;
    const cleanups = [], visits = tieVisits();
    let step = 0;
    frame(host, {
      title: 'Step through an exact three-axis tie',
      subtitle: 'Three XY layers · direction (1,1,1) before normalization',
      controls: '<button type="button" class="visual-button" data-previous>Previous callback</button><button type="button" class="visual-button" data-next>Next callback</button><button type="button" class="visual-button" data-reset>Reset</button>',
      caption: 'Origin (0.5,0.5,0.5), direction (1,1,1), tMax = 3 cells. Numbers are callback order; * marks a zero-length contact. At each corner X and Y produce boundary-only callbacks; the Z step then enters the diagonal cell for positive travel. DDA does not emit all eight contact cells.'
    });
    function draw() {
      const current = visits[step];
      const panes = [0, 1, 2].map(z => panel(`Z = ${z}`, grid({ z,
        label: `DDA callback ${step + 1}, XY layer Z=${z}`,
        cellAt: (x, y) => {
          const i = visits.findIndex(visit => key(visit.p) === key([x, y, z]));
          if (i < 0 || i > step) return {};
          const visit = visits[i], zero = visit.enter === visit.exit;
          return { kind: i === step ? 'selected' : 'a', zero, label: `${i + 1}${zero ? '*' : ''}`, title: `callback ${i + 1}, ${zero ? 'zero-length boundary contact' : 'positive travel'}, interval [${number(visit.enter)},${number(visit.exit)})` };
        }
      })));
      host.querySelector('[data-visual-body]').innerHTML = `<div class="visual-panes">${panes.join('')}</div><div class="visual-output" aria-live="polite" aria-atomic="true"><strong>Callback ${step + 1}/${visits.length}: ${point(current.p)}</strong><p>${current.stepped === 'start' ? 'Start at floor(origin).' : `Step ${current.stepped} from the previous callback.`} Interval [${number(current.enter)}, ${number(current.exit)}) · length ${number(current.exit - current.enter)} cells.</p><p>${current.enter === current.exit ? 'Boundary contact only: the ray has not traveled farther. The next tied axis is still pending.' : 'Positive travel through this cell.'}</p></div><p class="visual-legend">Current callback has a strong border. Earlier callbacks remain numbered. * = zero length; · = not emitted yet or outside this DDA sequence. Distances are rounded to three decimals.</p>`;
      host.querySelector('[data-previous]').disabled = step === 0;
      host.querySelector('[data-next]').disabled = step === visits.length - 1;
    }
    listen(host.querySelector('[data-previous]'), 'click', () => { step = Math.max(0, step - 1); draw(); }, cleanups);
    listen(host.querySelector('[data-next]'), 'click', () => { step = Math.min(visits.length - 1, step + 1); draw(); }, cleanups);
    listen(host.querySelector('[data-reset]'), 'click', () => { step = 0; draw(); }, cleanups);
    draw();
    return dispose(cleanups);
  };

  factories['query-comparison'] = function (host) {
    const { frame, listen } = window.WikiVisuals;
    const cleanups = [], id = `query-comparison-${++sequence}`;
    let occupied = [true, false, true, false], endpoint = 'face';
    frame(host, {
      title: 'The same cells, two different query rules',
      subtitle: 'Positive-X segment · Y = Z = 0.5',
      controls: `<label for="${id}-endpoint">Endpoint b</label><select class="visual-select" id="${id}-endpoint" data-endpoint><option value="face">On the face: X = 2</option><option value="inside">Inside cell 2: X = 2.5</option><option value="equal">Equal to a: X = 0.5</option></select>${[0, 1, 2, 3].map(x => `<button type="button" class="visual-button" data-cell="${x}" aria-pressed="${occupied[x]}">Cell ${x}: ${occupied[x] ? 'occupied' : 'empty'}</button>`).join('')}<button type="button" class="visual-button" data-reset>Reset</button>`,
      caption: 'Toggle occupancy with the cell buttons. Raycast tests the starting callback; line of sight skips it. Both use the same finite DDA interval. An endpoint on the face X=2 excludes cell 2; an endpoint inside cell 2 includes travel through that cell. Equal endpoints give no callbacks.'
    });
    function draw() {
      const end = endpoint === 'face' ? 2 : endpoint === 'inside' ? 2.5 : .5;
      const visited = endpoint === 'equal' ? [] : endpoint === 'face' ? [0, 1] : [0, 1, 2];
      const hit = visited.find(x => occupied[x]);
      const blocked = visited.slice(1).find(x => occupied[x]);
      const diagram = (kind, match) => grid({ columns: 4, rows: 1, label: `${kind}: cells along X from 0 to 3, endpoint X=${end}`, cellAt: x => {
        const tested = visited.includes(x) && (kind === 'Raycast' || x !== 0) && (match === undefined || x <= match);
        return { kind: x === match ? 'selected' : occupied[x] ? 'blocked' : tested ? 'a' : 'empty', label: x === match ? 'HIT' : kind !== 'Raycast' && x === 0 && visited.length ? 'SKIP' : tested ? 'TEST' : '—', title: `${occupied[x] ? 'occupied' : 'empty'}; ${x === match ? 'first accepted cell' : tested ? 'predicate tested; empty' : 'predicate not called'}` };
      } });
      const scene = grid({ columns: 4, rows: 1, label: `Occupied cells and segment a=(0.5,0.5,0.5) to b=(${end},0.5,0.5)`, cellAt: x => ({ kind: occupied[x] ? 'blocked' : 'empty', label: occupied[x] ? '■' : '·', title: occupied[x] ? 'occupied' : 'empty' }), segment: [[.5, .18], [end, .18]] });
      host.querySelector('[data-visual-body]').innerHTML = panel('Occupancy and segment', scene, `a = (0.5,0.5,0.5) → b = (${end},0.5,0.5). Segment length / tMax = ${end - .5}. The line is offset within the row for readability; Y and Z remain 0.5.`) + `<div class="visual-panes">${panel('Raycast.first', diagram('Raycast', hit), hit === undefined ? '<strong>null</strong> · no occupied callback' : `<strong>Hit ${point([hit, 0, 0])}</strong> · tEnter = ${hit === 0 ? 0 : hit - .5}`)}${panel('LineOfSight.clear', diagram('LineOfSight', blocked), blocked === undefined ? '<strong>true</strong> · no checked cell blocks' : `<strong>false</strong> · cell ${point([blocked, 0, 0])} blocks`)}</div><p class="visual-output" aria-live="polite" aria-atomic="true">${hit === undefined ? 'Raycast misses.' : `Raycast hits cell ${hit}.`} ${blocked === undefined ? 'Line of sight is clear.' : `Line of sight is blocked by cell ${blocked}.`} ${endpoint === 'equal' ? 'Equal finite endpoints: neither predicate runs.' : `Available DDA callbacks: ${visited.join(', ')}. Each query stops when its predicate first returns true.`}</p><p class="visual-legend">■ = occupied; HIT = first accepted cell; TEST = predicate called on an empty cell; SKIP = starting callback omitted; — = predicate not called. A filled endpoint cell can be outside the segment.</p>`;
      host.querySelectorAll('[data-cell]').forEach(button => {
        const x = Number(button.dataset.cell);
        button.setAttribute('aria-pressed', String(occupied[x]));
        button.textContent = `Cell ${x}: ${occupied[x] ? 'occupied' : 'empty'}`;
      });
    }
    listen(host.querySelector('[data-endpoint]'), 'change', event => { endpoint = event.target.value; draw(); }, cleanups);
    host.querySelectorAll('[data-cell]').forEach(button => listen(button, 'click', () => { const x = Number(button.dataset.cell); occupied[x] = !occupied[x]; draw(); }, cleanups));
    listen(host.querySelector('[data-reset]'), 'click', () => { occupied = [true, false, true, false]; endpoint = 'face'; host.querySelector('[data-endpoint]').value = endpoint; draw(); }, cleanups);
    draw();
    return dispose(cleanups);
  };

  // Literal bounded forms of BresenhamLine3D and SupercoverLine3D. The small
  // presets keep every cross product exactly representable as a JS integer.
  function thinLine(a, b) {
    const p = [...a], d = a.map((value, i) => Math.abs(b[i] - value));
    const s = a.map((value, i) => value < b[i] ? 1 : -1), cells = [];
    const m = d[0] >= d[1] && d[0] >= d[2] ? 0 : d[1] >= d[2] ? 1 : 2;
    const m1 = (m + 1) % 3, m2 = (m + 2) % 3;
    let e1 = 2 * d[m1] - d[m], e2 = 2 * d[m2] - d[m];
    while (true) {
      cells.push([...p]);
      if (p[m] === b[m]) return cells;
      if (e1 >= 0) { p[m1] += s[m1]; e1 -= 2 * d[m]; }
      if (e2 >= 0) { p[m2] += s[m2]; e2 -= 2 * d[m]; }
      p[m] += s[m]; e1 += 2 * d[m1]; e2 += 2 * d[m2];
    }
  }
  function coverLine(a, b) {
    const p = [...a], d = a.map((value, i) => Math.abs(b[i] - value));
    const s = a.map((value, i) => Math.sign(b[i] - value)), n = [1, 1, 1], cells = [[...p]];
    const compare = (i, j) => d[i] === 0 ? d[j] === 0 ? 0 : 1 : d[j] === 0 ? -1 : Math.sign(n[i] * d[j] - n[j] * d[i]);
    while (key(p) !== key(b)) {
      let min = 0;
      if (compare(1, min) < 0) min = 1;
      if (compare(2, min) < 0) min = 2;
      const axes = [0, 1, 2].reduce((mask, i) => mask | (compare(i, min) === 0 ? 1 << i : 0), 0);
      for (let mask = 1; mask <= 7; mask++) if ((mask & axes) === mask) cells.push(p.map((value, i) => value + ((mask & (1 << i)) ? s[i] : 0)));
      for (let i = 0; i < 3; i++) if (axes & (1 << i)) { p[i] += s[i]; n[i] += 2; }
    }
    return cells;
  }

  factories['line-coverage'] = function (host) {
    const { frame, listen } = window.WikiVisuals;
    const cleanups = [], id = `line-coverage-${++sequence}`;
    let preset = 'diagonal', reverse = false;
    const endpoints = { diagonal: [3, 3, 0], shallow: [4, 2, 0], corner: [1, 1, 1] };
    frame(host, {
      title: 'Thin line or every closed-cell contact?',
      subtitle: 'Compare the same endpoints side by side',
      controls: `<label for="${id}-preset">Segment</label><select class="visual-select" id="${id}-preset" data-preset><option value="diagonal">XY diagonal: (0,0,0) → (3,3,0)</option><option value="shallow">XY shallow: (0,0,0) → (4,2,0)</option><option value="corner">3D corner: (0,0,0) → (1,1,1)</option></select><button type="button" class="visual-button" data-reverse aria-pressed="false">Reverse endpoints</button><button type="button" class="visual-button" data-reset>Reset</button>`,
      caption: 'Numbers give callback order. The continuous segment joins the centers of the endpoint cells. The XY examples lie entirely in Z=0. The 3D corner example shows separate Z=0 and Z=1 layers; no projected line is drawn through those slices. Both line APIs include both endpoint cells.'
    });
    function draw() {
      const start = [0, 0, 0], end = endpoints[preset];
      const a = reverse ? end : start, b = reverse ? start : end;
      const thin = thinLine(a, b), cover = coverLine(a, b), thinSet = new Set(thin.map(key));
      const layers = preset === 'corner' ? [0, 1] : [0];
      const make = (name, cells, isCover) => panel(`${name} · ${cells.length} cells`, layers.map(z => grid({
        columns: preset === 'corner' ? 2 : 5, rows: preset === 'corner' ? 2 : 4, z,
        label: `${name}, layer Z=${z}, ${cells.filter(p => p[2] === z).length} selected cells`,
        segment: preset === 'corner' ? undefined : [a.slice(0, 2).map(value => value + .5), b.slice(0, 2).map(value => value + .5)],
        cellAt: (x, y) => {
          const index = cells.findIndex(p => key(p) === key([x, y, z]));
          return index < 0 ? {} : { kind: isCover && !thinSet.has(key([x, y, z])) ? 'b' : 'selected', label: String(index + 1), title: `callback ${index + 1}${isCover && !thinSet.has(key([x, y, z])) ? ', additional supercover cell' : ''}` };
        }
      })).join(''));
      host.querySelector('[data-visual-body]').innerHTML = `<div class="visual-panes">${make('Bresenham', thin, false)}${make('Supercover', cover, true)}</div><p class="visual-output" aria-live="polite" aria-atomic="true">${point(a)} → ${point(b)}: Bresenham ${thin.length} cells; supercover ${cover.length} cells (${cover.length - thin.length} additional). ${preset === 'corner' ? 'All eight cells sharing the corner are included by supercover.' : preset === 'diagonal' ? 'At each grid corner, supercover also includes the two cells touched only at that corner in this XY cut.' : 'The shallow segment crosses additional cells that the thin path omits.'}</p><p class="visual-legend">Number = visit order; · = omitted. Additional supercover cells use a different fill. Reversal keeps the supercover set; order can change, and Bresenham tie cells can change.</p>`;
      host.querySelector('[data-reverse]').setAttribute('aria-pressed', String(reverse));
    }
    listen(host.querySelector('[data-preset]'), 'change', event => { preset = event.target.value; draw(); }, cleanups);
    listen(host.querySelector('[data-reverse]'), 'click', () => { reverse = !reverse; draw(); }, cleanups);
    listen(host.querySelector('[data-reset]'), 'click', () => { preset = 'diagonal'; reverse = false; host.querySelector('[data-preset]').value = preset; draw(); }, cleanups);
    draw();
    return dispose(cleanups);
  };

  factories.neighborhoods = function (host) {
    const { frame, listen } = window.WikiVisuals;
    const cleanups = [], id = `neighborhoods-${++sequence}`;
    let size = 6;
    frame(host, {
      title: 'See all neighbors across three layers',
      subtitle: 'Offsets relative to center cell (0,0,0)',
      controls: `<label for="${id}-size">Neighborhood</label><select class="visual-select" id="${id}-size" data-size><option value="6">N6 · faces</option><option value="18">N18 · faces + edges</option><option value="26">N26 · faces + edges + corners</option></select>`,
      caption: 'Each panel is an XY slice of the same 3×3×3 neighborhood. Coordinates are offsets, not absolute world positions. The center cell is shown for orientation and is excluded from every neighborhood. Labels describe the contact type, not the array order.'
    });
    function draw() {
      const max = size === 6 ? 1 : size === 18 ? 2 : 3;
      const counts = [];
      const panes = [-1, 0, 1].map(z => {
        let count = 0;
        const body = grid({ minX: -1, minY: -1, z, label: `N${size}, offsets in layer Z=${z}`, cellAt: (x, y) => {
          const distance = Math.abs(x) + Math.abs(y) + Math.abs(z);
          if (distance === 0) return { kind: 'selected', label: 'O', title: 'center, excluded from neighborhood' };
          if (distance > max) return {};
          count++;
          return { kind: distance === 1 ? 'a' : distance === 2 ? 'b' : 'blocked', label: distance === 1 ? 'F' : distance === 2 ? 'E' : 'C', title: `${distance === 1 ? 'face' : distance === 2 ? 'edge' : 'corner'} neighbor` };
        } });
        counts.push(count);
        return panel(`Z = ${z} · ${count} neighbors`, body);
      });
      host.querySelector('[data-visual-body]').innerHTML = `<div class="visual-panes">${panes.join('')}</div><p class="visual-output" aria-live="polite" aria-atomic="true">N${size}: ${counts.join(' + ')} = ${size} neighbors. ${size === 6 ? '6 faces.' : size === 18 ? '6 faces + 12 edges.' : '6 faces + 12 edges + 8 corners.'} The center O is not counted.</p><p class="visual-legend">O = center; F = face contact; E = edge contact; C = corner contact; · = excluded. X increases rightward; Y increases downward in each displayed slice.</p>`;
    }
    listen(host.querySelector('[data-size]'), 'change', event => { size = Number(event.target.value); draw(); }, cleanups);
    draw();
    return dispose(cleanups);
  };

  factories['region-selection'] = function (host) {
    const { frame, listen } = window.WikiVisuals;
    const cleanups = [], id = `region-selection-${++sequence}`;
    let shape = 'sphere';
    frame(host, {
      title: 'See which cells a region selects',
      subtitle: 'Three XY slices · exact examples from this guide',
      controls: `<label for="${id}-shape">Region</label><select class="visual-select" id="${id}-shape" data-shape><option value="sphere">Sphere · radius 1</option><option value="cylinder">XZ cylinder · radius 1, Y=0..1</option><option value="integer">Integer box · (0,0,0)..(1,1,1)</option><option value="aabb">Continuous AABB · [0,1) on each axis</option></select>`,
      caption: 'Sphere and cylinder use center (0.5,0.5,0.5). Their tests use voxel centers. The integer box includes both cell-coordinate ends; the continuous AABB uses half-open geometric bounds. All selected cells for these presets fit in the three displayed layers.'
    });
    function draw() {
      let total = 0;
      const panes = [-1, 0, 1].map(z => {
        let count = 0;
        const body = grid({ minX: -1, minY: -1, z, label: `${shape}, XY slice Z=${z}`, cellAt: (x, y) => {
          const selected = shape === 'sphere' ? x * x + y * y + z * z <= 1 : shape === 'cylinder' ? x * x + z * z <= 1 && y >= 0 && y <= 1 : shape === 'integer' ? x >= 0 && y >= 0 && z >= 0 : x === 0 && y === 0 && z === 0;
          if (!selected) return {};
          count++; total++;
          return { kind: 'selected', label: '●', title: 'selected by region' };
        } });
        return panel(`Z = ${z} · ${count} cells`, body);
      });
      const detail = shape === 'sphere' ? 'The center cell and six face neighbors have centers inside or on the sphere.' : shape === 'cylinder' ? 'The five-cell XZ disk repeats for Y=0 and Y=1: 5 × 2 = 10 cells.' : shape === 'integer' ? 'Both coordinate ends are included: 2 × 2 × 2 = 8 cells.' : 'The geometric bounds cover exactly cell (0,0,0): 1 × 1 × 1 = 1 cell.';
      host.querySelector('[data-visual-body]').innerHTML = `<div class="visual-panes">${panes.join('')}</div><p class="visual-output" aria-live="polite" aria-atomic="true">${total} selected ${total === 1 ? 'cell' : 'cells'}. ${detail}</p><p class="visual-legend">● = selected; · = excluded. X increases rightward; Y increases downward. The cylinder extends along Y, not along the displayed Z layers.</p>`;
    }
    listen(host.querySelector('[data-shape]'), 'change', event => { shape = event.target.value; draw(); }, cleanups);
    draw();
    return dispose(cleanups);
  };
})();
