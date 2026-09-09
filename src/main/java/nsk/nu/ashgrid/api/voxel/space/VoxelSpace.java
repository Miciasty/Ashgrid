package nsk.nu.ashgrid.api.voxel.space;

import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.raster.util.GridMath;

/**
 * Defines mapping between world space and voxel space by scale and origin.
 * By default Ashgrid assumes unit voxels at origin. Use this to adapt different scales.
 * This supported helper performs no frame rotation; frame-aware conversion belongs to Ashspace.
 * Membership floors (position-origin)/scale without epsilon. Inputs and generated points must be
 * finite, and cell indices must fit int. Rounding at large origins/scales can erase cell detail.
 */
public final class VoxelSpace {
    private final double scale;
    private final Vector3 origin;

    public VoxelSpace(double scale, Vector3 origin) {
        if (!(scale > 0) || !Double.isFinite(scale)) throw new IllegalArgumentException("scale must be finite and > 0");
        if (origin == null) throw new IllegalArgumentException("origin must not be null");
        requireFinite(origin);
        this.scale = scale; this.origin = origin;
    }

    /** Convert world position to cell index (floor). */
    public int ix(Vector3 p){ requireFinite(p); return index(p.x() - origin.x()); }
    public int iy(Vector3 p){ requireFinite(p); return index(p.y() - origin.y()); }
    public int iz(Vector3 p){ requireFinite(p); return index(p.z() - origin.z()); }

    /** Cell corner in world space. */
    public Vector3 corner(int x,int y,int z){
        return requireFinite(new Vector3(origin.x() + x*scale, origin.y() + y*scale, origin.z() + z*scale));
    }

    /** Cell center in world space. */
    public Vector3 center(int x,int y,int z){
        return requireFinite(new Vector3(origin.x() + (x+0.5)*scale,
                origin.y() + (y+0.5)*scale,
                origin.z() + (z+0.5)*scale));
    }

    public double scale(){ return scale; }
    public Vector3 origin(){ return origin; }

    private int index(double delta){
        double value = delta / scale;
        // Preserve the lower cell if a negative quotient underflows to signed zero.
        return value == 0 && delta < 0 ? -1 : GridMath.floorToInt(value);
    }

    private static Vector3 requireFinite(Vector3 p){
        if (!Double.isFinite(p.x()) || !Double.isFinite(p.y()) || !Double.isFinite(p.z()))
            throw new IllegalArgumentException("position must be finite");
        return p;
    }
}
