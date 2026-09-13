/* First-result illustration for the runnable quick start. */
(() => {
  'use strict';
  const V = window.WikiVisuals;
  const factories = window.WikiDiagramFactories;
  let sequence = 0;
  factories['first-hit'] = host => {
    const id=`first-hit-${++sequence}`, cleanup=[];
    V.frame(host,{title:'Watch the quick-start ray reach the occupied cell',subtitle:'The same grid as AshgridQuickStart.java',controls:`<label for="${id}">Distance limit tMax<input type="range" id="${id}" min="0" max="7.5" step="0.5" value="7.5"></label><button class="visual-button" type="button" data-reset>Reset</button>`,caption:'The ray starts at (0.5, 1.5, 0.5) and points along +X. This view shows only the row Y=1, Z=0. Cell 3 has value 1. The exact endpoint is excluded, so tMax must exceed 2.5 to visit cell 3.'});
    const input=host.querySelector('input'), body=host.querySelector('[data-visual-body]');
    function draw() {
      const max=Number(input.value), hit=max>2.5;
      const traversed=Array.from({length:8},(_,x)=>max>0 && Math.max(0,x-0.5)<max && x<=3);
      input.setAttribute('aria-valuetext',`${max} grid units`);
      body.innerHTML=V.matrix({columns:8,rows:1,label:`Quick-start row Y=1 Z=0. Distance limit ${max}. ${hit?'Hit cell 3.':'No hit.'}`,xLabel:'X',yLabel:'Y',rowStart:1,cells:Array.from({length:8},(_,x)=>({label:x===3?(hit?'HIT':'1'):(traversed[x]?'→':'0'),kind:x===3?(hit?'selected':'blocked'):traversed[x]?'a':'empty',title:`Cell (${x},1,0), value ${x===3?1:0}${traversed[x]?', visited':''}`}))})+`<ul class="visual-legend"><li>0 = empty cell</li><li>1 = occupied cell</li><li>→ = visited empty cell</li><li>HIT = selected cell</li></ul><output class="visual-output" aria-live="polite">tMax = ${max} · ${hit?'cell = (3, 1, 0), tEnter = 2.5':'result = null'}</output><p>${hit?'The query stops at cell 3; it does not inspect later cells.':max===0?'An empty distance interval produces no callbacks.':'The query has not entered the occupied cell before the distance limit.'}</p>`;
    }
    V.listen(input,'input',draw,cleanup);
    V.listen(host.querySelector('[data-reset]'),'click',()=>{input.value='7.5';draw();},cleanup);
    draw(); return ()=>cleanup.forEach(fn=>fn());
  };

})();
