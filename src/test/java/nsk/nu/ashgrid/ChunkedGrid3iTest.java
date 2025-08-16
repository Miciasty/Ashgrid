package nsk.nu.ashgrid;

import nsk.nu.ashgrid.implementation.raster.chunked.ChunkedGrid3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Set (100,0,0)=7 in chunked grid (chunk 16³). Read back same value.
*/
class ChunkedGrid3iTest {

    @Test
    void write_and_read_far_cell_allocates_chunk_and_persists_value() {
        // GIVEN
        ChunkedGrid3i g = new ChunkedGrid3i(16,16,16);

        // WHEN
        g.set(100,0,0, 7);
        int v = g.get(100,0,0);

        // THEN
        assertEquals(7, v);
        assertTrue(g.inside(100,0,0));
    }
}