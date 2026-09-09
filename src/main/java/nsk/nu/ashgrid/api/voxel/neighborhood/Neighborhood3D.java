package nsk.nu.ashgrid.api.voxel.neighborhood;

/**
 * Precomputed neighbor offsets for 3D grids, in the declared array order.
 * Public arrays are retained for compatibility and must be treated as read-only, including rows.
 * Callers needing custom offsets must deep-copy them. Mutation changes later morphology/component
 * operations; concurrent mutation is unsupported. No operation snapshots these arrays.
 */
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
