package nsk.nu.ashgrid.integration.voxel;

import nsk.nu.ashgrid.api.raster.view.ClampedGrid3i;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.api.voxel.ops.morphology.MorphologyOps;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.components.ConnectedComponentsBFS;
import nsk.nu.ashgrid.implementation.voxel.ops.floodfill.FloodFillQueue;
import nsk.nu.ashgrid.implementation.voxel.ops.morphology.MorphologyBasic;
import org.junit.jupiter.api.Test;

import static nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents.Neighborhood.N6;
import static org.junit.jupiter.api.Assertions.*;

class OperationContractTest {
    @Test
    void flood_fill_on_clamped_view_stays_in_declared_volume() {
        // GIVEN / WHEN
        var grid = new ClampedGrid3i(new ArrayGrid3i(2,2,2));
        int count = new FloodFillQueue().fill(grid,0,0,0,v -> true,(x,y,z,v) -> {});
        // THEN
        assertEquals(8,count);
        assertEquals(0,new FloodFillQueue().fill(grid,-1,0,0,v -> true,(x,y,z,v) -> fail()));
    }

    @Test
    void output_shape_and_direct_alias_are_rejected_before_mutation() {
        // GIVEN
        var source = new ArrayGrid3i(2,2,2); source.set(0,0,0,7);
        var smaller = new ArrayGrid3i(1,1,1); smaller.set(0,0,0,9);
        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> new ConnectedComponentsBFS().label(source,v -> v==7,source,N6));
        assertThrows(IllegalArgumentException.class, () -> new ConnectedComponentsBFS().label(source,v -> v==7,smaller,N6));
        assertThrows(IllegalArgumentException.class, () -> new MorphologyBasic().dilate(source,v -> v==7,smaller,Morphology.Neighborhood.N6));
        assertEquals(7,source.get(0,0,0)); assertEquals(9,smaller.get(0,0,0));
    }

    @Test
    void repeated_morphology_classifies_intermediate_binary_masks_as_binary() {
        // GIVEN
        var source = new ArrayGrid3i(5,1,1); source.set(2,0,0,7);
        var tmp = new ArrayGrid3i(5,1,1); var dst = new ArrayGrid3i(5,1,1);
        // WHEN
        MorphologyOps.dilateN(new MorphologyBasic(),source,v -> v==7,tmp,dst,Morphology.Neighborhood.N6,2);
        // THEN
        for (int x=0;x<5;x++) assertEquals(1,dst.get(x,0,0));
    }
}
