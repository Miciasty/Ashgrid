package nsk.nu.ashgrid.api.voxel.ops.morphology;

import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.WritableGrid3i;

import java.util.function.IntPredicate;

/**
 * Binary morphology (dilate/erode) on integer grids with predicate-based foreground.
 */
public interface Morphology {

    /** One-step dilation with given neighborhood. */
    void dilate(ReadableGrid3i src, IntPredicate isForeground, WritableGrid3i dst, Neighborhood nh);

    /** One-step erosion with given neighborhood. */
    void erode(ReadableGrid3i src, IntPredicate isForeground, WritableGrid3i dst, Neighborhood nh);

    enum Neighborhood { N6, N18, N26 }
}