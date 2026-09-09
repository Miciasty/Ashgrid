package nsk.nu.ashgrid.api.raster.view;

import nsk.nu.ashgrid.api.raster.BoundedGrid3i;
import nsk.nu.ashgrid.implementation.raster.bitset.BitGrid3;

import java.util.Objects;

/** Live integer view of boolean storage. Reads 0/1; writes accept only 0/1. */
public final class BitGrid3iView implements BoundedGrid3i {
    private final BitGrid3 src;

    public BitGrid3iView(BitGrid3 src){ this.src=Objects.requireNonNull(src); }

    @Override public int get(int x,int y,int z){ return src.get(x,y,z)?1:0; }
    @Override public void set(int x,int y,int z,int v){
        if (v!=0&&v!=1) throw new IllegalArgumentException("bit value must be 0 or 1");
        src.set(x,y,z,v==1);
    }
    @Override public boolean inside(int x,int y,int z){ return x>=0&&x<width()&&y>=0&&y<height()&&z>=0&&z<depth(); }
    @Override public int width(){ return src.width(); } @Override public int height(){ return src.height(); } @Override public int depth(){ return src.depth(); }
}
