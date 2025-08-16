package nsk.nu.ashgrid.api.voxel.ops.morphology;

import nsk.nu.ashcore.api.spi.Identified;
import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.WritableGrid3i;

import java.util.function.IntPredicate;

/** Binary morphology (dilate/erode) on integer grids with predicate-based foreground. */
public interface Morphology extends Identified {
    void dilate(ReadableGrid3i src, IntPredicate isForeground, WritableGrid3i dst, Neighborhood nh);
    void erode(ReadableGrid3i src, IntPredicate isForeground, WritableGrid3i dst, Neighborhood nh);
    enum Neighborhood { N6, N18, N26 }
}