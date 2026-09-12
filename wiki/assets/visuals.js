/* Shared visual article components. Every figure uses locally authored data. */
(() => {
  'use strict';
  const escape = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  function frame(host, {title, subtitle = 'Interactive explanation', caption, controls = '', body = ''}) {
    host.innerHTML = `<figure class="wiki-visual diagram-component"><header class="visual-heading"><strong>${escape(title)}</strong><span>${escape(subtitle)}</span></header>${controls ? `<div class="visual-controls">${controls}</div>` : ''}<div class="visual-body" data-visual-body>${body}</div><figcaption class="visual-caption">${caption}</figcaption></figure>`;
  }
  function matrix({columns, rows, cells, label, xLabel = 'X', yLabel = 'Y', columnStart = 0, rowStart = 0}) {
    const size = 38;
    const left = 40, top = 39;
    const width = left + columns * size + 7, height = top + rows * size + 27;
    return `<svg class="visual-matrix" viewBox="0 0 ${width} ${height}" role="img" aria-label="${escape(label)}"><title>${escape(label)}</title><text x="${left + columns * size / 2}" y="13" class="visual-axis" text-anchor="middle">${escape(xLabel)} →</text><text x="10" y="${top + rows * size / 2}" class="visual-axis" transform="rotate(-90 10 ${top + rows * size / 2})" text-anchor="middle">${escape(yLabel)} →</text>${Array.from({length:columns},(_,x)=>`<text class="visual-axis" x="${left+(x+0.5)*size}" y="${top-7}" text-anchor="middle">${columnStart+x}</text>`).join('')}${Array.from({length:rows},(_,y)=>`<text class="visual-axis" x="${left-9}" y="${top+(y+0.5)*size+4}" text-anchor="end">${rowStart+y}</text>`).join('')}${cells.map((raw, i) => {
      const cell = raw ?? {};
      const x = left + (i % columns) * size, y = top + Math.floor(i / columns) * size;
      const kind = ['empty','selected','blocked','frontier','a','b'].includes(cell.kind) ? cell.kind : 'empty';
      return `<g data-cell="${i}" class="visual-cell visual-cell-${kind}"><title>${escape(cell.title ?? `(${columnStart + i % columns}, ${rowStart + Math.floor(i / columns)}): ${cell.label ?? ''}`)}</title><rect x="${x + 1}" y="${y + 1}" width="${size - 2}" height="${size - 2}" rx="3"/><text x="${x + size / 2}" y="${y + size / 2 + 4}" text-anchor="middle">${escape(cell.label)}</text></g>`;
    }).join('')}<text x="${width / 2}" y="${height - 7}" class="visual-axis" text-anchor="middle">Rows increase downward</text></svg>`;
  }
  function listen(element, event, handler, cleanups) {
    element.addEventListener(event, handler);
    cleanups.push(() => element.removeEventListener(event, handler));
  }
  window.WikiDiagramFactories = Object.create(null);
  window.WikiVisuals = Object.freeze({escape, frame, matrix, listen});
})();
