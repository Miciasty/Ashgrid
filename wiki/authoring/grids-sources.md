# Grid articles: source record

Documented version: Ashgrid 1.3.0 (`pom.xml`), with Ashcore 1.2.0. Public prose follows the English WIKI template. The Polish language template remains the editorial basis; existing Java identifiers are preserved. This authoring record is not a public article.

## Page and source map

| Page | Confirmed contracts | Primary source files | Existing evidence |
| --- | --- | --- | --- |
| `coordinates` | Half-open bounds, inclusive helper, overflow behavior | `api/grid/bounds/IntBox3.java`, `IntRect2.java` | `api/grid/bounds/IntBoundsTest.java`; `implementation/raster/GridLimitsTest.java` |
| `coordinates` | Floor conversion, world scale/origin, finite inputs and output | `api/voxel/space/VoxelSpace.java`; `api/raster/util/GridMath.java` | `api/voxel/space/VoxelSpaceTest.java`; `api/raster/GridMathTest.java`; `GridLimitsTest.java` |
| `coordinates` | Unit-grid chunk inputs, exact-boundary ranges, negative coordinates, Y span, neighbor order | `api/grid/indexing/ChunkScheme.java`; `implementation/grid/indexing/SquareXZChunkScheme.java` | `integration/grid/ChunkSchemeAndRegionIntegrationTest.java`; `GridLimitsTest.java` |
| `coordinates` | GridInfo retains metadata; space/chunks required, extent nullable | `api/grid/meta/GridInfo.java`; `api/grid/indexing/CellIndex3.java`, `ChunkIndex2.java`, `ChunkLocal3.java` | `api/grid/meta/GridInfoTest.java`; direct constructor inspection for nullable extent |
| `storage` | Bounded interfaces; array and bit dimensions; sparse default/has distinctions | `api/raster/*.java`; `implementation/raster/arrays/ArrayGrid3i.java`; `bitset/BitGrid3.java`; `sparse/HashSparseGrid3i.java`; `chunked/ChunkedGrid3i.java` | `implementation/raster/BitGrid3Test.java`, `HashSparseGrid3iTest.java`, `ChunkedGrid3iTest.java`, `GridLimitsTest.java` |
| `storage` | Stored count vs allocation, cleanup, iteration order, callback ownership, complexity | `api/raster/StoredGrid3i.java`; `HashSparseGrid3i.java`; `ChunkedGrid3i.java` | `implementation/raster/StorageLifecycleTest.java` |
| `views` | Live references, sparse local origin, bit values, subregion validation, const/clamped/masked behavior, slices | `api/raster/view/*.java`; `api/raster2d/view/SliceView2D.java`; `api/raster2d/Grid2i.java` | `api/raster/view/BackendViewsTest.java`, `GridViewsTest.java`; `api/raster2d/SliceView2DTest.java`; `GridLimitsTest.java` |
| `grid-operations` | Fill/copy regions, alias buffering, task units, scratch, cancellation, snapshot independence and origin | `api/raster/ops/GridOps.java` | `api/raster/GridOpsTest.java` |
| `grid-operations` | Set algebra, 0/1 output, dimensions, synchronous scan and absence of overlap buffering | `api/raster/ops/GridSets.java` | `api/raster/GridSetsTest.java`; direct loop inspection for write-through overlap constraint |

Source paths above are relative to `src/main/java/nsk/nu/ashgrid` for production Java and `src/test/java/nsk/nu/ashgrid` for tests.

## Authoring decisions

- State that `GridInfo.extent` may be null without assigning a library-defined meaning to null; no such meaning is enforced by the record.
- Describe backend choice in terms of allocation structure, without claiming measured speed or a universal performance ranking.
- Distinguish `SquareXZChunkScheme`'s two-dimensional chunk mapping from `ChunkedGrid3i`'s independent three-dimensional storage chunks.
- Document sparse `has` as storage materialization, never as a world-boundary or occupancy check.
- Explain that a snapshot is independently mutable and does not capture metadata. A `ConstGrid3i` wrapper remains live.
- Recommend independent GridSets output for shifted overlapping views because its implementation writes while scanning; do not promise the buffering provided by GridOps.
- Every Java code block contains complete imports, one public class, a main method, and an adjacent exact expected-output block. Root-task validation compiles and executes these examples against the local build.

## Examples

`CellBoundsExample`, `VoxelSpaceExample`, `ChunkMappingExample`, `SparseDefaultsExample`, `SparseWindowExample`, `BitViewExample`, `LiveViewsExample`, `GridCopyExample`, `GridSnapshotExample`, and `GridSetsExample`.
