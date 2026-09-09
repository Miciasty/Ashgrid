package nsk.nu.ashgrid.api.raster.util;

import nsk.nu.ashcore.api.math.Vector3;

/** Utility conversions and indexing helpers for grids. */
public final class GridMath {
    private GridMath() {}

    /** Positive dimensions with an int-sized volume; checks before allocation or flattening. */
    public static int cellCount(int w,int h,int d){
        if (w<=0||h<=0||d<=0) throw new IllegalArgumentException("dims > 0");
        long wh = (long)w*h;
        if (wh > Integer.MAX_VALUE || wh*d > Integer.MAX_VALUE)
            throw new IllegalArgumentException("volume exceeds int range");
        return (int)(wh*d);
    }

    public static int linearIndex(int x,int y,int z, int w,int h){
        int wh = cellCount(w,h,1);
        if (x<0||x>=w||y<0||y>=h||z<0) throw new IndexOutOfBoundsException();
        long i = (long)z*wh + (long)y*w + x;
        if (i > Integer.MAX_VALUE) throw new IllegalArgumentException("index exceeds int range");
        return (int)i;
    }
    public static int[] unindex(int i, int w,int h){
        int wh = cellCount(w,h,1);
        if (i < 0) throw new IndexOutOfBoundsException();
        int z = i / wh;
        int rem = i - z*wh;
        int y = rem / w;
        int x = rem - y*w;
        return new int[]{x,y,z};
    }

    /** Floor a finite coordinate, rejecting values outside the signed int cell range. */
    public static int floorToInt(double value){
        double f = Math.floor(value);
        if (!Double.isFinite(f) || f < Integer.MIN_VALUE || f > Integer.MAX_VALUE)
            throw new IllegalArgumentException("coordinate outside int range: " + value);
        return (int)f;
    }

    /** Continuous unit-grid coordinate to cell index (floor), without an epsilon. */
    public static int cellX(Vector3 p){ return floorToInt(p.x()); }
    public static int cellY(Vector3 p){ return floorToInt(p.y()); }
    public static int cellZ(Vector3 p){ return floorToInt(p.z()); }

    /** Cell center in continuous unit-grid coordinates. */
    public static Vector3 cellCenter(int x,int y,int z){
        return new Vector3(x + 0.5, y + 0.5, z + 0.5);
    }
}
