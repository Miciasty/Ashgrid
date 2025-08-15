package nsk.nu.ashgrid.implementation.voxel.traversal;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.voxel.traversal.CellVisitor;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

/**
 * 3D DDA (Amanatides & Woo) voxel traversal.
 * Assumes an axis-aligned unit grid where voxel (i,j,k) covers [i,i+1)×[j,j+1)×[k,k+1).
 */
public final class DDA3DTraverser implements VoxelTraverser {

    @Override
    public String id() {
        return "dda";
    }

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

        final double invAbsDx = d.x() != 0 ? 1.0 / Math.abs(d.x()) : Double.POSITIVE_INFINITY;
        final double invAbsDy = d.y() != 0 ? 1.0 / Math.abs(d.y()) : Double.POSITIVE_INFINITY;
        final double invAbsDz = d.z() != 0 ? 1.0 / Math.abs(d.z()) : Double.POSITIVE_INFINITY;

        final double nextBoundaryX = d.x() >= 0 ? (Math.floor(o.x()) + 1.0 - o.x()) : (o.x() - Math.floor(o.x()));
        final double nextBoundaryY = d.y() >= 0 ? (Math.floor(o.y()) + 1.0 - o.y()) : (o.y() - Math.floor(o.y()));
        final double nextBoundaryZ = d.z() >= 0 ? (Math.floor(o.z()) + 1.0 - o.z()) : (o.z() - Math.floor(o.z()));

        double tMaxX = d.x() != 0 ? nextBoundaryX * invAbsDx : Double.POSITIVE_INFINITY;
        double tMaxY = d.y() != 0 ? nextBoundaryY * invAbsDy : Double.POSITIVE_INFINITY;
        double tMaxZ = d.z() != 0 ? nextBoundaryZ * invAbsDz : Double.POSITIVE_INFINITY;

        final double tDeltaX = invAbsDx;
        final double tDeltaY = invAbsDy;
        final double tDeltaZ = invAbsDz;

        double t = 0.0;

        while (t < tMax) {
            final double tNext = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
            final double tEnter = t;
            final double tExit = Math.min(tNext, tMax);

            if (!visitor.visit(x, y, z, tEnter, tExit)) {
                return;
            }

            t = tNext;
            if (t >= tMax) {
                return;
            }

            if (tNext == tMaxX) {
                x += stepX;
                tMaxX += tDeltaX;
            } else if (tNext == tMaxY) {
                y += stepY;
                tMaxY += tDeltaY;
            } else {
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
        }
    }
}