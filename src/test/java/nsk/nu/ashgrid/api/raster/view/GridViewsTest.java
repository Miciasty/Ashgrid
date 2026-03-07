package nsk.nu.ashgrid.api.raster.view;

import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridViewsTest {

    @Test
    void subgrid_maps_coordinates_with_offset() {
        // GIVEN
        ArrayGrid3i src = new ArrayGrid3i(5, 5, 5);
        src.set(2, 2, 2, 9);
        SubGrid3i sub = new SubGrid3i(src, 1, 1, 1, 3, 3, 3);

        // WHEN
        int v = sub.get(1, 1, 1);
        sub.set(0, 0, 0, 7);

        // THEN
        assertEquals(9, v);
        assertEquals(7, src.get(1, 1, 1));
    }

    @Test
    void clamped_view_reads_and_writes_at_borders_for_out_of_range_coordinates() {
        // GIVEN
        ArrayGrid3i src = new ArrayGrid3i(2, 2, 2);
        src.set(0, 0, 0, 1);
        src.set(1, 1, 1, 5);
        ClampedGrid3i clamped = new ClampedGrid3i(src);

        // WHEN
        int v = clamped.get(9, 9, 9);
        clamped.set(-10, -10, -10, 3);

        // THEN
        assertEquals(5, v);
        assertEquals(3, src.get(0, 0, 0));
        assertTrue(clamped.inside(999, -20, 7));
    }

    @Test
    void masked_view_writes_only_where_mask_is_foreground() {
        // GIVEN
        ArrayGrid3i src = new ArrayGrid3i(3, 1, 1);
        ArrayGrid3i mask = new ArrayGrid3i(3, 1, 1);
        mask.set(1, 0, 0, 1);
        MaskedGrid3i masked = new MaskedGrid3i(src, mask, v -> v == 1);

        // WHEN
        masked.set(0, 0, 0, 8);
        masked.set(1, 0, 0, 8);

        // THEN
        assertEquals(0, src.get(0, 0, 0));
        assertEquals(8, src.get(1, 0, 0));
    }

    @Test
    void const_view_blocks_writes() {
        // GIVEN
        ArrayGrid3i src = new ArrayGrid3i(2, 2, 2);
        ConstGrid3i c = new ConstGrid3i(src);

        // WHEN / THEN
        assertThrows(UnsupportedOperationException.class, () -> c.set(0, 0, 0, 1));
    }
}
