package nsk.nu.ashgrid.implementation.raster.arrays;

import nsk.nu.ashgrid.api.raster.BoundedGrid3i;

/** Dense 3D int grid backed by a 1D array in Z-major order (z,y,x). */
public final class ArrayGrid3i implements BoundedGrid3i {
    private final int w,h,d;
    private final int[] data;

    public ArrayGrid3i(int width, int height, int depth) {
        if (width<=0||height<=0||depth<=0) throw new IllegalArgumentException("dims > 0");
        this.w=width; this.h=height; this.d=depth;
        this.data = new int[w*h*d];
    }

    @Override public int get(int x,int y,int z){ check(x,y,z); return data[idx(x,y,z)]; }
    @Override public void set(int x,int y,int z,int v){ check(x,y,z); data[idx(x,y,z)] = v; }
    @Override public boolean inside(int x,int y,int z){ return x>=0&&x<w&&y>=0&&y<h&&z>=0&&z<d; }
    @Override public int width(){ return w; }
    @Override public int height(){ return h; }
    @Override public int depth(){ return d; }

    private void check(int x,int y,int z){ if(!inside(x,y,z)) throw new IndexOutOfBoundsException(); }
    private int idx(int x,int y,int z){ return (z*h + y)*w + x; }
}
