package nsk.nu.ashgrid.api.voxel.draw;

import nsk.nu.ashcore.api.spi.Identified;

/**
 * Supercover 3D line between the centers of the integer endpoint cells.
 * Visits all closed unit voxels touched by the segment, including edge/corner contacts and endpoints.
 * A zero-length line visits one cell; false stops immediately. All signed int endpoints are supported.
 * supercover3d orders simultaneous contacts by subset masks X=1,Y=2,Z=4, ascending 1..7.
 * Each cell is emitted once. Reversing endpoints preserves the set, not necessarily the reverse order.
 */
public interface Line3DSupercover extends Identified {
    void trace(int x0,int y0,int z0, int x1,int y1,int z1, CellVisitor v);

    @FunctionalInterface interface CellVisitor { boolean visit(int x,int y,int z); }
}
