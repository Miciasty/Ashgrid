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

## Interactive grid figures

The teaching models in `wiki/assets/diagrams-grids.js` implement bounded JavaScript examples of the source contracts. They do not execute Ashgrid in the browser, benchmark backends, or estimate heap bytes. Every figure labels its XZ slice and Y interpretation. Native labelled controls expose all interactions without requiring pointer access to SVG cells.

| Figure | Source checked | Defaults and verification expectations |
| --- | --- | --- |
| `chunk-map` | `SquareXZChunkScheme.chunkOfPoint`; floor division/modulus contract in `ChunkedGrid3i` and existing chunk tests | Cell X = −1, Z = 1, size = 4 gives chunk (−1, 0), local (3, 1). Changing size to 2 gives chunk (−1, 0), local (1, 1). Cell (0, 0) maps to local (0, 0). Visible cell addresses are local X,Z; outer axis labels are signed cell coordinates. |
| `storage-backends` | `ArrayGrid3i` constructor; `BitGrid3` constructor/indexing; `HashSparseGrid3i.set`; `ChunkedGrid3i.set`, `has`, `allocatedCellCount`, `storedCellCount`, `pruneEmptyChunks` | Fresh 8 × 1 × 4 grids have 32 dense integer slots / 32 logical booleans. Four clustered cells create 4 hash entries and one 2 × 1 × 2 chunk (4 slots). Four scattered cells create 4 entries and 4 chunks (16 slots). Writing 0 removes hash entries but preserves allocated chunks. Pruning after clearing removes all those chunks. Full pattern has 32 foreground cells, 8 chunks, 32 slots. |
| `grid-views` | `SparseGridView3i.get`/`set`; `GridOps.snapshot`; `BackendViewsTest`; `GridOpsTest` | Window origin (−1, 60, 0), shape 3 × 1 × 2. Initial values [1,2,3,4,5,6] are copied into an independent snapshot. Selected local (1,0,0) maps to source (0,60,0); both and snapshot initially equal 2. Writing default selected value 9 to source or window changes source and window to 9, snapshot remains 2. Writing snapshot changes only the copy; capture copies the current six window values. The source panel is a displayed finite region of an unbounded sparse backend. |
| `set-operations` | `GridSets.union`, `intersect`, `subtract`, `invert`; `GridSetsTest` | Shape 6 × 1 × 4. A has six positive values (7), B six positive values (9), and their overlap contains two cells. Initial union has 10 ones, intersection 2, A minus B 4, invert A 18. Output is binary for predicate value > 0. Selected edit cell (3,0,1) begins in both sets. B edits are disabled during inversion because that operation has only input A. |

The figures are inserted before their sections' longer code/table reference material. Source loops and contracts were inspected directly; root-task browser validation verifies rendering, control effects, and cleanup through the shared diagram loader.
