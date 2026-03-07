package nsk.nu.ashgrid.integration.voxel;

import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.api.voxel.ops.floodfill.FloodFill;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.api.voxel.ops.morphology.MorphologyOps;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MorphologyAndComponentsIntegrationTest {

    @Test
    void dilate_n6_creates_cross_of_five() {
        // GIVEN
        Morphology morph = ServiceRegistry.of(Morphology.class).require("MorphologyBasic");
        ArrayGrid3i src = new ArrayGrid3i(3, 3, 1);
        src.set(1, 1, 0, 1);
        Grid3i dst = new ArrayGrid3i(3, 3, 1);
        IntPredicate fg = v -> v == 1;

        // WHEN
        morph.dilate(src, fg, dst, Morphology.Neighborhood.N6);

        // THEN
        int ones = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (dst.get(x, y, 0) == 1) ones++;
            }
        }
        assertEquals(5, ones);
        assertEquals(1, dst.get(1, 1, 0));
    }

    @Test
    void opening_removes_isolated_noise_and_keeps_connected_cross() {
        // GIVEN
        Morphology morph = ServiceRegistry.of(Morphology.class).require("MorphologyBasic");
        ArrayGrid3i src = new ArrayGrid3i(3, 3, 3);
        src.set(0, 0, 0, 1);
        int cx = 1, cy = 1, cz = 1;
        src.set(cx, cy, cz, 1);
        src.set(cx + 1, cy, cz, 1); src.set(cx - 1, cy, cz, 1);
        src.set(cx, cy + 1, cz, 1); src.set(cx, cy - 1, cz, 1);
        src.set(cx, cy, cz + 1, 1); src.set(cx, cy, cz - 1, 1);

        Grid3i tmp = new ArrayGrid3i(3, 3, 3);
        Grid3i dst = new ArrayGrid3i(3, 3, 3);

        // WHEN
        MorphologyOps.open(morph, src, v -> v == 1, tmp, dst, Morphology.Neighborhood.N6);

        // THEN
        assertEquals(0, dst.get(0, 0, 0));
        assertEquals(1, dst.get(1, 1, 1));

        int ones = 0;
        for (int z = 0; z < 3; z++) {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if (dst.get(x, y, z) == 1) ones++;
                }
            }
        }
        assertEquals(7, ones);
    }

    @Test
    void flood_fill_counts_single_component_seed() {
        // GIVEN
        ArrayGrid3i g = new ArrayGrid3i(3, 1, 1);
        g.set(1, 0, 0, 1);
        FloodFill fill = ServiceRegistry.of(FloodFill.class).require("floodfill-queue");
        IntPredicate isOne = v -> v == 1;
        final int[] cnt = {0};

        // WHEN
        int visited = fill.fill(g, 1, 0, 0, isOne, (x, y, z, val) -> cnt[0]++);

        // THEN
        assertEquals(1, visited);
        assertEquals(1, cnt[0]);
    }

    @Test
    void connected_components_labels_two_islands() {
        // GIVEN
        ArrayGrid3i src = new ArrayGrid3i(3, 1, 1);
        src.set(0, 0, 0, 1);
        src.set(2, 0, 0, 1);
        Grid3i labels = new ArrayGrid3i(3, 1, 1);
        ConnectedComponents cc = ServiceRegistry.of(ConnectedComponents.class).require("ConnectedComponentsBFS");
        IntPredicate fg = v -> v == 1;

        // WHEN
        int last = cc.label(src, fg, labels, ConnectedComponents.Neighborhood.N6);

        // THEN
        assertEquals(2, last);
        assertTrue(labels.get(0, 0, 0) != labels.get(2, 0, 0));
    }

    @Test
    void morphology_ops_rejects_mismatched_buffer_dimensions() {
        // GIVEN
        Morphology morph = ServiceRegistry.of(Morphology.class).require("MorphologyBasic");
        ArrayGrid3i src = new ArrayGrid3i(3, 3, 1);
        Grid3i tmp = new ArrayGrid3i(2, 3, 1);
        Grid3i dst = new ArrayGrid3i(3, 3, 1);

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class,
                () -> MorphologyOps.open(morph, src, v -> v == 1, tmp, dst, Morphology.Neighborhood.N6));
        assertThrows(IllegalArgumentException.class,
                () -> MorphologyOps.close(morph, src, v -> v == 1, tmp, dst, Morphology.Neighborhood.N6));
    }

    @Test
    void morphology_ops_rejects_negative_iteration_count() {
        // GIVEN
        Morphology morph = ServiceRegistry.of(Morphology.class).require("MorphologyBasic");
        ArrayGrid3i src = new ArrayGrid3i(3, 3, 1);
        Grid3i tmp = new ArrayGrid3i(3, 3, 1);
        Grid3i dst = new ArrayGrid3i(3, 3, 1);

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class,
                () -> MorphologyOps.dilateN(morph, src, v -> v == 1, tmp, dst, Morphology.Neighborhood.N6, -1));
        assertThrows(IllegalArgumentException.class,
                () -> MorphologyOps.erodeN(morph, src, v -> v == 1, tmp, dst, Morphology.Neighborhood.N6, -1));
    }
}
