package nsk.nu.ashgrid;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.implementation.grid.indexing.SquareXZChunkScheme;
import nsk.nu.ashgrid.implementation.voxel.region.AABBVoxelIterator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
chunk size = 16
point (17,*,33) -> chunk (1,2)
AABB [0,0,0]..[2,2,2) -> 2×2×2 = 8 cells
*/
class ChunkSchemeAndRegionTest {

    @Test
    void point_maps_to_expected_chunk_and_bounds() {
        // GIVEN
        var scheme = new SquareXZChunkScheme(16);
        Vector3 p = new Vector3(17, 0, 33);

        // WHEN
        var c = scheme.chunkOfPoint(p);
        var bounds = scheme.chunkBounds(c);

        // THEN
        assertEquals(1, c.cx());
        assertEquals(2, c.cz());
        assertEquals(16.0, bounds.min().x());
        assertEquals(32.0, bounds.min().z());
    }

    @Test void aabb_iterator_visits_expected_number_of_cells() {
        // GIVEN
        var it = new AABBVoxelIterator();
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0,0,0), new Vector3(2,2,2));
        final int[] count = {0};

        // WHEN
        it.forEachCell(box, (x,y,z) -> count[0]++);

        // THEN
        assertEquals(8, count[0]);
    }
}