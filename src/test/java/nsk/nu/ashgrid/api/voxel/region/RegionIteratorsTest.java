package nsk.nu.ashgrid.api.voxel.region;

import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionIteratorsTest {

    @Test
    void box_iterator_visits_all_inclusive_cells() {
        // GIVEN
        final int[] count = {0};

        // WHEN
        RegionIterators.forEachCellBox(0, 0, 0, 1, 1, 1, (x, y, z) -> count[0]++);

        // THEN
        assertEquals(8, count[0]);
    }

    @Test
    void sphere_iterator_contains_center_cell() {
        // GIVEN
        final boolean[] hasCenter = {false};
        Vector3 center = new Vector3(5.0, 5.0, 5.0);

        // WHEN
        RegionIterators.forEachCellSphere(center, 1.0, (x, y, z) -> {
            if (x == 5 && y == 5 && z == 5) hasCenter[0] = true;
        });

        // THEN
        assertTrue(hasCenter[0]);
    }

    @Test
    void cylinder_iterator_visits_expected_vertical_span() {
        // GIVEN
        final int[] minY = {Integer.MAX_VALUE};
        final int[] maxY = {Integer.MIN_VALUE};
        final int[] visited = {0};

        // WHEN
        RegionIterators.forEachCellCylinderXZ(new Vector3(0.0, 0.0, 0.0), 1.0, -2, 2, (x, y, z) -> {
            visited[0]++;
            minY[0] = Math.min(minY[0], y);
            maxY[0] = Math.max(maxY[0], y);
        });

        // THEN
        assertTrue(visited[0] > 0);
        assertEquals(-2, minY[0]);
        assertEquals(2, maxY[0]);
    }
}
