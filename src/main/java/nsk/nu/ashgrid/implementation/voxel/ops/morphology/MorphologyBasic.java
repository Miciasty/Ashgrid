package nsk.nu.ashgrid.implementation.voxel.ops.morphology;

import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.WritableGrid3i;
import nsk.nu.ashgrid.api.voxel.neighborhood.Neighborhood3D;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;

import java.util.function.IntPredicate;

/** Simple one-step morphology using chosen neighborhood. No nested triple loops. */
public final class MorphologyBasic implements Morphology {
    @Override public String id() { return "MorphologyBasic"; }

    @Override
    public void dilate(ReadableGrid3i src, IntPredicate fg, WritableGrid3i dst, Neighborhood nh) {
        final int[][] offs = switch (nh) {
            case N6 -> Neighborhood3D.N6; case N18 -> Neighborhood3D.N18; default -> Neighborhood3D.N26; };
        final int w=src.width(), h=src.height(), d=src.depth();
        final int wh=w*h, total=wh*d;
        for (int i=0;i<total;i++){
            int z=i/wh, rem=i-z*wh, y=rem/w, x=rem-y*w;
            boolean on = fg.test(src.get(x,y,z));
            if (!on) for (int[] o:offs){
                int nx=x+o[0], ny=y+o[1], nz=z+o[2];
                if (src.inside(nx,ny,nz) && fg.test(src.get(nx,ny,nz))) { on=true; break; }
            }
            dst.set(x,y,z, on?1:0);
        }
    }

    @Override
    public void erode(ReadableGrid3i src, IntPredicate fg, WritableGrid3i dst, Neighborhood nh) {
        final int[][] offs = switch (nh) {
            case N6 -> Neighborhood3D.N6; case N18 -> Neighborhood3D.N18; default -> Neighborhood3D.N26; };
        final int w=src.width(), h=src.height(), d=src.depth();
        final int wh=w*h, total=wh*d;
        for (int i=0;i<total;i++){
            int z=i/wh, rem=i-z*wh, y=rem/w, x=rem-y*w;
            boolean on = fg.test(src.get(x,y,z));
            if (on) for (int[] o:offs){
                int nx=x+o[0], ny=y+o[1], nz=z+o[2];
                if (!src.inside(nx,ny,nz) || !fg.test(src.get(nx,ny,nz))) { on=false; break; }
            }
            dst.set(x,y,z, on?1:0);
        }
    }
}
