package nsk.nu.ashgrid.api.voxel.neighborhood;

/** Precomputed neighbor offsets for 3D grids. */
public final class Neighborhood3D {
    private Neighborhood3D() {}

    /** 6-neighborhood (faces). */
    public static final int[][] N6 = {
            { 1, 0, 0}, {-1, 0, 0},
            { 0, 1, 0}, { 0,-1, 0},
            { 0, 0, 1}, { 0, 0,-1}
    };

    /** 26-neighborhood (faces + edges + corners). */
    public static final int[][] N26 = {
            // z = -1
            {-1,-1,-1}, { 0,-1,-1}, { 1,-1,-1},
            {-1, 0,-1}, { 0, 0,-1}, { 1, 0,-1},
            {-1, 1,-1}, { 0, 1,-1}, { 1, 1,-1},

            {-1,-1, 0}, { 0,-1, 0}, { 1,-1, 0},
            {-1, 0, 0},             { 1, 0, 0},
            {-1, 1, 0}, { 0, 1, 0}, { 1, 1, 0},

            {-1,-1, 1}, { 0,-1, 1}, { 1,-1, 1},
            {-1, 0, 1}, { 0, 0, 1}, { 1, 0, 1},
            {-1, 1, 1}, { 0, 1, 1}, { 1, 1, 1}
    };

    /** 18-neighborhood (faces + edges, without 8 corners). */
    public static final int[][] N18 = {
            { 0,-1,-1},
            {-1, 0,-1}, { 0, 0,-1}, { 1, 0,-1},
            { 0, 1,-1},

            {-1,-1, 0}, { 0,-1, 0}, { 1,-1, 0},
            {-1, 0, 0},             { 1, 0, 0},
            {-1, 1, 0}, { 0, 1, 0}, { 1, 1, 0},

            { 0,-1, 1},
            {-1, 0, 1}, { 0, 0, 1}, { 1, 0, 1},
            { 0, 1, 1}
    };
}