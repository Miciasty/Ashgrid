package nsk.nu.ashgrid.api.voxel.region;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;

/** Iterates over integer cells inside regions (AABB to voxel ranges). */
public interface RegionIterator {

    /** Consumer receiving each cell inside the region. */
    @FunctionalInterface
    interface CellConsumer { void accept(int x, int y, int z); }

    /**
     * Iterate all cells whose unit voxels intersect the AABB
     * (i.e. floor(min) .. floor(max)-1 in each dimension).
     */
    void forEachCell(AxisAlignedBox box, CellConsumer consumer);
}