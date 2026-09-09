package nsk.nu.ashgrid.api.voxel.traversal;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.spi.Identified;

/**
 * Visits unit voxels from a ray in continuous grid coordinates.
 * Repeatable for fixed inputs, callback behavior, provider and dependency versions in one environment.
 * Provider enumeration order and cross-version or cross-platform bitwise identity are not guaranteed.
 */
public interface VoxelTraverser extends Identified {

    /**
     * Traverse voxels intersected by {@code ray} from {@code t=0} up to {@code tMax},
     * invoking {@code visitor} for each visited voxel in order.
     *
     * <p>The implementation should call the visitor with a half-open interval
     * {@code [tEnter, tExit)} for the voxel. If {@code visitor.visit(...)} returns {@code false},
     * the traversal must stop immediately. Intervals satisfy 0 &lt;= tEnter &lt;= tExit &lt;= tMax
     * in nondecreasing parameter order. Zero-length boundary visits are permitted; their order is
     * provider-specific. With {@code dda}, the start cell is floor(origin), ties step X then Y then Z,
     * and negative directions from a face emit a zero-length starting visit. A zero direction
     * component stays on the floor-selected side of that face. The exact endpoint is excluded;
     * tMax=0 emits no callbacks. No membership epsilon is applied.</p>
     *
     * @param ray finite ray in unit-grid coordinates, with a non-zero normalized direction from Ashcore;
     *             t measures distance in cells, and floor(origin) must fit signed int
     * @param tMax maximum distance (non-negative, not NaN). With {@code +INF}, the callback must stop
     *             before traversal exhausts the signed int cell range
     * @param visitor callback for each visited voxel
     * @throws IllegalArgumentException if tMax is negative/NaN or ray coordinates/direction are invalid
     * @throws ArithmeticException if stepping would overflow a cell index
     */
    void traverse(Ray ray, double tMax, CellVisitor visitor);
}
