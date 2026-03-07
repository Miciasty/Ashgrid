package nsk.nu.ashgrid;

import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.draw.Line3D;
import nsk.nu.ashgrid.api.voxel.draw.Line3DSupercover;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Bresenham 3D: (0,0,0) -> (3,0,0)
●●●● along +X, 4 cells
Diagonal (0,0,0) -> (3,3,0):
- Bresenham hits 4 cells
- Supercover hits ≥ 6 (touching both axes)
*/
class LineRasterTest {

    @Test
    void bresenham_straight_along_x_visits_4_cells() {
        // GIVEN
        Line3D line = ServiceRegistry.of(Line3D.class).require("bresenham3d");
        final int[] count = {0};

        // WHEN
        line.trace(0,0,0, 3,0,0, (x,y,z) -> { count[0]++; return true; });

        // THEN
        assertEquals(4, count[0]);
    }

    @Test void supercover_hits_more_than_bresenham_on_diagonal() {
        // GIVEN
        Line3D b = ServiceRegistry.of(Line3D.class).require("bresenham3d");
        Line3DSupercover s = ServiceRegistry.of(Line3DSupercover.class).require("supercover3d");
        final int[] nb = {0}, ns = {0};

        // WHEN
        b.trace(0,0,0, 3,3,0, (x,y,z) -> { nb[0]++; return true; });
        s.trace(0,0,0, 3,3,0, (x,y,z) -> { ns[0]++; return true; });

        // THEN
        assertEquals(4, nb[0]);
        assertTrue(ns[0] > nb[0], "Supercover should visit more cells than classic Bresenham");
    }
}
