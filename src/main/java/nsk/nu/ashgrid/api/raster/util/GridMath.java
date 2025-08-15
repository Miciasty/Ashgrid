package nsk.nu.ashgrid.api.raster.util;

import nsk.nu.ashcore.api.math.Vector3;

/** Utility conversions and indexing helpers for grids. */
public final class GridMath {
    private GridMath() {}

    public static int linearIndex(int x,int y,int z, int w,int h){ return z*(w*h) + y*w + x; }
    public static int[] unindex(int i, int w,int h){
        int wh = w*h;
        int z = i / wh;
        int rem = i - z*wh;
        int y = rem / w;
        int x = rem - y*w;
        return new int[]{x,y,z};
    }

    /** World-space to cell index (floor). */
    public static int cellX(Vector3 p){ return (int)Math.floor(p.x()); }
    public static int cellY(Vector3 p){ return (int)Math.floor(p.y()); }
    public static int cellZ(Vector3 p){ return (int)Math.floor(p.z()); }

    /** Cell center in world-space. */
    public static Vector3 cellCenter(int x,int y,int z){
        return new Vector3(x + 0.5, y + 0.5, z + 0.5);
    }
}