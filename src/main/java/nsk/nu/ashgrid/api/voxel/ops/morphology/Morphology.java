package nsk.nu.ashgrid.api.voxel.ops.morphology;

import nsk.nu.ashcore.api.spi.Identified;
import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.WritableGrid3i;

import java.util.function.IntPredicate;

/**
 * Binary morphology (dilate/erode) with predicate-based foreground, writing only 0/1.
 * Source dimensions must be positive with int-sized volume. Output must accept the entire shape;
 * readable outputs are shape-checked. Source and output must not share storage, including views.
 * Sources, predicates and neighborhood arrays must remain stable during the operation.
 * Providers scan x-fastest, then Y, then Z; erosion treats cells outside src.inside as background.
 */
public interface Morphology extends Identified {
    void dilate(ReadableGrid3i src, IntPredicate isForeground, WritableGrid3i dst, Neighborhood nh);
    void erode(ReadableGrid3i src, IntPredicate isForeground, WritableGrid3i dst, Neighborhood nh);
    enum Neighborhood { N6, N18, N26 }
}
