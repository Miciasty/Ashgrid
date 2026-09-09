# Ashgrid

Low-level deterministic Java library for 3D voxel/grid indexing, traversal, raster operations, and spatial queries.

This checkout is **1.3.0-SNAPSHOT**, an unpublished development version. Release evidence and
compatibility changes are recorded in [docs/RELEASE.md](docs/RELEASE.md).

> [!NOTE]
> Ashgrid focuses on voxel/grid primitives.
> - World transforms and coordinate frames: Ashspace
> - Pathfinding and navigation graphs: Ashnav

## 1. Purpose

Ashgrid gives you reusable building blocks to store, traverse, and process 3D integer voxel worlds.

## 2. Problem

Voxel plugins and engines often re-implement the same low-level logic:
- world position to cell/chunk conversion,
- ray or line traversal through voxels,
- dense/sparse voxel storage,
- flood fill, morphology, connected components, distance maps.

Ashgrid centralizes these primitives with deterministic contracts so you can build higher-level systems without rewriting them.

## 3. When to use

Use Ashgrid when:
- you need deterministic voxel queries on a 3D integer grid,
- you need chunk/cell indexing with clear bounds rules,
- you need raster-style operations over voxel volumes.

Do not use Ashgrid when:
- you need mesh extraction or rendering pipelines,
- you need full coordinate-space graph/scene abstractions,
- you need high-level pathfinding/gameplay logic.

## 4. Simple example (Minecraft plugin example)

You want to detect which solid block a player is targeting:
1. Build a ray from player eye position and look direction.
2. Traverse voxels in order using DDA.
3. Stop at the first occupied cell.
4. Use the hit cell as the selected block.

You can then run region tools on affected blocks with the same grid primitives:
- flood fill for connected edits,
- morphology for thickening/thinning shapes,
- connected components for island detection.

## 5. How it works

1. Positions are mapped to integer cells (`floor`-based indexing).
2. Traversal walks cell-by-cell with ray parameter intervals (`[tEnter, tExit)`).
3. Storage backends expose a common grid API (dense, bitset, sparse, chunked).
4. Raster algorithms iterate cells using neighborhood definitions (`N6`, `N18`, `N26`).
5. Views (subgrid/clamped/masked/slice) let you reuse algorithms on limited regions.

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

## 6. Big-O for operations

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
| `FloodFill.fill` | `O(n+r)` | Allocates/initializes `O(n)` visited flags; queue uses `O(r)` space. |
| `ConnectedComponents.label` | `O(n)` | Full-volume scan + BFS expansion. |
| `MorphologyOps` (`dilate/erode/open/close`) | `O(n)` | Neighborhood size is constant (`6/18/26`). |
| `DistanceTransform` (chamfer) | `O(n)` | Linear passes over full volume. |
| `GridSets.union/intersect/subtract/invert` | `O(n)` | Element-wise full-volume set ops. |

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

## 7. Core terms

- `voxel`: one cell in a 3D integer grid.
- `cell`: integer coordinate `(x, y, z)` in voxel space.
- `chunk`: grouped block of cells used for spatial partitioning.
- `half-open range`: interval like `[min, max)` where `min` is included and `max` is excluded.
- `occupancy`: rule telling whether a cell is solid/blocked.
- `tEnter/tExit`: ray parameter interval where the ray is inside a visited cell.
- `N6/N18/N26`: neighborhood connectivity sets for voxel adjacency.

## 8. Quick-start

Maven:

Requires JDK 21+ and Maven. The coordinates below describe this checkout; build and install the
snapshot locally with `mvn -B clean install` before using it in another local project.

```xml
<dependency>
  <groupId>dev.nasaka.blackframe</groupId>
  <artifactId>ashgrid</artifactId>
  <version>1.3.0-SNAPSHOT</version>
</dependency>
```

Ashgrid depends on:

```xml
<dependency>
  <groupId>dev.nasaka.blackframe</groupId>
  <artifactId>ashcore</artifactId>
  <version>1.0.1</version>
</dependency>
```

Ashcore 1.0.1 remains the default dependency for compatibility. Integration with the corrected
Ashcore 1.1.0-SNAPSHOT is verified separately using `-Dashcore.version=1.1.0-SNAPSHOT` and an isolated
artifact repository; editing a neighboring checkout does not replace Maven's resolved JAR.

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
- Storage and views:
  - `ArrayGrid3i`, `BitGrid3`, `ChunkedGrid3i`, `HashSparseGrid3i`.
  - `SubGrid3i`, `ClampedGrid3i`, `MaskedGrid3i`, `ConstGrid3i`, `SliceView2D`.
- Utilities:
  - `VoxelSpace`, `GridMath`, Ashcore `ServiceRegistry`.

> [!CAUTION]
> Core grids are not thread-safe.

## Supported API and SPI

Public types and members under `nsk.nu.ashgrid.api` are supported. Public constructors/methods of
the concrete classes in the feature map are also supported, including `SquareXZChunkScheme` at its
existing `implementation.grid.indexing` path and the `VoxelSpace` helper. Private/package-private
helpers are internal. Existing public signatures are retained; future moves/removals need a documented
migration. This minor development version adds checked `GridMath.cellCount` and `floorToInt` helpers.
Bug corrections can change results: see the migration notes in [docs/RELEASE.md](docs/RELEASE.md).

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

`AABBVoxelIterator` and `RegionIterators` are direct APIs, not registered SPI providers. Packaged-artifact
tests verify every ID above, duplicate/missing handling, and compilation/execution of this quick start
against the main JAR and its Ashcore dependency.

## Contributing

- Use Java 21+.
- Keep `api/*` focused and `implementation/*` concrete.
- Add GIVEN/WHEN/THEN tests for behavior changes.
- Run `mvn -B clean verify` before pushing. This runs unit tests, Javadoc and packaged-artifact tests.
- CI covers every branch and pull request on Temurin 21/25. Publishing is a separate release workflow;
  publication routes, evidence and remaining release checks are in [docs/RELEASE.md](docs/RELEASE.md).

## License

Apache-2.0 Copyright 2025 Mateusz Aftanas
