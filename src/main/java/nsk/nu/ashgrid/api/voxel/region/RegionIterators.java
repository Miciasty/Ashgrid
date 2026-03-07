package nsk.nu.ashgrid.api.voxel.region;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Region iterators for common shapes.
 */
public final class RegionIterators {
    private RegionIterators() {}

    /** Iterate all cells in axis-aligned bounding box defined by integer ranges (inclusive). */
    public static void forEachCellBox(int x0,int y0,int z0, int x1,int y1,int z1, CellConsumer c) {
        for (int z=z0; z<=z1; z++)
            for (int y=y0; y<=y1; y++)
                for (int x=x0; x<=x1; x++)
                    c.accept(x,y,z);
    }

    /** Iterate cells that fall inside a sphere centered at {@code center} with given radius. */
    public static void forEachCellSphere(Vector3 center, double radius, CellConsumer c) {
        int cx = (int)Math.floor(center.x());
        int cy = (int)Math.floor(center.y());
        int cz = (int)Math.floor(center.z());
        int r = (int)Math.ceil(radius);
        double r2 = radius*radius;

        for (int z=cz-r; z<=cz+r; z++)
            for (int y=cy-r; y<=cy+r; y++)
                for (int x=cx-r; x<=cx+r; x++) {
                    double dx = (x+0.5) - center.x();
                    double dy = (y+0.5) - center.y();
                    double dz = (z+0.5) - center.z();
                    if (dx*dx + dy*dy + dz*dz <= r2) c.accept(x,y,z);
                }
    }

    /** Iterate cells inside a vertical cylinder aligned with Y axis in XZ plane. */
    public static void forEachCellCylinderXZ(Vector3 center, double radius, int y0, int y1, CellConsumer c) {
        int cx = (int)Math.floor(center.x());
        int cz = (int)Math.floor(center.z());
        int r = (int)Math.ceil(radius);
        double r2 = radius*radius;
        for (int y=y0; y<=y1; y++)
            for (int z=cz-r; z<=cz+r; z++)
                for (int x=cx-r; x<=cx+r; x++) {
                    double dx = (x+0.5) - center.x();
                    double dz = (z+0.5) - center.z();
                    if (dx*dx + dz*dz <= r2) c.accept(x,y,z);
                }
    }

    @FunctionalInterface public interface CellConsumer { void accept(int x,int y,int z); }
}
