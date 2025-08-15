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
     * Checks if the segment [a,b] is free of occluders (strictly inside, excluding the start cell).
     */
    public boolean clear(Vector3 a, Vector3 b, Occluder occ) {
        Vector3 dir = b.sub(a);
        double len = dir.length();
        if (len == 0) return true;
        Ray ray = new Ray(a, dir.mul(1.0 / len));
        final boolean[] blocked = {false};
        traverser.traverse(ray, Math.nextDown(len), (x,y,z,t0,t1) -> {
            if (occ.blocks(x,y,z)) { blocked[0] = true; return false; }
            return true;
        });
        return !blocked[0];
    }
}