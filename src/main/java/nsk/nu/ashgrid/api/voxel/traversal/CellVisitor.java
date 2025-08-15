package nsk.nu.ashgrid.api.voxel.traversal;

/**
 * Callback invoked for each voxel visited by a voxel traversal algorithm.
 * Implementations should return {@code true} to continue traversal or {@code false} to stop early.
 */
@FunctionalInterface
public interface CellVisitor {
    /**
     * @param x      voxel X coordinate
     * @param y      voxel Y coordinate
     * @param z      voxel Z coordinate
     * @param tEnter ray parameter where the ray enters this voxel (inclusive)
     * @param tExit  ray parameter where the ray exits this voxel (exclusive)
     * @return {@code true} to continue traversal, {@code false} to stop
     */
    boolean visit(int x, int y, int z, double tEnter, double tExit);
}
