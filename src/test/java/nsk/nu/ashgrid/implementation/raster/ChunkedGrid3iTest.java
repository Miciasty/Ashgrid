package nsk.nu.ashgrid.implementation.raster;

import nsk.nu.ashgrid.implementation.raster.chunked.ChunkedGrid3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkedGrid3iTest {

    @Test
    void write_and_read_far_cell_materializes_chunk() {
        // GIVEN
        ChunkedGrid3i g = new ChunkedGrid3i(16, 16, 16);

        // WHEN
        g.set(100, 0, 0, 7);
        int v = g.get(100, 0, 0);

        // THEN
        assertEquals(7, v);
        assertTrue(g.has(100, 0, 0));
    }

    @Test
    void missing_cell_returns_default_value() {
        // GIVEN
        ChunkedGrid3i g = new ChunkedGrid3i(16, 16, 16, -1);

        // WHEN
        int v = g.get(999, -20, 4);

        // THEN
        assertEquals(-1, v);
        assertEquals(-1, g.defaultValue());
    }

    @Test
    void non_zero_default_is_preserved_inside_materialized_chunk() {
        // GIVEN
        ChunkedGrid3i g = new ChunkedGrid3i(8, 8, 8, -3);

        // WHEN
        g.set(0, 0, 0, 11);

        // THEN
        assertEquals(11, g.get(0, 0, 0));
        assertEquals(-3, g.get(1, 0, 0));
        assertTrue(g.has(1, 0, 0));
    }
}
