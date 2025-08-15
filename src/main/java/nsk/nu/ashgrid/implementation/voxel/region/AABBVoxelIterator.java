package nsk.nu.ashgrid.implementation.voxel.region;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashgrid.api.voxel.region.RegionIterator;

/** Simple nested-loop iterator over voxels intersecting an AABB. */
public final class AABBVoxelIterator implements RegionIterator {
    @Override
    public void forEachCell(AxisAlignedBox box, CellConsumer c) {
        int x0 = (int) Math.floor(box.min().x());
        int y0 = (int) Math.floor(box.min().y());
        int z0 = (int) Math.floor(box.min().z());
        int x1 = (int) Math.floor(Math.nextDown(box.max().x()));
        int y1 = (int) Math.floor(Math.nextDown(box.max().y()));
        int z1 = (int) Math.floor(Math.nextDown(box.max().z()));
        for (int z = z0; z <= z1; z++)
            for (int y = y0; y <= y1; y++)
                for (int x = x0; x <= x1; x++)
                    c.accept(x, y, z);
    }
}