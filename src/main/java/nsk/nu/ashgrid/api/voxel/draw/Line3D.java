package nsk.nu.ashgrid.api.voxel.draw;

import nsk.nu.ashcore.api.spi.Identified;

/**
 * Traces a discrete 3D line through grid cells.
 */
public interface Line3D extends Identified {

    /**
     * Visit all cells intersected by a discrete line from (x0,y0,z0) to (x1,y1,z1).
     * The visitor returns {@code false} to stop early.
     */
    void trace(int x0, int y0, int z0, int x1, int y1, int z1, CellVisitor visitor);

    @FunctionalInterface
    interface CellVisitor {
        boolean visit(int x, int y, int z);
    }
}