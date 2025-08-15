package nsk.nu.ashgrid.api.raster.view;

import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.raster.ReadableGrid3i;

import java.util.function.IntPredicate;

/**
 * Write-through view that only writes foreground cells where {@code maskIsForeground} is true.
 * Reads pass through to the source grid.
 */
public final class MaskedGrid3i implements Grid3i {
    private final Grid3i src;
    private final ReadableGrid3i mask;
    private final IntPredicate maskIsForeground;

    public MaskedGrid3i(Grid3i src, ReadableGrid3i mask, IntPredicate maskIsForeground) {
        this.src = src; this.mask = mask; this.maskIsForeground = maskIsForeground;
    }

    @Override public int get(int x,int y,int z){ return src.get(x,y,z); }

    @Override
    public void set(int x,int y,int z,int v) {
        if (mask.inside(x,y,z) && maskIsForeground.test(mask.get(x,y,z))) {
            src.set(x,y,z,v);
        }
    }

    @Override public boolean inside(int x,int y,int z){ return src.inside(x,y,z); }
    @Override public int width(){ return src.width(); }
    @Override public int height(){ return src.height(); }
    @Override public int depth(){ return src.depth(); }
}