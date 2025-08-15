package nsk.nu.ashgrid.api.voxel.ops.components;

import nsk.nu.ashgrid.api.raster.Grid3i;

import java.util.function.IntPredicate;

/**
 * Labels connected components in a 3D integer grid using an integer output grid.
 * Foreground is defined by {@code isForeground(value)}.
 */
public interface ConnectedComponents {

    /**
     * Labels foreground components into {@code labelsOut}, starting from {@code startLabel+1}.
     * Background cells receive 0. Returns the last label used (0 if none).
     */
    int label(Grid3i src, IntPredicate isForeground, Grid3i labelsOut, Neighborhood neighborhood);

    enum Neighborhood { N6, N18, N26 }
}