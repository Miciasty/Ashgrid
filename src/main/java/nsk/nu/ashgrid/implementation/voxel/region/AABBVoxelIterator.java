package nsk.nu.ashgrid.implementation.voxel.region;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashgrid.api.raster.util.GridMath;
import nsk.nu.ashgrid.api.voxel.region.RegionIterator;
import nsk.nu.ashgrid.api.voxel.region.RegionIterators;

/** Simple nested-loop iterator over voxels intersecting an AABB. */
public final class AABBVoxelIterator implements RegionIterator {
    @Override
    public void forEachCell(AxisAlignedBox box, CellConsumer c) {
        int x0 = GridMath.cellX(box.min()), y0 = GridMath.cellY(box.min()), z0 = GridMath.cellZ(box.min());
        if (!Double.isFinite(box.max().x()) || !Double.isFinite(box.max().y()) || !Double.isFinite(box.max().z()))
            throw new IllegalArgumentException("bounds must be finite");
        if (box.max().x()<=box.min().x() || box.max().y()<=box.min().y() || box.max().z()<=box.min().z()) return;
        int x1 = GridMath.floorToInt(Math.nextDown(box.max().x()));
        int y1 = GridMath.floorToInt(Math.nextDown(box.max().y()));
        int z1 = GridMath.floorToInt(Math.nextDown(box.max().z()));
        RegionIterators.forEachCellBox(x0,y0,z0,x1,y1,z1,c::accept);
    }
}
