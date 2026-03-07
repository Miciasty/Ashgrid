package nsk.nu.ashgrid.api.grid.meta;

import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.voxel.space.VoxelSpace;
import nsk.nu.ashgrid.implementation.grid.indexing.SquareXZChunkScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GridInfoTest {

    @Test
    void null_space_or_chunk_scheme_is_rejected() {
        // GIVEN
        VoxelSpace space = new VoxelSpace(1.0, Vector3.ZERO);
        SquareXZChunkScheme chunks = new SquareXZChunkScheme(16);
        IntBox3 extent = new IntBox3(0, 0, 0, 16, 16, 16);

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> new GridInfo(null, chunks, extent, 0));
        assertThrows(IllegalArgumentException.class, () -> new GridInfo(space, null, extent, 0));
    }
}
