# Ashgrid

Low-level deterministic Java library for 3D voxel/grid indexing, traversal, raster operations, and spatial queries.

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

## 6. Big-O for operations

Definitions:
- `k`: number of visited cells along a ray/line.
- `n`: number of cells in processed volume.
- `r`: number of cells reached by flood fill.

| Operation | Complexity | Notes |
| --- | --- | --- |
| `ArrayGrid3i.get/set` | `O(1)` | Direct array indexing. |
| `BitGrid3.get/set` | `O(1)` | Bit operations on packed storage. |
| `HashSparseGrid3i.get/set` | average `O(1)` | Hash-map based sparse access. |
| `ChunkedGrid3i.get/set` | average `O(1)` | Hash lookup for chunk + local index. |
| `VoxelTraverser.traverse` (`dda`) | `O(k)` | One visit per crossed voxel cell. |
| `Raycast.first` | `O(k)` worst case | Stops early on first solid cell. |
| `LineOfSight.clear` | `O(k)` | Traverses segment cells, excludes start cell. |
| `Line3D.trace` / `Line3DSupercover.trace` | `O(k)` | `k` depends on segment length and raster mode. |
| `FloodFill.fill` | `O(r)` | Queue-based BFS over reached region. |
| `ConnectedComponents.label` | `O(n)` | Full-volume scan + BFS expansion. |
| `MorphologyOps` (`dilate/erode/open/close`) | `O(n)` | Neighborhood size is constant (`6/18/26`). |
| `DistanceTransform` (chamfer) | `O(n)` | Linear passes over full volume. |
| `GridSets.union/intersect/subtract/invert` | `O(n)` | Element-wise full-volume set ops. |

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

```xml
<dependency>
  <groupId>dev.nasaka.blackframe</groupId>
  <artifactId>ashgrid</artifactId>
  <version>1.2.0</version>
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

## Contributing

- Use Java 21+.
- Keep `api/*` focused and `implementation/*` concrete.
- Add GIVEN/WHEN/THEN tests for behavior changes.
- Run `mvn -DskipTests=false clean verify` before pushing.

## License

Apache-2.0 Copyright 2025 Mateusz Aftanas
