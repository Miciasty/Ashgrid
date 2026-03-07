package nsk.nu.ashgrid.integration.voxel;

import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.draw.Line3D;
import nsk.nu.ashgrid.api.voxel.draw.Line3DSupercover;
import nsk.nu.ashgrid.api.voxel.ops.distance.DistanceTransform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineAndDistanceIntegrationTest {

    @Test
    void bresenham_straight_line_visits_expected_cell_count() {
        // GIVEN
        Line3D line = ServiceRegistry.of(Line3D.class).require("bresenham3d");
        final int[] count = {0};

        // WHEN
        line.trace(0, 0, 0, 3, 0, 0, (x, y, z) -> {
            count[0]++;
            return true;
        });

        // THEN
        assertEquals(4, count[0]);
    }

    @Test
    void supercover_visits_more_cells_than_classic_bresenham_on_diagonal() {
        // GIVEN
        Line3D b = ServiceRegistry.of(Line3D.class).require("bresenham3d");
        Line3DSupercover s = ServiceRegistry.of(Line3DSupercover.class).require("supercover3d");
        final int[] nb = {0};
        final int[] ns = {0};

        // WHEN
        b.trace(0, 0, 0, 3, 3, 0, (x, y, z) -> { nb[0]++; return true; });
        s.trace(0, 0, 0, 3, 3, 0, (x, y, z) -> { ns[0]++; return true; });

        // THEN
        assertEquals(4, nb[0]);
        assertTrue(ns[0] > nb[0], "Supercover should include extra edge-touching cells");
    }

    @Test
    void chamfer_distance_for_face_neighbor_is_three() {
        // GIVEN
        DistanceTransform dt = ServiceRegistry.of(DistanceTransform.class).require("Chamfer345Distance");
        int w = 3, h = 3, d = 1;
        DistanceTransform.Mask mask = (x, y, z) -> x == 1 && y == 1 && z == 0;
        float[] out = new float[w * h * d];

        // WHEN
        dt.compute(w, h, d, mask, out);

        // THEN
        int idxFace = 1 * w + 0 * (w * h);
        assertEquals(3f, out[idxFace], 1e-5);
    }
}
