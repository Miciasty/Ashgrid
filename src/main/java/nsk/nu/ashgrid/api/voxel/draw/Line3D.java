package nsk.nu.ashgrid.api.voxel.draw;

import nsk.nu.ashcore.api.spi.Identified;

/**
 * Traces a discrete 3D line through grid cells.
 */
public interface Line3D extends Identified {

    /**
     * Rasterize from (x0,y0,z0) to (x1,y1,z1), including both endpoint cells.
     * This is a thin discrete line, not every continuous contact; use Line3DSupercover for that.
     * The visitor returns false to stop immediately. A zero-length line emits one cell.
     * bresenham3d selects dominant axes X, Y, Z on ties; reversal may select different tie cells.
     * All signed int endpoints are supported; cost is proportional to the emitted cells.
     */
    void trace(int x0, int y0, int z0, int x1, int y1, int z1, CellVisitor visitor);

    @FunctionalInterface
    interface CellVisitor {
        boolean visit(int x, int y, int z);
    }
}
