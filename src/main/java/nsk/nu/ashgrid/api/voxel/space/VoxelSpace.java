package nsk.nu.ashgrid.api.voxel.space;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Defines mapping between world space and voxel space by scale and origin.
 * By default Ashgrid assumes unit voxels at origin. Use this to adapt different scales.
 */
public final class VoxelSpace {
    private final double scale;
    private final Vector3 origin;

    public VoxelSpace(double scale, Vector3 origin) {
        if (scale <= 0) throw new IllegalArgumentException("scale must be > 0");
        this.scale = scale; this.origin = origin;
    }

    /** Convert world position to cell index (floor). */
    public int ix(Vector3 p){ return (int)Math.floor((p.x() - origin.x()) / scale); }
    public int iy(Vector3 p){ return (int)Math.floor((p.y() - origin.y()) / scale); }
    public int iz(Vector3 p){ return (int)Math.floor((p.z() - origin.z()) / scale); }

    /** Cell corner in world space. */
    public Vector3 corner(int x,int y,int z){
        return new Vector3(origin.x() + x*scale, origin.y() + y*scale, origin.z() + z*scale);
    }

    /** Cell center in world space. */
    public Vector3 center(int x,int y,int z){
        return new Vector3(origin.x() + (x+0.5)*scale,
                origin.y() + (y+0.5)*scale,
                origin.z() + (z+0.5)*scale);
    }

    public double scale(){ return scale; }
    public Vector3 origin(){ return origin; }
}
