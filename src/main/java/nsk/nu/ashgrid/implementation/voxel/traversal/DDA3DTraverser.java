package nsk.nu.ashgrid.implementation.voxel.traversal;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.raster.util.GridMath;
import nsk.nu.ashgrid.api.voxel.traversal.CellVisitor;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

/**
 * 3D DDA (Amanatides and Woo) on unit cells, starting at floor(origin).
 * Exact ties step X, then Y, then Z, emitting zero-length intermediate visits.
 * This is not supercover: not every cell touched at an edge or corner is visited.
 */
public final class DDA3DTraverser implements VoxelTraverser {
    @Override public String id() { return "dda"; }

    @Override
    public void traverse(Ray ray, double tMax, CellVisitor visitor) {
        if (!(tMax >= 0)) throw new IllegalArgumentException("tMax must be >= 0");
        if (visitor == null) throw new NullPointerException("visitor");

        final Vector3 o = ray.origin();
        final Vector3 d = ray.direction();

        int x = GridMath.cellX(o);
        int y = GridMath.cellY(o);
        int z = GridMath.cellZ(o);
        if (!Double.isFinite(d.x()) || !Double.isFinite(d.y()) || !Double.isFinite(d.z())
                || (d.x() == 0 && d.y() == 0 && d.z() == 0))
            throw new IllegalArgumentException("direction must be finite and non-zero");

        final int stepX = d.x() > 0 ? 1 : -1;
        final int stepY = d.y() > 0 ? 1 : -1;
        final int stepZ = d.z() > 0 ? 1 : -1;

        final double ax = Math.abs(d.x());
        final double ay = Math.abs(d.y());
        final double az = Math.abs(d.z());

        final double invAx = ax != 0 ? 1.0 / ax : Double.POSITIVE_INFINITY;
        final double invAy = ay != 0 ? 1.0 / ay : Double.POSITIVE_INFINITY;
        final double invAz = az != 0 ? 1.0 / az : Double.POSITIVE_INFINITY;

        final double nx = stepX > 0 ? ((double)x + 1) - o.x() : o.x() - x;
        final double ny = stepY > 0 ? ((double)y + 1) - o.y() : o.y() - y;
        final double nz = stepZ > 0 ? ((double)z + 1) - o.z() : o.z() - z;

        double tMaxX = ax != 0 ? nx / ax : Double.POSITIVE_INFINITY;
        double tMaxY = ay != 0 ? ny / ay : Double.POSITIVE_INFINITY;
        double tMaxZ = az != 0 ? nz / az : Double.POSITIVE_INFINITY;

        final double tDeltaX = invAx;
        final double tDeltaY = invAy;
        final double tDeltaZ = invAz;

        double t = 0.0;

        while (t < tMax) {
            final double tNext = min3(tMaxX, tMaxY, tMaxZ);
            final double tEnter = t;
            final double tExit  = Math.min(tNext, tMax);
            if (!visitor.visit(x, y, z, tEnter, tExit)) return;

            t = tNext;
            if (t >= tMax) return;

            int axis = argmin3(tMaxX, tMaxY, tMaxZ);
            switch (axis) {
                case 0 -> { x = Math.addExact(x, stepX); tMaxX += tDeltaX; }
                case 1 -> { y = Math.addExact(y, stepY); tMaxY += tDeltaY; }
                default -> { z = Math.addExact(z, stepZ); tMaxZ += tDeltaZ; }
            }
        }
    }

    private static double min3(double a, double b, double c) { return Math.min(a, Math.min(b, c)); }
    private static int argmin3(double a, double b, double c) {
        if (a <= b && a <= c) return 0;
        if (b <= c) return 1;
        return 2;
    }
}
