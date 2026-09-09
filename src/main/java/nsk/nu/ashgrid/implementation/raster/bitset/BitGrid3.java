package nsk.nu.ashgrid.implementation.raster.bitset;

import nsk.nu.ashgrid.api.raster.util.GridMath;

import java.util.BitSet;

/** Boolean occupancy grid: 1 bit per voxel. */
public final class BitGrid3 {
    private final int w,h,d;
    private final BitSet bits;

    public BitGrid3(int width, int height, int depth){
        int total = GridMath.cellCount(width,height,depth);
        this.w=width; this.h=height; this.d=depth; this.bits=new BitSet(total);
    }

    public boolean get(int x,int y,int z){ check(x,y,z); return bits.get(idx(x,y,z)); }
    public void set(int x,int y,int z, boolean v){ check(x,y,z); bits.set(idx(x,y,z), v); }

    public int width(){ return w; } public int height(){ return h; } public int depth(){ return d; }

    private void check(int x,int y,int z){
        if (x<0||x>=w||y<0||y>=h||z<0||z>=d) throw new IndexOutOfBoundsException();
    }
    private int idx(int x,int y,int z){ return (z*h + y)*w + x; }
}
