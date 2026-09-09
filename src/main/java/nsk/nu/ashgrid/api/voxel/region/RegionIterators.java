package nsk.nu.ashgrid.api.voxel.region;

import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.raster.util.GridMath;

/**
 * Synchronous region iteration, with no retained grid or snapshot. Callbacks run in loop order:
 * boxes/spheres scan Z,Y,X, cylinders Y,Z,X (X fastest). Callback state must be repeatable.
 * Inclusive boxes with a reversed axis are empty. Shape centers/radii must be finite, radius >= 0,
 * and the candidate cell bounds must fit int. These helpers do not accept infinite regions.
 */
public final class RegionIterators {
    private RegionIterators() {}

    /** Iterate all cells in axis-aligned bounding box defined by integer ranges (inclusive). */
    public static void forEachCellBox(int x0,int y0,int z0, int x1,int y1,int z1, CellConsumer c) {
        for (long z=z0; z<=z1; z++)
            for (long y=y0; y<=y1; y++)
                for (long x=x0; x<=x1; x++)
                    c.accept((int)x,(int)y,(int)z);
    }

    /** Iterate cells whose centers are inside or on the sphere, in unit-grid coordinates. */
    public static void forEachCellSphere(Vector3 center, double radius, CellConsumer c) {
        requireShape(center,radius);
        int x0=lower(center.x(),radius), x1=upper(center.x(),radius);
        int y0=lower(center.y(),radius), y1=upper(center.y(),radius);
        int z0=lower(center.z(),radius), z1=upper(center.z(),radius);
        double r2 = radius*radius;

        for (long z=z0; z<=z1; z++)
            for (long y=y0; y<=y1; y++)
                for (long x=x0; x<=x1; x++) {
                    double dx = (x+0.5) - center.x();
                    double dy = (y+0.5) - center.y();
                    double dz = (z+0.5) - center.z();
                    if (dx*dx + dy*dy + dz*dz <= r2) c.accept((int)x,(int)y,(int)z);
                }
    }

    /** Iterate cell centers inside/on an XZ circle for each Y in the inclusive [y0,y1] range. */
    public static void forEachCellCylinderXZ(Vector3 center, double radius, int y0, int y1, CellConsumer c) {
        requireShape(center,radius);
        int x0=lower(center.x(),radius), x1=upper(center.x(),radius);
        int z0=lower(center.z(),radius), z1=upper(center.z(),radius);
        double r2 = radius*radius;
        for (long y=y0; y<=y1; y++)
            for (long z=z0; z<=z1; z++)
                for (long x=x0; x<=x1; x++) {
                    double dx = (x+0.5) - center.x();
                    double dz = (z+0.5) - center.z();
                    if (dx*dx + dz*dz <= r2) c.accept((int)x,(int)y,(int)z);
                }
    }

    private static int lower(double center,double radius) { return GridMath.floorToInt(Math.ceil(center-radius-0.5)); }
    private static int upper(double center,double radius) { return GridMath.floorToInt(center+radius-0.5); }
    private static void requireShape(Vector3 center,double radius) {
        if (!(radius>=0) || !Double.isFinite(radius) || !Double.isFinite(center.x())
                || !Double.isFinite(center.y()) || !Double.isFinite(center.z()))
            throw new IllegalArgumentException("center/radius must be finite and radius >= 0");
    }

    @FunctionalInterface public interface CellConsumer { void accept(int x,int y,int z); }
}
