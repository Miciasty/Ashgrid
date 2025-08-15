package nsk.nu.ashgrid.api.raster.view;

import nsk.nu.ashgrid.api.raster.Grid3i;

/** A view into a sub-region of another {@link Grid3i}. */
public final class SubGrid3i implements Grid3i {
    private final Grid3i src; private final int ox,oy,oz, w,h,d;

    public SubGrid3i(Grid3i src, int ox, int oy, int oz, int width, int height, int depth) {
        this.src = src; this.ox=ox; this.oy=oy; this.oz=oz; this.w=width; this.h=height; this.d=depth;
        if (w<=0||h<=0||d<=0) throw new IllegalArgumentException("All dimensions must be > 0");
    }

    @Override public int get(int x,int y,int z){ check(x,y,z); return src.get(ox+x, oy+y, oz+z); }
    @Override public void set(int x,int y,int z,int v){ check(x,y,z); src.set(ox+x, oy+y, oz+z, v); }
    @Override public boolean inside(int x,int y,int z){ return x>=0&&x<w&&y>=0&&y<h&&z>=0&&z<d; }
    @Override public int width(){ return w; } @Override public int height(){ return h; } @Override public int depth(){ return d; }

    private void check(int x,int y,int z){ if(!inside(x,y,z)) throw new IndexOutOfBoundsException(); }
}