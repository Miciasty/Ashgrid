package nsk.nu.ashgrid.api.voxel.traversal;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.spi.Identified;

/**
 * Visits voxels intersected by a ray up to a given maximum ray parameter.
 * Implementations must be deterministic for the same inputs.
 */
public interface VoxelTraverser extends Identified {

    /**
     * Traverse voxels intersected by {@code ray} from {@code t=0} up to {@code tMax},
     * invoking {@code visitor} for each visited voxel in order.
     *
     * <p>The implementation should call the visitor with a half-open interval
     * {@code [tEnter, tExit)} for the voxel. If {@code visitor.visit(...)} returns {@code false},
     * the traversal must stop immediately.</p>
     *
     * @param ray  ray in continuous space (origin and direction from Ashcore)
     * @param tMax maximum ray parameter (non-negative). If {@code tMax} is {@code +INF}, traversal proceeds until termination.
     * @param visitor callback for each visited voxel
     * @throws IllegalArgumentException if {@code tMax} is negative
     */
    void traverse(Ray ray, double tMax, CellVisitor visitor);
}