# Upgrading to Ashgrid 1.3.0

Version 1.3.0 retains existing public signatures, concrete providers and SPI IDs.
It adds checked `GridMath` helpers, bounded storage views, storage lifecycle operations,
bulk operations and stepped tasks. Existing synchronous operations remain available.
Ashcore 1.2.0 is resolved transitively by Maven; do not force an older version onto the runtime classpath.

Corrections can affect results and the inputs that are accepted:

| Area | What callers need to account for |
| --- | --- |
| DDA traversal | Negative-axis crossing times are corrected. Starting cells, zero-length visits and exact X/Y/Z tie order remain. NaN limits and invalid ray/index values are rejected; index exhaustion throws instead of wrapping. Tiny non-zero components on a boundary no longer produce NaN through `0 * infinity`. |
| Raycast and line of sight | Raycast includes the start; line of sight skips the first callback and excludes the exact segment endpoint. Line of sight uses the actual segment length. Clipping excludes parallel upper faces and empty bounds while retaining entry boundary visits. |
| Chamfer distance | All 26 neighbor orientations are represented, with face/edge/corner costs 3/4/5. No foreground now yields positive infinity instead of the old finite sentinel; use `Float.isInfinite`. Dividing by three approximates geometric distance. |
| Supercover lines | Every closed cell touched between endpoint cell centers is visited. Corner crossings can produce more callbacks, ordered by ascending X/Y/Z subset masks. Bresenham remains a thin rasterizer. |
| Storage and ranges | Nonpositive or overflowing dimensions fail before allocation or flattened iteration. Bounds and neighbor arithmetic are checked. Subgrids must fit their source and slice indices must be valid; invalid calls can throw earlier. |
| Coordinate mapping | `SquareXZChunkScheme` and `VoxelSpace` retain their packages, constructors and units. Invalid coordinates are rejected; tiny negative inputs remain on the negative side when division underflows. |
| Raster operations | Flood fill stays within its declared volume, including clamped views. Components and morphology reject direct aliases and readable output shape mismatches before writing. Distinct views sharing storage still require caller-managed separation. Morphology composition treats intermediate outputs as binary masks. |
| Regions and views | Empty fractional AABBs emit no cells and int-max loops terminate without wrapping. Shape iterators validate radii and bounds. Views remain live; public neighborhood arrays must be treated as read-only. |

Output identity across provider/dependency versions and bitwise floating-point identity across
platforms are not guaranteed. DDA ties use exact computed double equality, without an epsilon.
Revalidate saved cell sequences and boundary cases when upgrading.

See the [README](../README.md) for installation, examples, operation budgets and the supported API.
