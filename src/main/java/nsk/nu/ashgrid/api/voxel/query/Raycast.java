package nsk.nu.ashgrid.api.voxel.query;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

/**
 * Ray-based queries on voxel grids using a {@link VoxelTraverser}.
 */
public final class Raycast {
    private final VoxelTraverser traverser;

    public record Hit(int x, int y, int z, double tEnter, double tExit) {}

    @FunctionalInterface
    public interface Occupancy {
        /** @return true if the cell is considered "solid" (hit). */
        boolean test(int x, int y, int z);
    }

    public Raycast(VoxelTraverser traverser) { this.traverser = traverser; }

    /**
     * Returns the first occupied callback cell on [0,tMax), or null if none.
     * Includes the starting cell and zero-length visits; ties follow the selected traverser.
     * Occupancy and its grid must remain stable for repeatable results.
     */
    public Hit first(Ray ray, double tMax, Occupancy occ) {
        final Hit[] out = new Hit[1];
        traverser.traverse(ray, tMax, (x,y,z,t0,t1) -> {
            if (occ.test(x,y,z)) { out[0] = new Hit(x,y,z,t0,t1); return false; }
            return true;
        });
        return out[0];
    }
}
