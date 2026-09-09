package nsk.nu.ashgrid.api.voxel.region;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;

/** Iterates over integer cells inside regions (AABB to voxel ranges). */
public interface RegionIterator {

    /** Consumer receiving each cell inside the region. */
    @FunctionalInterface
    interface CellConsumer { void accept(int x, int y, int z); }

    /**
     * Iterate all cells whose unit voxels intersect the finite half-open AABB.
     * Bounds are floor(min) through floor(nextDown(max)), inclusive; empty boxes emit nothing.
     * Cell indices must fit int. AABBVoxelIterator scans Z,Y,X with X fastest.
     * Iteration is synchronous and owns no grid state or snapshot.
     */
    void forEachCell(AxisAlignedBox box, CellConsumer consumer);
}
