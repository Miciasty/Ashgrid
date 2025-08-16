# Ashgrid

3D voxel/grid toolkit for world partitioning, raster operations, and fast ray/line traversal. Batteries included, boilerplate excluded.

> [!NOTE]
> **Ashgrid** focuses on grid/voxel primitives and algorithms.  
> World transforms and frames: see **Ashspace**.  
> Pathfinding, graphs, heuristics: see **Ashnav**.

---

## Table of contents

- [What is it?](#what-is-it)
- [Why use it](#why-use-it)
- [Features](#features)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Core concepts](#core-concepts)
- [API highlights](#api-highlights)
- [Performance notes](#performance-notes)
- [Project status](#project-status)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## What is it?

**Ashgrid** is a small, pragmatic Java library for working with 3D grids and voxels. It gives you the essential building blocks for:

- partitioning large worlds into **chunks**,
- traversing voxels along **rays** and **lines**,
- running classic **morphology** and **flood/connected components** over masks,
- building sparse/dense **grid storage** with useful views and iterators.

It’s intentionally low-level and fast, so you can compose your own higher-level systems without wrestling frameworks.

> [!TIP]
> If you only need coordinate systems and transforms (scale/origin), use `VoxelSpace` from Ashgrid and keep everything else optional.

---

## Why use it

- **No “utils soup.”** Common patterns (AABB→cells, neighborhood offsets, chunk math) are provided and consistent.
- **Fast primitives.** DDA traversal, Bresenham/Supercover lines, BFS components, chamfer distance.
- **Storage that fits.** Dense arrays, bit grids, and chunked sparse storage. Views for subregions and masks.
- **Plain Java.** Minimal deps; integrates with existing engines and data.

> [!WARNING]
> Ashgrid does not do mesh extraction or marching cubes. That’s a different layer.

---

## Features

- **Indexing & Chunks**
    - `ChunkScheme` (e.g. `SquareXZChunkScheme`) maps world→chunk/cell; neighbors4/8; chunksInAABB; cellsInAABB.
- **Traversal**
    - `VoxelTraverser` DDA (Amanatides & Woo) + `VoxelTraversers.clipped(aabb)` helper.
- **Lines**
    - `Line3D` (Bresenham 3D), `Line3DSupercover` (covers all touched voxels).
- **Queries**
    - `Raycast` first-hit; `LineOfSight` clear/blocked segment check.
- **Neighborhoods**
    - `Neighborhood3D` with N6/N18/N26 offsets.
- **Regions**
    - `RegionIterators` for AABB, sphere, vertical cylinder; `AABBVoxelIterator`.
- **Raster ops**
    - `Morphology` (dilate/erode) + `MorphologyOps` (open/close, N-steps).
    - `FloodFill` (queue) and `ConnectedComponents` (BFS) with 6/18/26-connectivity.
    - `DistanceTransform` (chamfer 3–4–5).
    - Set algebra on masks: `GridSets.union/intersect/subtract/invert`.
- **Storage**
    - Dense: `ArrayGrid3i`, `ArrayGrid3f`; boolean: `BitGrid3`.
    - Chunked sparse: `ChunkedGrid3i`; map-based sparse: `HashSparseGrid3i`.
    - Views: `SubGrid3i`, `ClampedGrid3i`, `MaskedGrid3i`, `ConstGrid3i`; 2D slices via `SliceView2D`.
- **Utilities**
    - `VoxelSpace` (scale/origin, world↔cell).
    - `GridMath` (indexing helpers).
    - `GridServices` (SPI discovery by `id()`).

> [!CAUTION]
> Grids are **not** thread-safe. Partition work by chunks if you go parallel.

---

## Installation

Maven:

```xml
<dependency>
  <groupId>nsk.nu</groupId>
  <artifactId>Ashgrid</artifactId>
  <version>1.1</version>
</dependency>
```

Gradle (Kotlin):

```kts
implementation("nsk.nu:Ashgrid:1.1")
```

> [!NOTE]
> Ashgrid depends on **Ashcore** for basic math and SPI. Make sure your build pulls a compatible Ashcore version.

---

## Quick start

### 1) Traverse voxels along a ray and find the first solid cell

```java
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.util.GridServices;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;
import nsk.nu.ashgrid.api.voxel.query.Raycast;

// GIVEN: a density mask and a voxel traverser
var traverser = GridServices.require(VoxelTraverser.class, "dda");
var raycast = new Raycast(traverser);

// WHEN: cast a ray up to tMax
var ray = new Ray(new Vector3(0.2, 1.6, 0.2), new Vector3(1, -1, 0).normalized());
var hit = raycast.first(ray, 100.0, (x,y,z) -> isSolid(x,y,z));

// THEN: consume the hit
if (hit != null) {
    System.out.printf("Hit cell (%d,%d,%d) at t=%.3f%n", hit.x(), hit.y(), hit.z(), hit.tEnter());
}
```

### 2) Dilate a mask, then run connected components

```java
import nsk.nu.ashgrid.api.voxel.ops.morphology.*;
import nsk.nu.ashgrid.api.voxel.ops.components.*;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

var morph = GridServices.require(Morphology.class, "MorphologyBasic");
var cc    = GridServices.require(ConnectedComponents.class, "ConnectedComponentsBFS");

var src = new ArrayGrid3i(64, 32, 64);
var tmp = new ArrayGrid3i(64, 32, 64);
var dst = new ArrayGrid3i(64, 32, 64);

// dilate once with 6-neighborhood
morph.dilate(src, v -> v == 1, dst, Morphology.Neighborhood.N6);

// label components into dst (in place or copy as needed)
int lastLabel = cc.label(dst, v -> v == 1, dst, ConnectedComponents.Neighborhood.N6);
System.out.println("Components: " + lastLabel);
```

### 3) Chunk math: locate chunk, iterate touching chunks

```java
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.implementation.grid.indexing.SquareXZChunkScheme;

var scheme = new SquareXZChunkScheme(16);

var chunk = scheme.chunkOfPoint(new Vector3(17, 0, 33));  // -> (1,2)
for (var c : scheme.neighbors4(chunk)) {
    // process neighbor chunk c
}

var box = new AxisAlignedBox(new Vector3(0,0,0), new Vector3(40,10,40));
for (var c : scheme.chunksInAABB(box)) {
    // load/generate chunk c
}
```

---

## Core concepts

- **Cells and indices.** Cells are integer lattice points. Conventions use half-open ranges `[min,max)`.
- **World vs voxel space.** `VoxelSpace` defines scale and origin for mapping world coordinates to cells.
- **Neighborhoods.** Use `Neighborhood3D` N6/N18/N26 consistently across ops.
- **Traversal.** DDA visits voxels in order of intersection intervals `[tEnter,tExit)`.

> [!NOTE]
> Rays do not need to be normalized. DDA takes care of parametric stepping; normalize only if your own math assumes it.

---

## API highlights

- `VoxelTraverser#traverse(Ray ray, double tMax, CellVisitor v)`
- `Line3D#trace(...)`, `Line3DSupercover#trace(...)`
- `Raycast#first(...)`, `LineOfSight#clear(...)`
- `ChunkScheme` and `SquareXZChunkScheme`
- `RegionIterators` (AABB/sphere/cylinder)
- `Morphology`, `MorphologyOps`
- `FloodFill`, `ConnectedComponents`
- `DistanceTransform` (Chamfer 3–4–5)
- `ArrayGrid3i/3f`, `BitGrid3`, `ChunkedGrid3i`, `HashSparseGrid3i`
- Views: `SubGrid3i`, `ClampedGrid3i`, `MaskedGrid3i`, `ConstGrid3i`, `SliceView2D`
- Utilities: `VoxelSpace`, `GridMath`, `GridSets`, `GridServices`, `VoxelTraversers`

---

## Performance notes

- DDA is branch-light and works with unnormalized rays; use `VoxelTraversers.clipped(aabb)` to avoid stepping before entry.
- Prefer `BitGrid3` for boolean masks (8× less memory than bytes).
- For large scenes, store data in `ChunkedGrid3i` and operate per-chunk.
- BFS in components/fill can use array-based queues if profiling says so.

---

## Project status

- ✅ Core traversal, lines, neighborhoods
- ✅ Morphology, flood fill, connected components
- ✅ Chamfer distance transform
- ✅ Dense/bit/chunked/sparse storage and views
- ✅ SPI loading via `META-INF/services` and `GridServices`

> [!IMPORTANT]
> Verify your JAR contains the `META-INF/services/*` entries. A quick check:
>
> ```bash
> jar tf target/Ashgrid-*.jar | grep META-INF/services
> ```

---

## Roadmap

- Prefix sums (integral volume) for O(1) box queries
- Benchmarks (JMH) for traversal, lines, morphology
- Optional parallel chunk runners

---

## Contributing

- Use Java 21+.
- Keep `api/*` lean, `implementation/*` final and focused.
- Add tests (GIVEN–WHEN–THEN) and ASCII sketches when it clarifies intent.
- Run `mvn -DskipTests=false clean verify` before pushing.

---

## License

Apache-2.0 (or project’s chosen license). Include a `LICENSE` file at repository root.
