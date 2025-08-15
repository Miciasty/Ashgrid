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
     */
    int fill(ReadableGrid3i grid, int sx, int sy, int sz,
             IntPredicate canVisit, CellConsumer visit);

    @FunctionalInterface
    interface CellConsumer {
        void accept(int x, int y, int z, int value);
    }
}