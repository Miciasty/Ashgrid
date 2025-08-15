package nsk.nu.ashgrid.api.voxel.traversal;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;

/**
 * Convenience helpers for voxel traversal.
 */
public final class VoxelTraversers {
    private VoxelTraversers() {}

    /** Clip traversal to an AABB before delegating. */
    public static VoxelTraverser clipped(VoxelTraverser delegate, AxisAlignedBox clip) {
        return new VoxelTraverser() {
            @Override public String id() { return delegate.id() + "-clipped"; }
            @Override public void traverse(Ray ray, double tMax, CellVisitor visitor) {
                double[] te = intersectRayAABB(ray, clip);
                if (te == null) return;
                double tEnter = Math.max(0.0, te[0]);
                double tExit  = Math.min(tMax, te[1]);
                if (tExit <= tEnter) return;

                Vector3 o = ray.origin().add(ray.direction().mul(tEnter));
                delegate.traverse(new Ray(o, ray.direction()), tExit - tEnter, visitor);
            }
        };
    }

    /** Slab method. Returns [tEnter, tExit] or null if no hit. */
    private static double[] intersectRayAABB(Ray ray, AxisAlignedBox box) {
        double tmin = 0.0, tmax = Double.POSITIVE_INFINITY;
        double[] ro = { ray.origin().x(), ray.origin().y(), ray.origin().z() };
        double[] rd = { ray.direction().x(), ray.direction().y(), ray.direction().z() };
        double[] mn = { box.min().x(), box.min().y(), box.min().z() };
        double[] mx = { box.max().x(), box.max().y(), box.max().z() };

        for (int i=0;i<3;i++) {
            double o = ro[i], d = rd[i], min = mn[i], max = mx[i];
            if (d == 0.0) { if (o < min || o > max) return null; }
            else {
                double inv = 1.0 / d;
                double t1 = (min - o) * inv, t2 = (max - o) * inv;
                if (t1 > t2) { double t = t1; t1 = t2; t2 = t; }
                tmin = Math.max(tmin, t1);
                tmax = Math.min(tmax, t2);
                if (tmax < tmin) return null;
            }
        }
        return new double[]{tmin, tmax};
    }
}