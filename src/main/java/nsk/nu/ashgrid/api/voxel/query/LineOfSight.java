package nsk.nu.ashgrid.api.voxel.query;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

/**
 * Line-of-sight check using voxel traversal.
 */
public final class LineOfSight {
    private final VoxelTraverser traverser;

    @FunctionalInterface
    public interface Occluder {
        /** @return true if the cell blocks the line of sight. */
        boolean blocks(int x, int y, int z);
    }

    public LineOfSight(VoxelTraverser traverser) { this.traverser = traverser; }

    /**
     * Checks [a,b), excluding the first callback cell, including subsequent zero-length visits.
     * Points use unit-grid coordinates; equal finite points are clear without callbacks.
     * Occluders must remain stable. Boundary ties follow the selected traverser.
     */
    public boolean clear(Vector3 a, Vector3 b, Occluder occ) {
        Vector3 dir = b.sub(a);
        if (!Double.isFinite(a.x()) || !Double.isFinite(a.y()) || !Double.isFinite(a.z())
                || !Double.isFinite(b.x()) || !Double.isFinite(b.y()) || !Double.isFinite(b.z()))
            throw new IllegalArgumentException("endpoints must be finite");
        double len = Math.hypot(Math.hypot(dir.x(), dir.y()), dir.z());
        if (!Double.isFinite(len)) throw new IllegalArgumentException("segment length must be finite");
        if (len == 0) return true;
        Ray ray = new Ray(a, new Vector3(dir.x()/len, dir.y()/len, dir.z()/len));
        final boolean[] blocked = {false};
        final boolean[] firstCell = {true};
        traverser.traverse(ray, len, (x,y,z,t0,t1) -> {
            if (firstCell[0]) {
                firstCell[0] = false;
                return true;
            }
            if (occ.blocks(x,y,z)) { blocked[0] = true; return false; }
            return true;
        });
        return !blocked[0];
    }
}
