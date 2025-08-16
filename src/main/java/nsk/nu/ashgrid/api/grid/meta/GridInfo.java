package nsk.nu.ashgrid.api.grid.meta;

import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.grid.indexing.ChunkScheme;
import nsk.nu.ashgrid.api.voxel.space.VoxelSpace;

/** Bundle of grid metadata: space, chunk scheme, defined extent, default value. */
public record GridInfo(VoxelSpace space, ChunkScheme chunks, IntBox3 extent, int defaultValue) {
    public GridInfo {
        if (space == null || chunks == null) throw new IllegalArgumentException("space/chunks required");
    }
}