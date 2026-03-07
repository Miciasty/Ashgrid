package nsk.nu.ashgrid.api.raster;

import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.raster.util.GridMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GridMathTest {

    @Test
    void linear_index_and_unindex_are_inverse_for_valid_coordinates() {
        // GIVEN
        int w = 7, h = 5;
        int x = 3, y = 4, z = 2;

        // WHEN
        int i = GridMath.linearIndex(x, y, z, w, h);
        int[] xyz = GridMath.unindex(i, w, h);

        // THEN
        assertArrayEquals(new int[]{x, y, z}, xyz);
    }

    @Test
    void world_to_cell_uses_floor_semantics() {
        // GIVEN
        Vector3 p = new Vector3(1.9, -1.1, 0.0);

        // WHEN / THEN
        assertEquals(1, GridMath.cellX(p));
        assertEquals(-2, GridMath.cellY(p));
        assertEquals(0, GridMath.cellZ(p));
    }

    @Test
    void cell_center_is_center_of_unit_voxel() {
        // GIVEN / WHEN
        Vector3 c = GridMath.cellCenter(2, 3, 4);

        // THEN
        assertEquals(2.5, c.x(), 1e-12);
        assertEquals(3.5, c.y(), 1e-12);
        assertEquals(4.5, c.z(), 1e-12);
    }
}
