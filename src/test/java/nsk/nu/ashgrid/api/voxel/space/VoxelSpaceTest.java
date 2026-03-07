package nsk.nu.ashgrid.api.voxel.space;

import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VoxelSpaceTest {

    @Test
    void world_to_cell_uses_scale_and_origin_with_floor_behavior() {
        // GIVEN
        VoxelSpace space = new VoxelSpace(2.0, new Vector3(10, 0, -4));
        Vector3 p = new Vector3(13.9, -0.1, -0.1);

        // WHEN / THEN
        assertEquals(1, space.ix(p));
        assertEquals(-1, space.iy(p));
        assertEquals(1, space.iz(p));
    }

    @Test
    void corner_and_center_map_back_to_world_space() {
        // GIVEN
        VoxelSpace space = new VoxelSpace(0.5, new Vector3(1, 2, 3));

        // WHEN
        Vector3 corner = space.corner(2, 4, 6);
        Vector3 center = space.center(2, 4, 6);

        // THEN
        assertEquals(2.0, corner.x(), 1e-12);
        assertEquals(4.0, corner.y(), 1e-12);
        assertEquals(6.0, corner.z(), 1e-12);

        assertEquals(2.25, center.x(), 1e-12);
        assertEquals(4.25, center.y(), 1e-12);
        assertEquals(6.25, center.z(), 1e-12);
    }

    @Test
    void non_positive_scale_is_rejected() {
        // GIVEN / WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> new VoxelSpace(0.0, Vector3.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new VoxelSpace(-1.0, Vector3.ZERO));
    }

    @Test
    void null_origin_is_rejected() {
        // GIVEN / WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> new VoxelSpace(1.0, null));
    }
}
