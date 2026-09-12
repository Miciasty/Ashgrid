/* Data flow and first-result illustrations for Ashgrid integration. */
(() => {
  'use strict';
  const V = window.WikiVisuals;
  const factories = window.WikiDiagramFactories;
  let sequence = 0;
  const node = (step, title, text, active = false) => `<div class="visual-flow-node" data-active="${active}"><span class="visual-step-number">${step}</span><strong>${V.escape(title)}</strong><p>${text}</p></div>`;

  factories['library-map'] = host => {
    const id = `library-map-${++sequence}`, cleanup = [];
    const cases = {
      target: {input:'Eye position + look direction', data:'Your occupancy callback', operation:'Raycast.first', result:'Cell (3, 1, 0), tEnter = 2.5', use:'Your plugin selects a block or performs a detailed shape test.'},
      fill: {input:'Seed cell + matching rule', data:'A bounded integer grid', operation:'FloodFill.fill', result:'Callbacks for connected matching cells', use:'Your callback edits data; your plugin decides when to apply world changes.'},
      islands: {input:'Foreground rule + neighborhood', data:'A source grid and a separate label grid', operation:'ConnectedComponents.label', result:'Region labels and a component count', use:'Your plugin associates each label with the region it represents.'}
    };
    V.frame(host,{title:'Follow data through Ashgrid',subtitle:'From plugin input to a usable result',controls:`<label for="${id}">Example task<select class="visual-select" id="${id}"><option value="target">Select a block</option><option value="fill">Edit a connected region</option><option value="islands">Find separate islands</option></select></label>`,caption:'The arrows describe who supplies and processes the data. Ashgrid computes grid results. Your plugin owns world access, scheduling, and gameplay decisions.'});
    const select = host.querySelector('select'), body = host.querySelector('[data-visual-body]');
    function draw() {
      const current=cases[select.value];
      body.innerHTML = `<div class="visual-flow">${node('1 · YOUR PLUGIN →','Supply inputs',V.escape(current.input))}${node('2 · DATA →','Choose a representation',V.escape(current.data))}${node('3 · ASHGRID →',current.operation,V.escape(current.result),true)}${node('4 · YOUR PLUGIN','Use the result',current.use)}</div><output class="visual-output" aria-live="polite">${V.escape(current.input)} → ${V.escape(current.operation)} → ${V.escape(current.result)}</output>`;
    }
    V.listen(select,'change',draw,cleanup); draw();
    return ()=>cleanup.forEach(fn=>fn());
  };

  factories['dependency-map'] = host => {
    const id=`dependency-map-${++sequence}`, cleanup=[];
    V.frame(host,{title:'See which libraries your project needs',subtitle:'Direct and transitive dependencies',controls:`<label for="${id}">View<select class="visual-select" id="${id}"><option value="compile">Compile-time dependency graph</option><option value="runtime">Private plugin bundle</option></select></label>`,caption:'Maven resolves Ashcore transitively through Ashgrid. A private shaded plugin is one runtime packaging option; an ordinary Java application can put both libraries on its classpath.'});
    const select=host.querySelector('select'), body=host.querySelector('[data-visual-body]');
    function draw() {
      if(select.value==='compile') body.innerHTML=`<div class="visual-flow">${node('DECLARE →','Your project','Add <code>dev.nasaka.blackframe:ashgrid:1.3.0</code>.')}${node('DIRECT DEPENDENCY →','Ashgrid 1.3.0','Storage, indexing, voxel queries, and raster operations.',true)}${node('TRANSITIVE DEPENDENCY','Ashcore 1.2.0','Math values, rays, and service discovery.')}</div><output class="visual-output" aria-live="polite">Your project → Ashgrid 1.3.0 → Ashcore 1.2.0</output>`;
      else body.innerHTML=`<div class="visual-panel"><h3>Your plugin JAR · private bundled copy</h3><div class="visual-flow">${node('APPLICATION','Your plugin classes','Minecraft integration, world reads, and decisions.')}${node('LIBRARY','Ashgrid + Ashcore','Include both libraries. If relocating, relocate their packages consistently.',true)}${node('DISCOVERY','META-INF/services','Merge service descriptors and relocate provider names with Maven Shade.')}</div></div><output class="visual-output" aria-live="polite">Runtime needs library classes + service descriptors. See the Shade configuration below.</output>`;
    }
    V.listen(select,'change',draw,cleanup); draw();
    return ()=>cleanup.forEach(fn=>fn());
  };

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

  factories['provider-flow'] = host => {
    const id=`provider-flow-${++sequence}`, cleanup=[];
    const choices={
      dda:['VoxelTraverser','voxel.traversal','dda','DDA3DTraverser'],
      fill:['FloodFill','voxel.ops.floodfill','floodfill-queue','FloodFillQueue'],
      distance:['DistanceTransform','voxel.ops.distance','Chamfer345Distance','Chamfer345Distance'],
      missing:['VoxelTraverser','voxel.traversal','missing','No provider']
    };
    V.frame(host,{title:'Follow a service lookup',subtitle:'Exact IDs select a provider',controls:`<label for="${id}">Lookup<select class="visual-select" id="${id}"><option value="dda">VoxelTraverser: dda</option><option value="fill">FloodFill: floodfill-queue</option><option value="distance">DistanceTransform: Chamfer345Distance</option><option value="missing">VoxelTraverser: missing ID</option></select></label>`,caption:'ServiceRegistry loads providers eagerly from service descriptors, then indexes them by ID. This diagram shows the lookup stages; it does not run Java or promise discovery order.'});
    const select=host.querySelector('select'),body=host.querySelector('[data-visual-body]');
    function draw(){
      const [type,pkg,key,implementation]=choices[select.value],missing=select.value==='missing';
      body.innerHTML=`<div class="visual-flow">${node('1 · TYPE →',type,'Choose the service interface.')}${node('2 · DISCOVERY →','Service descriptor','The class loader finds and constructs registered providers.')}${node('3 · LOOKUP →',`require("${key}")`,'Match the exact, case-sensitive provider ID.',true)}${node('4 · RESULT',missing?'IllegalStateException':implementation,missing?'No provider has the requested ID.':'Return the provider instance.')}</div><div class="visual-output">META-INF/services/nsk.nu.ashgrid.api.${pkg}.${type}</div><output class="visual-output" aria-live="polite">${missing?'No service registered for id: missing':`${key} → ${implementation}`}</output>`;
    }
    V.listen(select,'change',draw,cleanup);draw();return()=>cleanup.forEach(fn=>fn());
  };
})();
