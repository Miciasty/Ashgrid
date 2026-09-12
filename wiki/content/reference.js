(() => {
  const {code, table, note} = window.WIKI_HTML;
  const types = names => names.split(' ').map(name => `<code>${name}</code>`).join(', ');
  const apiRows = [
    ['api.grid.bounds','IntBox3 IntRect2','coordinates','Half-open integer bounds and checked dimensions.'],
    ['api.grid.indexing','CellIndex3 ChunkIndex2 ChunkLocal3 ChunkScheme','coordinates','Cell indices, chunk coordinates, and coordinate conversion.'],
    ['api.grid.meta','GridInfo','coordinates','Cell size, origin, and coordinate metadata.'],
    ['api.voxel.space','VoxelSpace','coordinates','Conversion between supplied positions and axis-aligned cells.'],
    ['api.raster','Grid3i ReadableGrid3i WritableGrid3i BoundedGrid3i SparseGrid3i StoredGrid3i','storage','Read/write access, dimensions, sparse storage, and stored-cell iteration.'],
    ['api.raster.view','SubGrid3i ClampedGrid3i MaskedGrid3i ConstGrid3i SparseGridView3i BitGrid3iView','views','Live wrappers over bounded, sparse, or bit storage.'],
    ['api.raster2d','Grid2i','views','Two-dimensional integer grid access.'],
    ['api.raster2d.view','SliceView2D','views','Live X, Y, or Z slice of a three-dimensional grid.'],
    ['api.raster.ops','GridOps GridSets','grid-operations','Bulk fill, copy, snapshot, and binary set operations.'],
    ['api.raster.util','GridMath','coordinates','Checked dimensions, indexing, floor mapping, and cell centers.'],
    ['api.voxel.traversal','CellVisitor VoxelTraverser VoxelTraversers','traversal','DDA visits and clipping adapters.'],
    ['api.voxel.query','Raycast LineOfSight','raycasting','First occupied cell and segment visibility queries.'],
    ['api.voxel.draw','Line3D Line3DSupercover','lines-regions','Thin and full-contact line rasterization contracts.'],
    ['api.voxel.region','RegionIterator RegionIterators','lines-regions','Box, sphere, and cylinder cell iteration.'],
    ['api.voxel.neighborhood','Neighborhood3D','lines-regions','N6, N18, and N26 adjacency offsets.'],
    ['api.voxel.ops','VoxelTask','stepped-tasks','Task progress, cancellation, and work budgets.'],
    ['api.voxel.ops.floodfill','FloodFill','flood-fill','Visit a bounded connected region.'],
    ['api.voxel.ops.components','ConnectedComponents','components','Write labels for foreground components.'],
    ['api.voxel.ops.morphology','Morphology MorphologyOps','morphology-distance','Binary dilation, erosion, and composition.'],
    ['api.voxel.ops.distance','DistanceTransform','morphology-distance','Write a caller-owned floating-point distance map.']
  ];
  const concreteRows = [
    ['implementation.grid.indexing','SquareXZChunkScheme','coordinates','Square XZ chunks; Y is not partitioned.'],
    ['implementation.raster.arrays','ArrayGrid3i','storage','Bounded dense int array.'],
    ['implementation.raster.bitset','BitGrid3','storage','Bounded packed boolean storage.'],
    ['implementation.raster.sparse','HashSparseGrid3i','storage','Hash storage of non-default cells.'],
    ['implementation.raster.chunked','ChunkedGrid3i','storage','Dense chunks allocated on demand.'],
    ['implementation.voxel.traversal','DDA3DTraverser','traversal','Provider dda.'],
    ['implementation.voxel.draw','BresenhamLine3D SupercoverLine3D','lines-regions','Providers bresenham3d and supercover3d.'],
    ['implementation.voxel.region','AABBVoxelIterator','lines-regions','Direct box iterator; no SPI registration.'],
    ['implementation.voxel.ops.floodfill','FloodFillQueue','flood-fill','Flood fill with nested Task and reusable Workspace.'],
    ['implementation.voxel.ops.components','ConnectedComponentsBFS','components','Component labeling with nested Task and reusable Workspace.'],
    ['implementation.voxel.ops.morphology','MorphologyBasic','morphology-distance','Synchronous and stepped morphology.'],
    ['implementation.voxel.ops.distance','Chamfer345Distance','morphology-distance','Synchronous and stepped chamfer transform.']
  ];
  const apiTable = rows => table(['Package suffix','Types','Purpose'],rows.map(([pkg,names,page,description])=>[
    `<code>${pkg}</code>`,types(names),`<a href="#/${page}">${description}</a>`
  ]));
  window.WIKI_PAGES.push(
    {
      id: 'services', category: 'Reference', title: 'Service providers', kind: 'reference', readingTime: 4,
      description: 'Load an algorithm by its exact provider ID and preserve discovery when packaging.',
      sections: [
        {id: 'provider-ids', title: 'Built-in provider IDs', html: '<p>Ashgrid registers seven providers through Java service descriptors. Use <code>nsk.nu.ashcore.api.spi.ServiceRegistry</code> to select one by ID. IDs are case-sensitive.</p>'+table(['Interface','Provider ID','Implementation'],[
          ['VoxelTraverser','<code>dda</code>','DDA3DTraverser'],['Line3D','<code>bresenham3d</code>','BresenhamLine3D'],['Line3DSupercover','<code>supercover3d</code>','SupercoverLine3D'],['FloodFill','<code>floodfill-queue</code>','FloodFillQueue'],['ConnectedComponents','<code>ConnectedComponentsBFS</code>','ConnectedComponentsBFS'],['Morphology','<code>MorphologyBasic</code>','MorphologyBasic'],['DistanceTransform','<code>Chamfer345Distance</code>','Chamfer345Distance']
        ])+'<p><code>AABBVoxelIterator</code> and <code>RegionIterators</code> are direct APIs. They are not SPI providers.</p>'},
        {id: 'verify-discovery', title: 'Verify provider discovery', html: '<p>Run this program with Ashgrid, Ashcore, and the service descriptors on the classpath. Each lookup must succeed. It also checks every built-in provider ID without relying on discovery order.</p>'+code('java','ProviderExample.java',`import nsk.nu.ashcore.api.spi.Identified;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;
import nsk.nu.ashgrid.api.voxel.draw.Line3D;
import nsk.nu.ashgrid.api.voxel.draw.Line3DSupercover;
import nsk.nu.ashgrid.api.voxel.ops.floodfill.FloodFill;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.api.voxel.ops.distance.DistanceTransform;

public final class ProviderExample {
    private static <T extends Identified> void check(Class<T> type, String id) {
        var registry = ServiceRegistry.of(type, type.getClassLoader());
        if (!id.equals(registry.require(id).id())) throw new AssertionError(id);
    }

    public static void main(String[] args) {
        check(VoxelTraverser.class, "dda");
        check(Line3D.class, "bresenham3d");
        check(Line3DSupercover.class, "supercover3d");
        check(FloodFill.class, "floodfill-queue");
        check(ConnectedComponents.class, "ConnectedComponentsBFS");
        check(Morphology.class, "MorphologyBasic");
        check(DistanceTransform.class, "Chamfer345Distance");
        System.out.println("providers=7");
    }
}`)+code('output','Expected output','providers=7')},
        {id: 'registry-contract', title: 'Lookup and class loaders', html: '<div data-diagram="provider-flow"></div><p><code>ServiceRegistry.of(type)</code> uses the thread context class loader. When it is absent, it falls back to the service type’s loader, then the system loader. Use <code>of(type, loader)</code> when the caller must choose a loader explicitly.</p>'+table(['Operation','Result or failure'],[
          ['<code>require(id)</code>','Returns the provider; a missing ID throws IllegalStateException.'],['<code>get(id)</code>','Returns Optional.empty() when a valid ID is absent.'],['<code>contains(id)</code>','Tests membership for a valid ID.'],['<code>ids()</code> / <code>all()</code>','Immutable collection membership, unspecified enumeration order. Provider objects are not copied.'],['Null or blank lookup ID','IllegalArgumentException.'],['Duplicate provider IDs','Registry construction throws IllegalStateException.']
        ])+'<p>Each factory call eagerly loads a new registry. Loading may run provider constructors, static initialization, and <code>id()</code>. Those side effects are not rolled back if loading fails. ServiceLoader errors propagate.</p>'},
        {id: 'direct-providers', title: 'Use concrete providers for stepped work', html: '<p>The SPI interfaces expose synchronous operations. For stepped flood fill, components, morphology, or distance transforms, construct the documented concrete provider and call its <code>begin...</code> method. See <a href="#/stepped-tasks">stepped tasks</a>.</p><p>The public constructors and methods documented in the <a href="#/api-index">API index</a> are supported even when their existing package contains <code>implementation</code>. Private and package-private helpers remain internal.</p>'}
      ]
    },
    {
      id: 'api-index', category: 'Reference', title: 'API index', kind: 'reference', readingTime: 5,
      description: 'Find a public type by package and follow its usage guide.',
      sections: [
        {id: 'supported-api', title: 'Supported API', html: '<p>Every package below starts with <code>nsk.nu.ashgrid.</code>. Public types and members under <code>api</code> are supported. The documented concrete providers and backends also retain their public constructors and methods.</p><p>This index leads to usage and contract pages. For complete overload signatures, open the <code>ashgrid-1.3.0-javadoc.jar</code> artifact from <a href="https://central.sonatype.com/artifact/dev.nasaka.blackframe/ashgrid/1.3.0">Maven Central</a>, or generate Javadoc from source with <code>mvn -B verify</code>.</p>'},
        {id: 'interfaces-and-helpers', title: 'Interfaces, values, and helpers', html: apiTable(apiRows)},
        {id: 'concrete-types', title: 'Concrete storage and algorithms', html: apiTable(concreteRows)},
        {id: 'nested-types', title: 'Results, callbacks, and workspaces', html: '<p>Nested callback and result types belong to their enclosing API. <code>Raycast.Hit</code> carries the selected cell and entry parameter. <code>FloodFillQueue.Task</code> exposes its visited count; <code>ConnectedComponentsBFS.Task</code> exposes its component result. Each provider has a corresponding <code>Workspace</code> for reusable work buffers.</p><p>Read <a href="#/stepped-tasks">task lifecycle rules</a> before retaining a task across updates. A live task needs stable inputs and exclusive ownership of its outputs and workspace.</p>'}
      ]
    },
    {
      id: 'migration', category: 'Reference', title: 'Upgrading to 1.3.0', navTitle: 'Migration', kind: 'guide', readingTime: 4,
      description: 'Keep existing signatures while accounting for corrected boundaries and new storage operations.',
      sections: [
        {id: 'dependency', title: 'Update the dependency', html: '<p>Set the Ashgrid dependency to <code>1.3.0</code>. Maven resolves Ashcore <code>1.2.0</code> transitively. Remove dependency overrides that force an older Ashcore runtime.</p><p>The release retains existing public signatures, concrete providers, and SPI IDs. It adds checked <code>GridMath</code> helpers, bounded storage views, storage lifecycle methods, bulk operations, and stepped tasks. Synchronous calls remain available.</p>'},
        {id: 'behavior-changes', title: 'Review corrected behavior', html: table(['Area','What to account for'],[
          ['<a href="#/traversal">DDA traversal</a>','Negative-axis crossing times are corrected. NaN limits and invalid ray/index values are rejected. Exhausting int indices throws instead of wrapping. Tiny nonzero boundary components no longer produce NaN through 0 × infinity.'],
          ['<a href="#/raycasting">Raycast and visibility</a>','Raycast includes the start. Line of sight skips the first callback and excludes the exact endpoint. Clipping excludes empty bounds and parallel upper faces while retaining entry boundary visits.'],
          ['<a href="#/morphology-distance">Chamfer distance</a>','All 26 neighbor orientations use costs 3/4/5. With no foreground, results are positive infinity. Use Float.isInfinite instead of testing an old finite sentinel.'],
          ['<a href="#/lines-regions">Supercover lines</a>','All closed cells touched between endpoint cell centers are visited. Edge and corner crossings can produce more callbacks. Bresenham remains thin.'],
          ['<a href="#/coordinates">Bounds and mapping</a>','Nonpositive or overflowing dimensions fail before allocation. Bounds arithmetic is checked. Invalid subgrids and slices can throw earlier. Tiny negative mapped positions remain on the negative side.'],
          ['<a href="#/flood-fill">Raster operations</a>','Flood fill stays inside its declared volume, including clamped views. Components and morphology reject direct aliases and readable output shape mismatches. Separate views still require separate storage.'],
          ['<a href="#/views">Views and regions</a>','Views remain live. Empty fractional AABBs emit no cells; int-max loops terminate without wrapping. Treat public neighborhood arrays and their rows as read-only.']
        ])},
        {id: 'revalidate-consumers', title: 'Recheck stored results and boundary cases', html: '<p>Re-run consumer checks for saved cell sequences, face and corner contacts, negative coordinates, and empty masks. Exact DDA ties compare computed doubles without an epsilon. Rounding can separate crossings that were mathematically equal.</p><p>Repeatability requires fixed provider and dependency versions, inputs, neighborhood order, callback behavior, and environment. Output identity across versions and bitwise floating-point identity across platforms are not guaranteed.</p><p>The repository also maintains a <a href="https://github.com/Miciasty/Ashgrid/blob/master/docs/MIGRATION.md">Markdown migration guide</a>.</p>'}
      ]
    },
    {
      id: 'troubleshooting', category: 'Reference', title: 'Troubleshooting', kind: 'guide', readingTime: 5,
      description: 'Match a runtime symptom to its cause and the next check.',
      sections: [
        {id: 'missing-classes', title: 'The runtime cannot load Ashgrid or Ashcore', html: '<p>For <code>NoClassDefFoundError</code>, inspect the final application classpath or bundled plugin JAR. Both Ashgrid and Ashcore must be available at runtime. A Maven <code>provided</code> dependency is not packaged automatically.</p><p>For <code>UnsupportedClassVersionError</code>, run the application with Java 21 or newer. For <code>NoSuchMethodError</code>, check for an older library copy winning class loading. Read <a href="#/installation">installation</a> before changing dependency scope or relocation.</p>'},
        {id: 'missing-provider', title: 'No service registered for id: dda', html: '<p>Check the exact lowercase ID <code>dda</code>. Confirm that the final artifact retains <code>META-INF/services/nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser</code> and its provider class. When relocating, the descriptor and provider names must be relocated together.</p><p>Use the <a href="#/installation?section=package-with-plugin">Shade service transformer</a> when bundling. If a context class loader cannot see the provider, select the intended class loader explicitly. Run the <a href="#/services?section=verify-discovery">discovery example</a> on the packaged artifact.</p><p>A duplicate-ID error means two discovered providers claim the same ID. Inspect bundled service descriptors and dependencies; changing enumeration order does not resolve the collision.</p>'},
        {id: 'unexpected-cells', title: 'A query returns an unexpected cell', html: '<p>Check that the ray and callback use the same coordinate space. Coordinates use floor, so X=-0.2 belongs to cell -1. Maximum bounds and the exact ray endpoint are excluded.</p><p>At exact ties, DDA steps X, then Y, then Z. It can report zero-length visits; raycast tests them too. Line of sight skips only the first visit. Use <a href="#/traversal">traversal contracts</a> to distinguish this behavior from <a href="#/lines-regions">supercover</a>.</p><p>A voxel hit does not describe the collision shape inside the cell. Add shape-specific testing in your consumer when partial Minecraft blocks matter.</p>'},
        {id: 'invalid-volume', title: 'A volume or coordinate is rejected', html: '<p>Dense dimensions must be positive, and flattened volume products must fit <code>Integer.MAX_VALUE</code>. Arithmetic checks also reject bounds and coordinates that overflow their integer representation. A valid product can still exceed available heap.</p><p>Use a smaller bounded window or <a href="#/storage">sparse storage</a> for a large sparse domain. Wrap sparse or bit storage in the appropriate <a href="#/views">bounded view</a> before passing it to dense algorithms. BitGrid3iView accepts only 0 and 1 writes.</p>'},
        {id: 'changed-input', title: 'Results change while processing', html: '<p>Keep sources stable and outputs exclusively owned for the whole operation. A read-only wrapper is still a live view. Flood-fill callbacks may edit the current cell, but must not modify unvisited cells.</p><p>Use separate backing storage for component and morphology outputs. Two different wrappers can still alias the same data. For an independent copy, use <a href="#/grid-operations">GridOps.snapshot</a>.</p>'},
        {id: 'unfinished-task', title: 'A task has not finished after step()', html: '<p><code>step(budget)</code> consumes at most that many work units. A used budget does not mean the operation is done. Retain the task and resume it until <code>isDone()</code>. Require <code>status() == VoxelTask.Status.COMPLETED</code> before treating its output as final; failed and canceled tasks are also done.</p><p>Initialization, callback work, and allocation are not a wall-clock guarantee. Canceling does not undo writes already made. See <a href="#/stepped-tasks">stepped tasks</a> for workspaces and per-operation budget units.</p>'},
        {id: 'infinite-distance', title: 'A distance map contains infinity or multiples of three', html: '<p>With no foreground cell, chamfer output is positive infinity. A face step costs 3, an edge step 4, and a corner step 5. Divide finite values by 3 for an approximate distance in cells. The result is not exact Euclidean distance or a movement-clearance test.</p><p>Read <a href="#/morphology-distance">morphology and distance</a> before interpreting the output as physical distance.</p>'}
      ]
    }
  );
})();
