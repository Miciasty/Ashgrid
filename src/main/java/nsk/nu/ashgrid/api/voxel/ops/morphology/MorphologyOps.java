package nsk.nu.ashgrid.api.voxel.ops.morphology;

import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.WritableGrid3i;

import java.util.function.IntPredicate;

/**
 * Convenience composition helpers for morphology.
 */
public final class MorphologyOps {
    private MorphologyOps() {}

    /** Opening = erode then dilate. tmp must be a read/write buffer. */
    public static void open(Morphology m,
                            ReadableGrid3i src,
                            IntPredicate fg,
                            Grid3i tmp,
                            Grid3i dst,
                            Morphology.Neighborhood nh) {
        m.erode(src, fg, tmp, nh);
        m.dilate(tmp, fg, dst, nh);
    }

    /** Closing = dilate then erode. tmp must be a read/write buffer. */
    public static void close(Morphology m,
                             ReadableGrid3i src,
                             IntPredicate fg,
                             Grid3i tmp,
                             Grid3i dst,
                             Morphology.Neighborhood nh) {
        m.dilate(src, fg, tmp, nh);
        m.erode(tmp, fg, dst, nh);
    }

    /** n× dilation with two buffers; result guaranteed in dst. */
    public static void dilateN(Morphology m,
                               ReadableGrid3i src,
                               IntPredicate fg,
                               Grid3i tmp,
                               Grid3i dst,
                               Morphology.Neighborhood nh,
                               int n) {
        if (n <= 0) { copy(src, dst); return; }
        m.dilate(src, fg, dst, nh);
        if (n == 1) return;

        Grid3i a = dst;
        Grid3i b = tmp;
        for (int i = 1; i < n; i++) {
            m.dilate(a, fg, b, nh);
            Grid3i t = a; a = b; b = t;
        }
        if (a != dst) copy(a, dst);
    }

    /** n× erosion with two buffers; result guaranteed in dst. */
    public static void erodeN(Morphology m,
                              ReadableGrid3i src,
                              IntPredicate fg,
                              Grid3i tmp,
                              Grid3i dst,
                              Morphology.Neighborhood nh,
                              int n) {
        if (n <= 0) { copy(src, dst); return; }
        m.erode(src, fg, dst, nh);
        if (n == 1) return;

        Grid3i a = dst;
        Grid3i b = tmp;
        for (int i = 1; i < n; i++) {
            m.erode(a, fg, b, nh);
            Grid3i t = a; a = b; b = t;
        }
        if (a != dst) copy(a, dst);
    }

    private static void copy(ReadableGrid3i a, Grid3i b) {
        for (int z = 0; z < a.depth(); z++)
            for (int y = 0; y < a.height(); y++)
                for (int x = 0; x < a.width(); x++)
                    b.set(x, y, z, a.get(x, y, z));
    }
}