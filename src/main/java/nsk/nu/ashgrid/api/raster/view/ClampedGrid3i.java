package nsk.nu.ashgrid.api.raster.view;

import nsk.nu.ashgrid.api.raster.Grid3i;

/**
 * Read/write view that clamps coordinates to the nearest valid cell.
 * Writes are clamped to the border cell.
 */
public final class ClampedGrid3i implements Grid3i {
    private final Grid3i src;

    public ClampedGrid3i(Grid3i src) { this.src = src; }

    @Override public int get(int x,int y,int z){ int[] c = clamp(x,y,z); return src.get(c[0], c[1], c[2]); }
    @Override public void set(int x,int y,int z,int v){ int[] c = clamp(x,y,z); src.set(c[0], c[1], c[2], v); }
    @Override public boolean inside(int x,int y,int z){ return true; }
    @Override public int width(){ return src.width(); } @Override public int height(){ return src.height(); } @Override public int depth(){ return src.depth(); }

    private int[] clamp(int x,int y,int z){
        x = Math.max(0, Math.min(x, src.width()-1));
        y = Math.max(0, Math.min(y, src.height()-1));
        z = Math.max(0, Math.min(z, src.depth()-1));
        return new int[]{x,y,z};
    }
}