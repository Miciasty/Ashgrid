package nsk.nu.ashgrid.api.voxel.draw;

import nsk.nu.ashcore.api.spi.Identified;

/**
 * Supercover 3D line: visits all voxels touched by the continuous line segment.
 * Useful for robust collision checks on thin geometry.
 */
public interface Line3DSupercover extends Identified {
    void trace(int x0,int y0,int z0, int x1,int y1,int z1, CellVisitor v);

    @FunctionalInterface interface CellVisitor { boolean visit(int x,int y,int z); }
}