package nsk.nu.ashgrid.api.raster.view;

import nsk.nu.ashgrid.api.raster.Grid3i;

/** Read-only wrapper around a {@link Grid3i}. */
public final class ConstGrid3i implements Grid3i {
    private final Grid3i src;
    public ConstGrid3i(Grid3i src){ this.src = src; }

    @Override public int get(int x,int y,int z){ return src.get(x,y,z); }
    @Override public void set(int x,int y,int z,int v){ throw new UnsupportedOperationException("read-only"); }
    @Override public boolean inside(int x,int y,int z){ return src.inside(x,y,z); }
    @Override public int width(){ return src.width(); }
    @Override public int height(){ return src.height(); }
    @Override public int depth(){ return src.depth(); }
}