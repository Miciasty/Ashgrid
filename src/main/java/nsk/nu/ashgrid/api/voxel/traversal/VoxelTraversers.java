package nsk.nu.ashgrid.api.voxel.traversal;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;

/**
 * Convenience helpers for voxel traversal.
 */
public final class VoxelTraversers {
    private VoxelTraversers() {}

    /**
     * Clip to a finite half-open unit-grid AABB, preserving original ray distances.
     * Empty boxes emit no visits. Delegate boundary visits at entry are retained, including
     * a zero-length visit on the upper face when entering in a negative direction.
     */
    public static VoxelTraverser clipped(VoxelTraverser delegate, AxisAlignedBox clip) {
        if (delegate == null) throw new NullPointerException("delegate");
        requireFinite(clip.min()); requireFinite(clip.max());
        return new VoxelTraverser() {
            @Override public String id() { return delegate.id() + "-clipped"; }
            @Override public void traverse(Ray ray, double tMax, CellVisitor visitor) {
                if (!(tMax >= 0)) throw new IllegalArgumentException("tMax must be >= 0");
                requireFinite(ray.origin()); requireFinite(ray.direction());
                if (ray.direction().x() == 0 && ray.direction().y() == 0 && ray.direction().z() == 0)
                    throw new IllegalArgumentException("direction must be non-zero");
                if (clip.max().x() <= clip.min().x() || clip.max().y() <= clip.min().y()
                        || clip.max().z() <= clip.min().z()) return;
                double[] te = intersectRayAABB(ray, clip);
                if (te == null) return;
                double tEnter = Math.max(0.0, te[0]);
                double tExit  = Math.min(tMax, te[1]);
                if (tExit <= tEnter) return;

                Vector3 o = ray.origin().add(ray.direction().mul(tEnter));
                delegate.traverse(new Ray(o, ray.direction()), tExit - tEnter, (x, y, z, localT0, localT1) ->
                        visitor.visit(x, y, z, localT0 + tEnter, localT1 + tEnter));
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
            if (d == 0.0) { if (o < min || o >= max) return null; }
            else {
                double t1 = (min - o) / d, t2 = (max - o) / d;
                if (t1 > t2) { double t = t1; t1 = t2; t2 = t; }
                tmin = Math.max(tmin, t1);
                tmax = Math.min(tmax, t2);
                if (tmax < tmin) return null;
            }
        }
        return new double[]{tmin, tmax};
    }

    private static void requireFinite(Vector3 p) {
        if (!Double.isFinite(p.x()) || !Double.isFinite(p.y()) || !Double.isFinite(p.z()))
            throw new IllegalArgumentException("coordinates must be finite");
    }
}
