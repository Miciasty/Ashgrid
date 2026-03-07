package nsk.nu.ashgrid.integration.grid;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.implementation.grid.indexing.SquareXZChunkScheme;
import nsk.nu.ashgrid.implementation.voxel.region.AABBVoxelIterator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkSchemeAndRegionIntegrationTest {

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
        assertEquals(0.0, bounds.min().y());
        assertEquals(32.0, bounds.min().z());
        assertEquals(1.0, bounds.max().y());
    }

    @Test
    void chunks_and_cells_ranges_are_half_open() {
        // GIVEN
        var scheme = new SquareXZChunkScheme(16);
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(32, 2, 32));

        // WHEN
        var chunks = scheme.chunksInAABB(box);
        var cells = scheme.cellsInAABB(box);

        // THEN
        assertEquals(0, chunks.cx0());
        assertEquals(0, chunks.cz0());
        assertEquals(2, chunks.cx1());
        assertEquals(2, chunks.cz1());

        assertEquals(0, cells.minX());
        assertEquals(0, cells.minY());
        assertEquals(0, cells.minZ());
        assertEquals(32, cells.maxX());
        assertEquals(2, cells.maxY());
        assertEquals(32, cells.maxZ());
    }

    @Test
    void zero_thickness_aabb_produces_empty_ranges() {
        // GIVEN
        var scheme = new SquareXZChunkScheme(16);
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(5, 2, 9), new Vector3(5, 2, 9));

        // WHEN
        var chunks = scheme.chunksInAABB(box);
        var cells = scheme.cellsInAABB(box);

        // THEN
        assertTrue(chunks.empty());
        assertTrue(cells.empty());
    }

    @Test
    void aabb_iterator_visits_expected_number_of_cells() {
        // GIVEN
        var it = new AABBVoxelIterator();
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(2, 2, 2));
        final int[] count = {0};

        // WHEN
        it.forEachCell(box, (x, y, z) -> count[0]++);

        // THEN
        assertEquals(8, count[0]);
    }

    @Test
    void non_finite_aabb_is_rejected_by_range_helpers() {
        // GIVEN
        var scheme = new SquareXZChunkScheme(16);
        AxisAlignedBox box = new AxisAlignedBox(
                new Vector3(0, Double.NEGATIVE_INFINITY, 0),
                new Vector3(10, Double.POSITIVE_INFINITY, 10)
        );

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> scheme.cellsInAABB(box));
    }

    @Test
    void chunk_bounds_are_compatible_with_cells_in_aabb() {
        // GIVEN
        var scheme = new SquareXZChunkScheme(16);
        AxisAlignedBox bounds = scheme.chunkBounds(new nsk.nu.ashgrid.api.grid.indexing.ChunkIndex2(3, -2));

        // WHEN
        var cells = scheme.cellsInAABB(bounds);

        // THEN
        assertFalse(cells.empty());
        assertEquals(48, cells.minX());
        assertEquals(-32, cells.minZ());
        assertEquals(64, cells.maxX());
        assertEquals(-16, cells.maxZ());
    }
}
