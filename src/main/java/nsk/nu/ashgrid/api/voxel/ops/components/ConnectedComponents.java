package nsk.nu.ashgrid.api.voxel.ops.components;

import nsk.nu.ashcore.api.spi.Identified;
import nsk.nu.ashgrid.api.raster.Grid3i;

import java.util.function.IntPredicate;

/**
 * Labels connected components in a 3D integer grid using an integer output grid.
 * Foreground is defined by {@code isForeground(value)}.
 */
public interface ConnectedComponents extends Identified {

    /**
     * Labels foreground components into {@code labelsOut}. Background cells receive 0.
     * Returns last label used (0 if none).
     * Source and labels must have equal positive dimensions and int-sized volume, with disjoint storage.
     * The output is cleared first. Source, predicate and neighborhood arrays must remain stable.
     * ConnectedComponentsBFS assigns labels from 1 in x-fastest, then Y, then Z scan order.
     */
    int label(Grid3i src, IntPredicate isForeground, Grid3i labelsOut, Neighborhood neighborhood);

    enum Neighborhood { N6, N18, N26 }
}
