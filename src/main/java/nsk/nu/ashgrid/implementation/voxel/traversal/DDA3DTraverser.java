package nsk.nu.ashgrid.implementation.voxel.traversal;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.voxel.traversal.CellVisitor;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

/** 3D DDA (Amanatides & Woo) voxel traversal. */
public final class DDA3DTraverser implements VoxelTraverser {
    @Override public String id() { return "dda"; }

    @Override
    public void traverse(Ray ray, double tMax, CellVisitor visitor) {
        if (tMax < 0) throw new IllegalArgumentException("tMax must be >= 0");

        final Vector3 o = ray.origin();
        final Vector3 d = ray.direction();

        int x = (int) Math.floor(o.x());
        int y = (int) Math.floor(o.y());
        int z = (int) Math.floor(o.z());

        final int stepX = d.x() > 0 ? 1 : -1;
        final int stepY = d.y() > 0 ? 1 : -1;
        final int stepZ = d.z() > 0 ? 1 : -1;

        final double ax = Math.abs(d.x());
        final double ay = Math.abs(d.y());
        final double az = Math.abs(d.z());

        final double invAx = ax != 0 ? 1.0 / ax : Double.POSITIVE_INFINITY;
        final double invAy = ay != 0 ? 1.0 / ay : Double.POSITIVE_INFINITY;
        final double invAz = az != 0 ? 1.0 / az : Double.POSITIVE_INFINITY;

        final double nx = (x + (stepX > 0 ? 1 : 0)) - o.x();
        final double ny = (y + (stepY > 0 ? 1 : 0)) - o.y();
        final double nz = (z + (stepZ > 0 ? 1 : 0)) - o.z();

        double tMaxX = ax != 0 ? nx * invAx : Double.POSITIVE_INFINITY;
        double tMaxY = ay != 0 ? ny * invAy : Double.POSITIVE_INFINITY;
        double tMaxZ = az != 0 ? nz * invAz : Double.POSITIVE_INFINITY;

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
                case 0 -> { x += stepX; tMaxX += tDeltaX; }
                case 1 -> { y += stepY; tMaxY += tDeltaY; }
                default -> { z += stepZ; tMaxZ += tDeltaZ; }
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