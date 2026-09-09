package nsk.nu.ashgrid.implementation.voxel.ops.morphology;

import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.util.GridMath;
import nsk.nu.ashgrid.api.raster.WritableGrid3i;
import nsk.nu.ashgrid.api.voxel.neighborhood.Neighborhood3D;
import nsk.nu.ashgrid.api.voxel.ops.VoxelTask;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;

import java.util.function.IntPredicate;
import java.util.Objects;

/** Simple one-step morphology using chosen neighborhood. No nested triple loops. */
public final class MorphologyBasic implements Morphology {
    @Override public String id() { return "MorphologyBasic"; }

    @Override
    public void dilate(ReadableGrid3i src, IntPredicate fg, WritableGrid3i dst, Neighborhood nh) {
        beginDilate(src,fg,dst,nh).runToCompletion();
    }

    @Override
    public void erode(ReadableGrid3i src, IntPredicate fg, WritableGrid3i dst, Neighborhood nh) {
        beginErode(src,fg,dst,nh).runToCompletion();
    }

    /**
     * One unit writes one cell after checking at most 26 neighbors. No volume-sized scratch.
     * Source, dimensions, predicate and neighborhood must stay stable between steps.
     * Output must use separate storage, including views, and must not be changed by the caller.
     */
    public VoxelTask beginDilate(ReadableGrid3i src,IntPredicate fg,WritableGrid3i dst,Neighborhood nh){
        return begin(src,fg,dst,nh,true);
    }

    /** Same unit and source/output ownership rules as beginDilate. */
    public VoxelTask beginErode(ReadableGrid3i src,IntPredicate fg,WritableGrid3i dst,Neighborhood nh){
        return begin(src,fg,dst,nh,false);
    }

    private VoxelTask begin(ReadableGrid3i src,IntPredicate fg,WritableGrid3i dst,Neighborhood nh,boolean dilate){
        Objects.requireNonNull(fg); Objects.requireNonNull(dst);
        final int[][] offs = switch (nh) {
            case N6 -> Neighborhood3D.N6; case N18 -> Neighborhood3D.N18; default -> Neighborhood3D.N26; };
        final int w=src.width(), h=src.height(), d=src.depth();
        final int total=GridMath.cellCount(w,h,d), wh=w*h;
        requireOutput(src,dst);
        return new VoxelTask() {
            private int i;

            @Override protected boolean advance(){
                int z=i/wh, rem=i-z*wh, y=rem/w, x=rem-y*w;
                boolean on=fg.test(src.get(x,y,z));
                if (on!=dilate) for (int[] o : offs) {
                    int nx=x+o[0], ny=y+o[1], nz=z+o[2];
                    boolean neighbor=src.inside(nx,ny,nz) && fg.test(src.get(nx,ny,nz));
                    if (neighbor==dilate) { on=dilate; break; }
                }
                dst.set(x,y,z,on?1:0);
                return ++i==total;
            }
        };
    }

    private static void requireOutput(ReadableGrid3i src, WritableGrid3i dst) {
        if (src == dst) throw new IllegalArgumentException("output must not alias source");
        if (dst instanceof ReadableGrid3i out
                && (out.width()!=src.width() || out.height()!=src.height() || out.depth()!=src.depth()))
            throw new IllegalArgumentException("output dimensions must match source");
    }
}
