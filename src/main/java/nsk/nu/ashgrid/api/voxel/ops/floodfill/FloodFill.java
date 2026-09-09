package nsk.nu.ashgrid.api.voxel.ops.floodfill;

import nsk.nu.ashcore.api.spi.Identified;
import nsk.nu.ashgrid.api.raster.ReadableGrid3i;

import java.util.function.IntPredicate;

/**
 * 3D flood fill over an integer grid in 6-neighborhood.
 */
public interface FloodFill extends Identified {

    /**
     * Fill starting at (sx,sy,sz). A cell is visitable if {@code canVisit(value)} is true.
     * The consumer is invoked for each visited cell. Returns number of visited cells.
     * Only [0,width) x [0,height) x [0,depth) is searched, even for clamped views.
     * Dimensions must be positive with int-sized volume; an outside seed returns zero.
     * floodfill-queue uses BFS with +X,-X,+Y,-Y,+Z,-Z neighbor order and O(volume) visited storage.
     * Predicates must be repeatable; callbacks may edit the current cell, but unvisited cells must stay stable.
     */
    int fill(ReadableGrid3i grid, int sx, int sy, int sz,
             IntPredicate canVisit, CellConsumer visit);

    @FunctionalInterface
    interface CellConsumer {
        void accept(int x, int y, int z, int value);
    }
}
