# Ashgrid

Ashgrid stores, traverses and processes 3D integer grids in Java.
It provides cell/chunk indexing, dense and sparse storage, voxel queries and raster operations.

Version **1.3.0** is available from [Maven Central](https://central.sonatype.com/artifact/dev.nasaka.blackframe/ashgrid/1.3.0).

> [!NOTE]
> Ashgrid focuses on voxel/grid primitives.
>
> - World transforms and coordinate frames: Ashspace
> - Pathfinding and navigation graphs: Ashnav

## When to use it

Use Ashgrid when:

- you need deterministic voxel queries on a 3D integer grid,
- you need chunk/cell indexing with clear bounds rules,
- you need raster-style operations over voxel volumes.

Do not use Ashgrid when:

- you need mesh extraction or rendering pipelines,
- you need full coordinate-space graph/scene abstractions,
- you need high-level pathfinding/gameplay logic.

## Example: selecting a block

You want to detect which solid block a player is targeting:

1. Build a ray from player eye position and look direction.
2. Traverse voxels in order using DDA.
3. Stop at the first occupied cell.
4. Use the hit cell as the selected block.

You can then run region tools on affected blocks with the same grid primitives:

- flood fill for connected edits,
- morphology for thickening/thinning shapes,
- connected components for island detection.

## Requirements and quick start

Use JDK 21 or newer. Add this dependency to your Maven project:

```xml
<dependency>
  <groupId>dev.nasaka.blackframe</groupId>
  <artifactId>ashgrid</artifactId>
  <version>1.3.0</version>
</dependency>
```

Maven downloads Ashgrid and its transitive dependency, Ashcore 1.2.0, from Maven Central.
No additional repository configuration or local dependency installation is required.

To build Ashgrid from source, use Maven 3.9+ and run `mvn -B clean verify` in this checkout.

Minimal raycast example:

```java
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.query.Raycast;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

public final class AshgridQuickStart {
    public static void main(String[] args) {
        VoxelTraverser traverser = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        Raycast raycast = new Raycast(traverser);

        Ray ray = new Ray(new Vector3(0.2, 1.2, 0.2), new Vector3(1.0, 0.0, 0.0));
        Raycast.Hit hit = raycast.first(ray, 10.0, (x, y, z) -> x == 3 && y == 1 && z == 0);

        if (hit == null) {
            System.out.println("No voxel hit.");
        } else {
            System.out.printf("Hit (%d, %d, %d) at t=%.3f%n", hit.x(), hit.y(), hit.z(), hit.tEnter());
        }
    }
}
```

Bounded sparse editing with reusable work buffers:

```java
import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.raster.ops.GridOps;
import nsk.nu.ashgrid.api.raster.view.BitGrid3iView;
import nsk.nu.ashgrid.api.raster.view.SparseGridView3i;
import nsk.nu.ashgrid.implementation.raster.bitset.BitGrid3;
import nsk.nu.ashgrid.implementation.raster.chunked.ChunkedGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.floodfill.FloodFillQueue;

public final class AshgridStorageExample {
    public static void main(String[] args) {
        var storage = new ChunkedGrid3i(4,4,4);
        var window = new SparseGridView3i(storage,new IntBox3(-2,-1,-1,2,2,1));
        GridOps.fill(window,1);
        var snapshot = GridOps.snapshot(window);

        var workspace = new FloodFillQueue.Workspace();
        try (var task = new FloodFillQueue().begin(window,0,0,0,v -> v==1,
                (x,y,z,v) -> window.set(x,y,z,0),workspace)) {
            task.step(5);
            // Keep task to resume later. This standalone example finishes it here.
            while (!task.isDone()) task.step(5);
            if (task.count()!=24) throw new IllegalStateException("Unexpected fill count");
        }
        storage.pruneEmptyChunks();

        var bits = new BitGrid3iView(new BitGrid3(4,3,2));
        GridOps.copy(snapshot,bits);
        if (storage.chunkCount()!=0 || bits.get(0,0,0)!=1)
            throw new IllegalStateException("Snapshot/storage mismatch");
    }
}
```

## How it works

1. Positions are mapped to integer cells (`floor`-based indexing).
2. Traversal walks cell-by-cell with ray parameter intervals (`[tEnter, tExit)`).
3. Dense grids expose bounded integer access; small views connect bitset and sparse/chunked storage to that API.
4. Raster algorithms iterate cells using neighborhood definitions (`N6`, `N18`, `N26`).
5. Views (sparse window/bit/subgrid/clamped/masked/slice) let you reuse algorithms on limited regions.

### Coordinates and boundaries

`ChunkScheme`, `GridMath` and traversal use continuous **unit-grid coordinates**: cell `(0,0,0)`
occupies `[0,1)` on each axis, and `-0.2` maps to cell `-1`. Some legacy parameters are named `world`;
they still expect these grid units. `SquareXZChunkScheme(16)` groups 16 by 16 cells in XZ.
Its `chunkBounds` has Y range `[0,1)`, not the height of a world column.

`VoxelSpace(cellSize, origin)` keeps the existing conversion `floor((position-origin)/cellSize)`.
For example, cell size 2 and origin X=10 put X=9.8 in cell -1 and X=12 in cell 1.
Use Ashspace for rotated frames. To traverse scaled cells, convert the origin to grid coordinates
and divide world-distance limits by cell size; convert hit distances back by multiplying by cell size.
Keep the ray, occupancy callback and map in the same coordinate system.

`dda` starts at `floor(origin)` and reports nondecreasing `[tEnter,tExit)` intervals.
The normalized Ashcore ray makes `t` a distance in cells. Exact floating-point ties step X, then Y,
then Z. Intermediate visits may have zero length, including a start on a face pointing in the
negative direction. A zero direction component stays on the floor-selected side. `tMax=0` visits
nothing; the exact endpoint is excluded. Negative/NaN limits are rejected. With `+INF`, stop the
callback before the signed int index range is exhausted; stepping beyond it throws `ArithmeticException`.

`Raycast` includes the start and zero-length visits. `LineOfSight` excludes the first visit and the
segment endpoint, but includes later zero-length visits. These rules preserve DDA tie behavior;
they do not promise full contact coverage. `supercover3d` instead visits every closed voxel touched
by the segment between endpoint cell centers, including all edge/corner contacts. Simultaneous cells
use ascending subset masks X=1, Y=2, Z=4. `bresenham3d` draws a thin discrete line with both endpoints.

No universal epsilon changes cell membership. DDA accumulates double crossing times: nearly equal
crossings can differ by rounding, producing very short intervals and different contact order.
Large translations and extreme scales can erase fractional cell detail. Mapping rejects non-finite
values and out-of-range indices; it cannot recover precision already lost in the input.

### Distance maps and mutable inputs

`Chamfer345Distance` assigns foreground distance zero and the shortest 26-neighbor path cost to
background: face steps cost 3, edge diagonals 4, corner diagonals 5. Values are not divided by 3.
Dividing gives an approximation in cells, not exact Euclidean distance. No foreground yields
`Float.POSITIVE_INFINITY`; large costs round to float precision. This is not by itself a test of
whether a character fits or can move through a world.

All grids are mutable and are not thread-safe. Views retain live references: `ConstGrid3i` prevents
writes through the wrapper but does not freeze its source. Neighborhood arrays `N6/N18/N26`, including
their rows, must be treated as read-only; deep-copy them for custom offsets. Queries require stable
source data and repeatable callbacks. Flood-fill callbacks may edit the current cell, but must not
change unvisited cells. Morphology/component outputs must use separate storage, including through
views; direct aliases and readable output shape mismatches are rejected. Morphology produces 0/1,
and composition helpers apply the original predicate only to the original source.

For fixed provider/dependency versions, inputs, array order and callback behavior, results are
repeatable in one environment. Cross-version output identity and cross-platform bitwise floating-point
identity are not promised. BFS labels follow Z,Y,X scanning with X fastest; flood-fill neighbors are
ordered +X,-X,+Y,-Y,+Z,-Z. Region boxes/spheres scan Z,Y,X and cylinders Y,Z,X.

## Operation costs

### Storage, regions and work budgets

`SparseGridView3i(storage, bounds)` exposes a live local window: `(0,0,0)` maps to the bounds minimum.
Missing cells return the backend default and remain inside the window. Constructing the view allocates
no cells; writes can materialize chunks. Its origin/dimensions overload also supports a last cell at
`Integer.MAX_VALUE`. Positive dimensions and representable last-cell coordinates are required.
`BitGrid3iView(bits)` reads 0/1 and accepts only 0/1 writes; other values throw instead of discarding
integer information. Component labels need integer output storage. Boolean `BitGrid3.get/set` remain available. These views change representation
and indexing; frame conversion, navigation policies and engine adapters belong to their owning layers.

Both sparse backends implement the optional `StoredGrid3i` interface. `storedCellCount` and
`forEachStored` concern non-default values; `has` still means materialized storage. Hash iteration
orders cells by Z,Y,X. Chunked iteration orders chunk coordinates by Z,Y,X, then local cells by Z,Y,X.
These are deterministic but different orders. Callbacks must not modify the storage.
`HashSparseGrid3i.remove` removes a cell. Chunked `forEachChunk` includes empty materialized chunks;
`removeChunk(cx,cy,cz)` takes chunk coordinates, and `pruneEmptyChunks()` explicitly releases chunks
containing only defaults. `clear()` releases all entries/chunks. `chunkCount` and `allocatedCellCount`
report allocation counts; the latter includes default slots and padding at signed-int coordinate limits,
and does not estimate JVM heap bytes.

`GridOps.fill` and `copy` use contained, local, half-open `IntBox3` regions. Copy reads the whole region
before writing, preserving original values even for overlapping views; it needs `int[volume]` scratch,
which the caller may supply and reuse. Scratch must not back either grid or serve another active task.
Empty fill/copy regions are no-ops. `snapshot` creates an independent mutable dense grid with local
origin zero; empty snapshots are rejected. Use a sparse window to snapshot a selected sparse region.

`FloodFillQueue.begin`, `ConnectedComponentsBFS.begin`, `Chamfer345Distance.begin`,
`MorphologyBasic.beginDilate/beginErode` and `GridOps.beginFill/beginCopy` return a `VoxelTask`.
Existing synchronous methods run the same calculations to completion. Stepping is an additional
concrete-provider API; existing SPI interfaces and IDs remain unchanged.

Call `step(maxWork)` to perform at most that many work units. Zero does nothing; a negative budget
throws. Stop calling it to pause and call it again to resume. `cancel()`/`close()` are terminal and
leave partial output. Status distinguishes `RUNNING`, `COMPLETED`, `CANCELLED` and `FAILED`;
callback exceptions propagate and mark the task failed. `workDone()` counts successfully completed
units, while flood/component `count()` reports visits/components discovered so far. Reentrant stepping
or cancellation from a callback is rejected. No executor, tick scheduling or rollback is provided.

| Task | One work unit |
| --- | --- |
| Flood fill | Dequeue one candidate, including rejected candidates; examine at most six neighbors. |
| Components | Clear one output cell, scan one source position, or dequeue one cell with at most 26 neighbors. |
| Chamfer | Initialize or relax one cell; three passes, `3n` units. |
| Morphology | Produce one cell after at most 26 neighbor checks. |
| Fill / copy | One write / one read or write; fill takes `n`, copy takes `2n` units. |

Budgets bound cell work, not elapsed time, allocations or callback cost. Flood-fill and component
`Workspace` objects retain primitive queue capacity for reuse and are held by one active task at a
time. Completion, cancellation or failure releases the workspace. Flood-fill begin clears previously
used visited-bit words; clearing and buffer growth are outside the step budget. Chamfer reuses the
caller-owned float output; morphology needs no volume-sized scratch beyond output (composition
already accepts a temporary grid). Dispose of unused workspaces to let the GC reclaim retained buffers.
Sources, dimensions, predicates and neighborhoods must stay stable between steps; outputs belong
exclusively to the task. Flood-fill callbacks may still edit their current cell. Copy cancellation during
its reading phase leaves the destination untouched; cancellation during writing can leave a partial copy.

### Cost model

Definitions:

- `k`: number of visited cells along a ray/line.
- `n`: number of cells in processed volume.
- `r`: number of cells reached by flood fill.
- `s`: number of explicitly stored sparse cells; `c`: materialized chunks; `v`: cells per chunk.

| Operation | Complexity | Notes |
| --- | --- | --- |
| `ArrayGrid3i.get/set` | `O(1)` | Direct array indexing. |
| `BitGrid3.get/set` | `O(1)` | Bit operations on packed storage. |
| `HashSparseGrid3i.get/set` | average `O(1)` | Hash-map based sparse access. |
| `ChunkedGrid3i.get/set` | average `O(1)` | Hash lookup for chunk + local index. |
| `VoxelTraverser.traverse` (`dda`) | `O(k)` | Includes zero-length boundary visits; `O(1)` working memory. |
| `Raycast.first` | `O(k)` worst case | Stops early on first solid cell. |
| `LineOfSight.clear` | `O(k)` | Traverses segment cells, excludes start cell. |
| `Line3D.trace` / `Line3DSupercover.trace` | `O(k)` | `k` depends on segment length and raster mode. |
| `FloodFill.fill` | `O(n+r)` worst case | Visited bits use up to `O(n)` space; primitive candidate queue uses `O(r+1)` space. |
| `ConnectedComponents.label` | `O(n)` | Full-volume scan + BFS expansion. |
| `MorphologyOps` (`dilate/erode/open/close`) | `O(n)` | Neighborhood size is constant (`6/18/26`). |
| `DistanceTransform` (chamfer) | `O(n)` | Linear passes over full volume. |
| `GridSets.union/intersect/subtract/invert` | `O(n)` | Element-wise full-volume set ops. |
| `GridOps.fill/copy/snapshot` | `O(n)` | Fill needs `O(1)` scratch; copy `O(n)` scratch; snapshot `O(n)` independent output. |
| Hash `forEachStored` | `O(s log s)` | Sorted keys use `O(s)` temporary memory; stored count is `O(1)`. |
| Chunked `forEachStored` | `O(c log c + c*v)` | Sorted chunk keys use `O(c)` temporary memory. |
| Chunked `forEachChunk` | `O(c log c)` | Includes empty materialized chunks; `O(c)` temporary memory. |
| Chunked `storedCellCount/pruneEmptyChunks` | `O(c*v)` | Scans allocated slots. Allocation counts are `O(1)`. |

These costs assume constant-time grid access and callbacks. Dense int storage uses about `4n` bytes
plus array/object overhead; bit storage about `n/8` bytes. A sparse map uses `O(s)` entries with hash
and object overhead. Chunked storage uses `O(c*v)` cells: the first write into a chunk allocates and
initializes `O(v)` storage; later accesses have average `O(1)` lookup. Clearing its cells does not
automatically remove that chunk. Doubling all three dense dimensions multiplies memory by eight.

Connected components need `O(n)` output and up to `O(n)` queue memory. Chamfer needs a caller-owned
`O(n)` float output and constant extra working storage. Morphology uses `O(n)` output; composition
also needs an `O(n)` temporary grid, and N-step operations cost `O(N*n)`. Region scans cost the
candidate bounding volume, even when few cells pass the shape predicate. Views retain their sources;
clamped access also creates a small coordinate array. These are cost models, not latency benchmarks.

Dense/bit/chunk allocations, distance maps and flattened algorithms reject nonpositive dimensions
or products larger than `Integer.MAX_VALUE` before allocation. A valid int volume can still exceed
available heap. Bounds helpers throw on overflowing int results; no multi-gigabyte allocation is
required to test rejection.

## Feature map

- Indexing and chunks:
  - `ChunkScheme`, `SquareXZChunkScheme`, `chunksInAABB`, `cellsInAABB`.
- Traversal and queries:
  - `VoxelTraverser` (`dda`), `VoxelTraversers.clipped(...)`, `Raycast`, `LineOfSight`.
- Line rasterization:
  - `Line3D` (`bresenham3d`), `Line3DSupercover` (`supercover3d`).
- Neighborhoods and regions:
  - `Neighborhood3D` (`N6`, `N18`, `N26`), `RegionIterators`, `AABBVoxelIterator`.
- Raster operations:
  - `Morphology`, `MorphologyOps`, `FloodFill`, `ConnectedComponents`, `DistanceTransform`.
  - `GridSets.union/intersect/subtract/invert`.
  - `GridOps.fill/copy/snapshot`, `VoxelTask`.
  - Stepped `FloodFillQueue`, `ConnectedComponentsBFS`, `Chamfer345Distance`, `MorphologyBasic`;
    flood-fill/component nested `Task` and `Workspace` types.
- Storage and views:
  - `ArrayGrid3i`, `BitGrid3`, `ChunkedGrid3i`, `HashSparseGrid3i`.
  - `SubGrid3i`, `ClampedGrid3i`, `MaskedGrid3i`, `ConstGrid3i`, `SliceView2D`.
  - `SparseGridView3i`, `BitGrid3iView`, `StoredGrid3i`.
- Utilities:
  - `VoxelSpace`, `GridMath`, Ashcore `ServiceRegistry`.

> [!CAUTION]
> Core grids are not thread-safe.

## Supported API and SPI

Public types and members under `nsk.nu.ashgrid.api` are supported. Public constructors/methods of
the concrete classes in the feature map are also supported, including `SquareXZChunkScheme` at its
existing `implementation.grid.indexing` path and the `VoxelSpace` helper. Private/package-private
helpers are internal. Existing public signatures are retained; future moves/removals need a documented
migration. This minor release adds checked `GridMath` helpers, backend views,
storage lifecycle operations, bulk operations and stepped concrete-provider methods.
Bug corrections can change results: see the [migration guide](docs/MIGRATION.md).

Select providers by exact ID, not enumeration position. `ServiceRegistry` eagerly loads providers;
constructors and `id()` can execute code. Registry ordering comes from Ashcore and is unspecified.
Missing required IDs and duplicate IDs fail with `IllegalStateException`.

| Interface | Provider ID |
| --- | --- |
| `VoxelTraverser` | `dda` |
| `Line3D` | `bresenham3d` |
| `Line3DSupercover` | `supercover3d` |
| `FloodFill` | `floodfill-queue` |
| `ConnectedComponents` | `ConnectedComponentsBFS` |
| `Morphology` | `MorphologyBasic` |
| `DistanceTransform` | `Chamfer345Distance` |

`AABBVoxelIterator` and `RegionIterators` are direct APIs, not registered SPI providers.

## Glossary

- `voxel`: one cell in a 3D integer grid.
- `cell`: integer coordinate `(x, y, z)` in voxel space.
- `chunk`: grouped block of cells used for spatial partitioning.
- `half-open range`: interval like `[min, max)` where `min` is included and `max` is excluded.
- `occupancy`: rule telling whether a cell is solid/blocked.
- `tEnter/tExit`: ray parameter interval where the ray is inside a visited cell.
- `N6/N18/N26`: neighborhood connectivity sets for voxel adjacency.

## License

Apache License 2.0. See [LICENSE](LICENSE).
