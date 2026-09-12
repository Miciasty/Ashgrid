/* Verified Ashgrid articles. Subject files append pages using the shared template components. */
window.WIKI_PAGES = [];
window.WIKI_HTML = (() => {
  const escape = value => String(value).replace(/[&<>"']/g, character => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[character]));
  return {
    code: (language, filename, source) => `<div class="code-block my-[21px] overflow-hidden rounded-[7px] border border-line bg-surface print:break-inside-avoid" data-language="${escape(language)}" data-filename="${escape(filename)}"><pre><code>${escape(source.trim())}</code></pre></div>`,
    table: (headers, rows) => `<div class="doc-table my-[22px] overflow-x-auto rounded-[6px] border border-line"><table class="w-full border-collapse text-left text-[12px]"><thead><tr>${headers.map(value => `<th scope="col">${value}</th>`).join('')}</tr></thead><tbody>${rows.map(row => `<tr>${row.map(value => `<td>${value}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`,
    note: (title, html, warning = false) => `<div class="callout ${warning ? 'warning' : 'note'} my-6 grid grid-cols-[18px_minmax(0,1fr)] gap-x-[11px] rounded-[6px] border border-line bg-surface px-[17px] py-4 print:break-inside-avoid"><div><strong>${escape(title)}</strong>${html}</div></div>`
  };
})();
