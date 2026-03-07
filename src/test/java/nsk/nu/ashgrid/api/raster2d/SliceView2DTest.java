package nsk.nu.ashgrid.api.raster2d;

import nsk.nu.ashgrid.api.raster2d.view.SliceView2D;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SliceView2DTest {

    @Test
    void xy_slice_maps_to_fixed_z_plane() {
        // GIVEN
        ArrayGrid3i src = new ArrayGrid3i(4, 3, 2);
        SliceView2D slice = new SliceView2D(src, SliceView2D.Plane.XY, 1);

        // WHEN
        slice.set(2, 1, 7);

        // THEN
        assertEquals(7, src.get(2, 1, 1));
        assertEquals(4, slice.width());
        assertEquals(3, slice.height());
        assertTrue(slice.inside(2, 1));
    }

    @Test
    void yz_slice_maps_to_fixed_x_plane() {
        // GIVEN
        ArrayGrid3i src = new ArrayGrid3i(4, 3, 2);
        SliceView2D slice = new SliceView2D(src, SliceView2D.Plane.YZ, 2);

        // WHEN
        slice.set(1, 0, 9);

        // THEN
        assertEquals(9, src.get(2, 1, 0));
        assertEquals(3, slice.width());
        assertEquals(2, slice.height());
    }
}
