package nsk.nu.ashgrid.api.raster.view;

import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.raster.BoundedGrid3i;
import nsk.nu.ashgrid.api.raster.SparseGrid3i;

import java.util.Objects;

/** Live bounded window onto sparse storage. Local (0,0,0) maps to the supplied origin. */
public final class SparseGridView3i implements BoundedGrid3i {
    private final SparseGrid3i src;
    private final int ox,oy,oz,w,h,d;

    public SparseGridView3i(SparseGrid3i src, IntBox3 bounds) {
        this(src,bounds.minX(),bounds.minY(),bounds.minZ(),bounds.width(),bounds.height(),bounds.depth());
    }

    /** Positive dimensions; the last cell on each axis must fit a signed int. No cells are allocated. */
    public SparseGridView3i(SparseGrid3i src, int ox,int oy,int oz, int width,int height,int depth) {
        this.src=Objects.requireNonNull(src); this.ox=ox; this.oy=oy; this.oz=oz;
        this.w=width; this.h=height; this.d=depth;
        if (w<=0||h<=0||d<=0) throw new IllegalArgumentException("All dimensions must be > 0");
        if ((long)ox+w-1>Integer.MAX_VALUE || (long)oy+h-1>Integer.MAX_VALUE || (long)oz+d-1>Integer.MAX_VALUE)
            throw new IllegalArgumentException("window exceeds signed int coordinates");
    }

    @Override public int get(int x,int y,int z){ check(x,y,z); return src.get(ox+x,oy+y,oz+z); }
    @Override public void set(int x,int y,int z,int v){ check(x,y,z); src.set(ox+x,oy+y,oz+z,v); }
    @Override public boolean inside(int x,int y,int z){ return x>=0&&x<w&&y>=0&&y<h&&z>=0&&z<d; }
    @Override public int width(){ return w; } @Override public int height(){ return h; } @Override public int depth(){ return d; }

    private void check(int x,int y,int z){ if (!inside(x,y,z)) throw new IndexOutOfBoundsException(); }
}
