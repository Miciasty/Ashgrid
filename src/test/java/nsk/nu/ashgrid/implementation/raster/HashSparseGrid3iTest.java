package nsk.nu.ashgrid.implementation.raster;

import nsk.nu.ashgrid.implementation.raster.sparse.HashSparseGrid3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashSparseGrid3iTest {

    @Test
    void missing_cells_return_default_value() {
        // GIVEN
        HashSparseGrid3i g = new HashSparseGrid3i(-5);

        // WHEN
        int v = g.get(40, -7, 3);

        // THEN
        assertEquals(-5, v);
        assertFalse(g.has(40, -7, 3));
    }

    @Test
    void writing_default_value_removes_explicit_cell() {
        // GIVEN
        HashSparseGrid3i g = new HashSparseGrid3i(0);
        g.set(1, 2, 3, 9);
        assertTrue(g.has(1, 2, 3));

        // WHEN
        g.set(1, 2, 3, 0);

        // THEN
        assertFalse(g.has(1, 2, 3));
        assertEquals(0, g.get(1, 2, 3));
        assertFalse(g.has(2, 2, 3));
    }
}
