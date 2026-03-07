package nsk.nu.ashgrid.api.grid.bounds;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntBoundsTest {

    @Test
    void int_box_intersection_and_contains_follow_half_open_contract() {
        // GIVEN
        IntBox3 a = new IntBox3(0, 0, 0, 4, 4, 4);
        IntBox3 b = new IntBox3(2, 2, 2, 6, 6, 6);

        // WHEN
        IntBox3 i = a.intersect(b);

        // THEN
        assertEquals(new IntBox3(2, 2, 2, 4, 4, 4), i);
        assertTrue(i.contains(2, 2, 2));
        assertFalse(i.contains(4, 4, 4));
    }

    @Test
    void int_rect_reports_empty_only_when_range_is_degenerate() {
        // GIVEN
        IntRect2 nonEmpty = new IntRect2(1, 1, 3, 3);
        IntRect2 empty = new IntRect2(2, 2, 2, 2);

        // WHEN / THEN
        assertFalse(nonEmpty.empty());
        assertTrue(empty.empty());
    }
}
