package nsk.nu.ashgrid.api.raster;

import nsk.nu.ashgrid.api.raster.ops.GridSets;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GridSetsTest {

    @Test
    void union_intersect_subtract_and_invert_follow_boolean_set_logic() {
        // GIVEN
        ArrayGrid3i a = new ArrayGrid3i(3, 1, 1);
        ArrayGrid3i b = new ArrayGrid3i(3, 1, 1);
        a.set(0, 0, 0, 1);
        b.set(1, 0, 0, 1);
        IntPredicate fg = v -> v == 1;

        ArrayGrid3i outUnion = new ArrayGrid3i(3, 1, 1);
        ArrayGrid3i outIntersect = new ArrayGrid3i(3, 1, 1);
        ArrayGrid3i outSubtract = new ArrayGrid3i(3, 1, 1);
        ArrayGrid3i outInvert = new ArrayGrid3i(3, 1, 1);

        // WHEN
        GridSets.union(a, b, fg, outUnion);
        GridSets.intersect(a, b, fg, outIntersect);
        GridSets.subtract(a, b, fg, outSubtract);
        GridSets.invert(a, fg, outInvert);

        // THEN
        assertEquals(1, outUnion.get(0, 0, 0));
        assertEquals(1, outUnion.get(1, 0, 0));
        assertEquals(0, outUnion.get(2, 0, 0));

        assertEquals(0, outIntersect.get(0, 0, 0));
        assertEquals(0, outIntersect.get(1, 0, 0));

        assertEquals(1, outSubtract.get(0, 0, 0));
        assertEquals(0, outSubtract.get(1, 0, 0));

        assertEquals(0, outInvert.get(0, 0, 0));
        assertEquals(1, outInvert.get(1, 0, 0));
        assertEquals(1, outInvert.get(2, 0, 0));
    }

    @Test
    void binary_set_ops_reject_mismatched_input_dimensions() {
        // GIVEN
        ArrayGrid3i a = new ArrayGrid3i(3, 1, 1);
        ArrayGrid3i b = new ArrayGrid3i(2, 1, 1);
        ArrayGrid3i out = new ArrayGrid3i(3, 1, 1);
        IntPredicate fg = v -> v == 1;

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> GridSets.union(a, b, fg, out));
        assertThrows(IllegalArgumentException.class, () -> GridSets.intersect(a, b, fg, out));
        assertThrows(IllegalArgumentException.class, () -> GridSets.subtract(a, b, fg, out));
    }
}
