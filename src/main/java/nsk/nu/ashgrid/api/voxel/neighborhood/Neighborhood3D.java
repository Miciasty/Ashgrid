package nsk.nu.ashgrid.api.voxel.neighborhood;

/** Precomputed neighbor offsets for 3D grids. */
public final class Neighborhood3D {
    private Neighborhood3D() {}
    public static final int[][] N6  = { {1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1} };
    public static final int[][] N18 = {/* N6 plus edge-sharing neighbors */};
    public static final int[][] N26 = {/* N18 plus corner-sharing neighbors */};
}