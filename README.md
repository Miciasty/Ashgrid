# Ashgrid

Low-level 3D voxel/grid toolkit for indexing, traversal, raster operations, and spatial queries.

> [!NOTE]
> Ashgrid focuses on grid and voxel primitives.
> - World transforms and frames: Ashspace
> - Pathfinding and graphs: Ashnav

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

Ashgrid is a pragmatic Java library for 3D integer grids and voxels. It provides:

- chunk and cell indexing,
- voxel traversal for rays and segments,
- raster algorithms (morphology, flood fill, connected components, distance transform),
- dense and sparse storage with useful views.

It stays intentionally low-level so higher-level systems can compose it without framework lock-in.

---

## Why use it

- Consistent primitives for common voxel tasks.
- Deterministic algorithms suitable for server-side simulation.
- Storage options for different memory/performance tradeoffs.
- Minimal dependencies and plain Java integration.

> [!WARNING]
> Ashgrid does not include mesh extraction (for example marching cubes).

---

## Features

- **Indexing and chunks**
  - `ChunkScheme`, `SquareXZChunkScheme`, `chunksInAABB`, `cellsInAABB`, neighborhood helpers.
- **Traversal and queries**
  - `VoxelTraverser` (`dda`), `VoxelTraversers.clipped(...)`, `Raycast`, `LineOfSight`.
- **Line rasterization**
  - `Line3D` (`bresenham3d`), `Line3DSupercover` (`supercover3d`).
- **Neighborhoods and regions**
  - `Neighborhood3D` (`N6`, `N18`, `N26`), `RegionIterators`, `AABBVoxelIterator`.
- **Raster operations**
  - `Morphology`, `MorphologyOps`, `FloodFill`, `ConnectedComponents`, `DistanceTransform`.
  - `GridSets.union/intersect/subtract/invert`.
- **Storage**
  - Dense: `ArrayGrid3i` (`BoundedGrid3i`).
  - Boolean: `BitGrid3`.
  - Sparse: `ChunkedGrid3i`, `HashSparseGrid3i` (`SparseGrid3i`).
  - Views: `SubGrid3i`, `ClampedGrid3i`, `MaskedGrid3i`, `ConstGrid3i`, `SliceView2D`.
- **Utilities**
  - `VoxelSpace`, `GridMath`, Ashcore `ServiceRegistry`.

> [!CAUTION]
> Core grids are not thread-safe.

---

## Installation

Maven:

```xml
<dependency>
  <groupId>dev.nasaka.blackframe</groupId>
  <artifactId>ashgrid</artifactId>
  <version>1.2.0</version>
</dependency>
```

Gradle (Kotlin):

```kts
implementation("dev.nasaka.blackframe:ashgrid:1.2.0")
```

Ashgrid depends on Ashcore (`dev.nasaka.blackframe:ashcore`).

---

## Quick start

### 1) Raycast first solid voxel

```java
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.query.Raycast;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

var traverser = ServiceRegistry.of(VoxelTraverser.class).require("dda");
var raycast = new Raycast(traverser);

var ray = new Ray(new Vector3(0.2, 1.6, 0.2), new Vector3(1, -1, 0).normalized());
var hit = raycast.first(ray, 100.0, (x, y, z) -> isSolid(x, y, z));

if (hit != null) {
    System.out.printf("Hit (%d,%d,%d) at t=%.3f%n", hit.x(), hit.y(), hit.z(), hit.tEnter());
}
```

### 2) Morphology + connected components

```java
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

var morph = ServiceRegistry.of(Morphology.class).require("MorphologyBasic");
var cc = ServiceRegistry.of(ConnectedComponents.class).require("ConnectedComponentsBFS");

var src = new ArrayGrid3i(64, 32, 64);
var dilated = new ArrayGrid3i(64, 32, 64);
var labels = new ArrayGrid3i(64, 32, 64);

morph.dilate(src, v -> v == 1, dilated, Morphology.Neighborhood.N6);
int lastLabel = cc.label(dilated, v -> v == 1, labels, ConnectedComponents.Neighborhood.N6);
System.out.println("Components: " + lastLabel);
```

### 3) Chunk range from world AABB

```java
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.implementation.grid.indexing.SquareXZChunkScheme;

var scheme = new SquareXZChunkScheme(16);
var box = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(40, 10, 40));

var range = scheme.chunksInAABB(box); // IntRect2, half-open: [cx0,cx1) x [cz0,cz1)
for (int cz = range.cz0(); cz < range.cz1(); cz++) {
    for (int cx = range.cx0(); cx < range.cx1(); cx++) {
        // process chunk (cx, cz)
    }
}
```

---

## Core concepts

- Cells are integer lattice points.
- Most spatial ranges are half-open (`[min,max)`).
- `VoxelSpace` maps world coordinates to cell coordinates with scale and origin.
- DDA traversal visits voxels with intervals `[tEnter, tExit)`.
- `LineOfSight.clear(a, b, ...)` excludes the start cell from occlusion checks.

---

## API highlights

- `VoxelTraverser#traverse(Ray, double, CellVisitor)`
- `VoxelTraversers#clipped(...)`
- `Raycast#first(...)`, `LineOfSight#clear(...)`
- `Line3D#trace(...)`, `Line3DSupercover#trace(...)`
- `ChunkScheme`, `SquareXZChunkScheme`
- `RegionIterators`, `AABBVoxelIterator`
- `Morphology`, `MorphologyOps`, `FloodFill`, `ConnectedComponents`, `DistanceTransform`
- `ArrayGrid3i`, `BitGrid3`, `ChunkedGrid3i`, `HashSparseGrid3i`
- Grid views: `SubGrid3i`, `ClampedGrid3i`, `MaskedGrid3i`, `ConstGrid3i`, `SliceView2D`

---

## Performance notes

- DDA is branch-light and supports unnormalized rays.
- `BitGrid3` reduces memory for boolean occupancy.
- `ChunkedGrid3i` is useful for large sparse worlds.
- Flood fill/components are queue-based BFS implementations.

---

## Project status

- Core traversal, line rasterization, and neighborhoods: done.
- Morphology, flood fill, connected components, distance transform: done.
- Dense/sparse storage and views: done.
- SPI discovery via `META-INF/services`: done.

> [!IMPORTANT]
> Validate service entries in packaged JAR:
>
> ```bash
> jar tf target/ashgrid-*.jar | grep META-INF/services
> ```

---

## Roadmap

- Integral volume (prefix sums) helpers for O(1) box queries.
- JMH benchmarks for key algorithms.
- Optional parallel chunk runners.

---

## Contributing

- Use Java 21+.
- Keep `api/*` focused and `implementation/*` concrete.
- Add GIVEN/WHEN/THEN tests for behavior changes.
- Run `mvn -DskipTests=false clean verify` before pushing.

---

## License

Apache-2.0 Copyright 2025 Mateusz Aftanas
