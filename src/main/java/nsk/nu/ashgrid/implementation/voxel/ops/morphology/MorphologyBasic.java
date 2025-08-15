package nsk.nu.ashgrid.implementation.voxel.ops.morphology;

import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.WritableGrid3i;
import nsk.nu.ashgrid.api.voxel.neighborhood.Neighborhood3D;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;

import java.util.function.IntPredicate;

/** Simple one-step morphology using chosen neighborhood. No nested triple loops. */
public final class MorphologyBasic implements Morphology {

    @Override
    public void dilate(ReadableGrid3i src, IntPredicate isForeground, WritableGrid3i dst, Neighborhood nh) {
        final int[][] offs = toOffsets(nh);
        final int w = src.width(), h = src.height(), d = src.depth();
        final int wh = w * h, total = wh * d;

        for (int i = 0; i < total; i++) {
            final int z = i / wh;
            final int rem = i - z * wh;
            final int y = rem / w;
            final int x = rem - y * w;

            boolean fg = isForeground.test(src.get(x, y, z));
            if (!fg && anyNeighbor(src, x, y, z, offs, isForeground)) {
                fg = true;
            }
            dst.set(x, y, z, fg ? 1 : 0);
        }
    }

    @Override
    public void erode(ReadableGrid3i src, IntPredicate isForeground, WritableGrid3i dst, Neighborhood nh) {
        final int[][] offs = toOffsets(nh);
        final int w = src.width(), h = src.height(), d = src.depth();
        final int wh = w * h, total = wh * d;

        for (int i = 0; i < total; i++) {
            final int z = i / wh;
            final int rem = i - z * wh;
            final int y = rem / w;
            final int x = rem - y * w;

            boolean fg = isForeground.test(src.get(x, y, z));
            if (fg && !allNeighbors(src, x, y, z, offs, isForeground)) {
                fg = false;
            }
            dst.set(x, y, z, fg ? 1 : 0);
        }
    }

    private static boolean anyNeighbor(ReadableGrid3i src, int x, int y, int z,
                                       int[][] offs, IntPredicate isFg) {
        for (int[] o : offs) {
            int nx = x + o[0], ny = y + o[1], nz = z + o[2];
            if (src.inside(nx, ny, nz) && isFg.test(src.get(nx, ny, nz))) {
                return true;
            }
        }
        return false;
    }

    private static boolean allNeighbors(ReadableGrid3i src, int x, int y, int z,
                                        int[][] offs, IntPredicate isFg) {
        for (int[] o : offs) {
            int nx = x + o[0], ny = y + o[1], nz = z + o[2];
            if (!src.inside(nx, ny, nz) || !isFg.test(src.get(nx, ny, nz))) {
                return false;
            }
        }
        return true;
    }

    private static int[][] toOffsets(Neighborhood nh) {
        return switch (nh) {
            case N6  -> Neighborhood3D.N6;
            case N18 -> Neighborhood3D.N18;
            case N26 -> Neighborhood3D.N26;
        };
    }
}
