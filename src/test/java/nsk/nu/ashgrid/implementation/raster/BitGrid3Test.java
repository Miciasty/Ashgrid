package nsk.nu.ashgrid.implementation.raster;

import nsk.nu.ashgrid.implementation.raster.bitset.BitGrid3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitGrid3Test {

    @Test
    void set_and_get_roundtrip_for_boolean_voxel() {
        // GIVEN
        BitGrid3 g = new BitGrid3(4, 4, 4);

        // WHEN
        g.set(1, 2, 3, true);

        // THEN
        assertTrue(g.get(1, 2, 3));
        assertFalse(g.get(0, 0, 0));
    }

    @Test
    void out_of_bounds_access_throws() {
        // GIVEN
        BitGrid3 g = new BitGrid3(2, 2, 2);

        // WHEN / THEN
        assertThrows(IndexOutOfBoundsException.class, () -> g.get(2, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> g.set(-1, 0, 0, true));
    }
}
