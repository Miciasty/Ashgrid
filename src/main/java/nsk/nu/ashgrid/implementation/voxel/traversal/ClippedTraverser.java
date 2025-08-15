package nsk.nu.ashgrid.implementation.voxel.traversal;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.voxel.traversal.CellVisitor;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

/**
 * Decorator that clips a ray to an AABB before delegating traversal.
 */
public final class ClippedTraverser implements VoxelTraverser {
    private final VoxelTraverser delegate;
    private final AxisAlignedBox clip;

    public ClippedTraverser(VoxelTraverser delegate, AxisAlignedBox clip) {
        this.delegate = delegate; this.clip = clip;
    }

    @Override public String id() { return delegate.id() + "-clipped"; }

    @Override
    public void traverse(Ray ray, double tMax, CellVisitor visitor) {
        double[] te = intersectRayAABB(ray, clip);
        if (te == null) return; // no intersection
        double tEnter = Math.max(0.0, te[0]);
        double tExit  = Math.min(tMax, te[1]);
        if (tExit <= tEnter) return;

        Vector3 o = ray.origin().add(ray.direction().mul(tEnter));
        Ray shifted = new Ray(o, ray.direction());
        delegate.traverse(shifted, tExit - tEnter, visitor);
    }

    /** Slab method. Returns [tEnter, tExit] or null if no hit. */
    private static double[] intersectRayAABB(Ray ray, AxisAlignedBox box) {
        double tmin = 0.0;
        double tmax = Double.POSITIVE_INFINITY;

        double[] ro = { ray.origin().x(), ray.origin().y(), ray.origin().z() };
        double[] rd = { ray.direction().x(), ray.direction().y(), ray.direction().z() };
        double[] mn = { box.min().x(), box.min().y(), box.min().z() };
        double[] mx = { box.max().x(), box.max().y(), box.max().z() };

        for (int i=0;i<3;i++) {
            double o = ro[i], d = rd[i], min = mn[i], max = mx[i];
            if (d == 0.0) {
                if (o < min || o > max) return null;
            } else {
                double inv = 1.0 / d;
                double t1 = (min - o) * inv;
                double t2 = (max - o) * inv;
                if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                tmin = Math.max(tmin, t1);
                tmax = Math.min(tmax, t2);
                if (tmax < tmin) return null;
            }
        }
        return new double[]{tmin, tmax};
    }
}