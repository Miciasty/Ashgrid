package nsk.nu.ashgrid.api.raster.ops;

import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.WritableGrid3i;

import java.util.function.IntPredicate;

/**
 * Set algebra over integer grids using a foreground predicate.
 * Writes 1 for foreground, 0 for background.
 */
public final class GridSets {
    private GridSets() {}

    public static void union(ReadableGrid3i a, ReadableGrid3i b, IntPredicate fg, WritableGrid3i out) {
        scan2(a, b, (va, vb) -> fg.test(va) || fg.test(vb), out);
    }
    public static void intersect(ReadableGrid3i a, ReadableGrid3i b, IntPredicate fg, WritableGrid3i out) {
        scan2(a, b, (va, vb) -> fg.test(va) && fg.test(vb), out);
    }
    public static void subtract(ReadableGrid3i a, ReadableGrid3i b, IntPredicate fg, WritableGrid3i out) {
        scan2(a, b, (va, vb) -> fg.test(va) && !fg.test(vb), out);
    }
    public static void invert(ReadableGrid3i a, IntPredicate fg, WritableGrid3i out) {
        scan1(a, v -> !fg.test(v), out);
    }

    private interface Bin { boolean apply(int a, int b); }
    private interface Uni { boolean apply(int a); }

    private static void scan2(ReadableGrid3i a, ReadableGrid3i b, Bin fn, WritableGrid3i out) {
        int w=a.width(), h=a.height(), d=a.depth();
        for (int z=0; z<d; z++)
            for (int y=0; y<h; y++)
                for (int x=0; x<w; x++)
                    out.set(x,y,z, fn.apply(a.get(x,y,z), b.get(x,y,z)) ? 1 : 0);
    }
    private static void scan1(ReadableGrid3i a, Uni fn, WritableGrid3i out) {
        int w=a.width(), h=a.height(), d=a.depth();
        for (int z=0; z<d; z++)
            for (int y=0; y<h; y++)
                for (int x=0; x<w; x++)
                    out.set(x,y,z, fn.apply(a.get(x,y,z)) ? 1 : 0);
    }
}