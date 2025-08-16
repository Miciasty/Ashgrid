package nsk.nu.ashgrid;

import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.util.GridServices;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.api.voxel.ops.floodfill.FloodFill;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Row y=1: 0 1 0 (seed at x=1 fills cross of 1s)
Fill count should be 1 (only the seed) for predicate v==1 unless neighbors also 1.
Components: two isolated voxels -> 2 labels.
*/
class FloodFillAndComponentsTest {

    @Test
    void flood_fill_counts_region() {
        // GIVEN
        ArrayGrid3i g = new ArrayGrid3i(3,1,1);
        g.set(1,0,0, 1);
        FloodFill fill = GridServices.require(FloodFill.class, "floodfill-queue");
        IntPredicate isOne = v -> v == 1;
        final int[] cnt = {0};

        // WHEN
        int visited = fill.fill(g, 1,0,0, isOne, (x,y,z,val) -> cnt[0]++);

        // THEN
        assertEquals(1, visited);
        assertEquals(1, cnt[0]);
    }

    @Test void connected_components_labels_two_islands() {
        // GIVEN
        ArrayGrid3i src = new ArrayGrid3i(3,1,1);
        src.set(0,0,0, 1);
        src.set(2,0,0, 1);
        Grid3i labels = new ArrayGrid3i(3,1,1);
        ConnectedComponents cc = GridServices.require(ConnectedComponents.class, "ConnectedComponentsBFS");
        IntPredicate fg = v -> v == 1;

        // WHEN
        int last = cc.label(src, fg, labels, ConnectedComponents.Neighborhood.N6);

        // THEN
        assertEquals(2, last);
        assertTrue(labels.get(0,0,0) != labels.get(2,0,0));
    }
}