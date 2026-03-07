package nsk.nu.ashgrid.integration.voxel;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.query.LineOfSight;
import nsk.nu.ashgrid.api.voxel.query.Raycast;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraversers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraversalAndQueryIntegrationTest {

    @Test
    void dda_visits_target_cell_and_is_monotonic_on_positive_axes() {
        // GIVEN
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        Ray ray = new Ray(new Vector3(0.2, 0.2, 0.2), new Vector3(1, 1, 0));
        final int targetX = 3, targetY = 3, targetZ = 0;
        final int[] prev = {Integer.MIN_VALUE, Integer.MIN_VALUE};
        final boolean[] seenTarget = {false};

        // WHEN
        dda.traverse(ray, 1e6, (x, y, z, t0, t1) -> {
            assertTrue(x >= prev[0], "X must be monotonic for +x direction");
            assertTrue(y >= prev[1], "Y must be monotonic for +y direction");
            prev[0] = x;
            prev[1] = y;
            if (x == targetX && y == targetY && z == targetZ) {
                seenTarget[0] = true;
                return false;
            }
            return true;
        });

        // THEN
        assertTrue(seenTarget[0], "Target voxel should be visited");
    }

    @Test
    void raycast_returns_first_solid_voxel() {
        // GIVEN
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        Raycast raycast = new Raycast(dda);
        Ray ray = new Ray(new Vector3(0.1, 0.1, 0.1), new Vector3(1, 0, 0));

        // WHEN
        Raycast.Hit hit = raycast.first(ray, 10.0, (x, y, z) -> x == 2 && y == 0 && z == 0);

        // THEN
        assertNotNull(hit);
        assertEquals(2, hit.x());
        assertEquals(0, hit.y());
        assertEquals(0, hit.z());
    }

    @Test
    void line_of_sight_reports_blocked_segment() {
        // GIVEN
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        LineOfSight los = new LineOfSight(dda);
        Vector3 a = new Vector3(0.1, 0.1, 0.1);
        Vector3 b = new Vector3(5.9, 0.1, 0.1);

        // WHEN
        boolean clear = los.clear(a, b, (x, y, z) -> x == 3 && y == 0 && z == 0);

        // THEN
        assertFalse(clear);
    }

    @Test
    void line_of_sight_ignores_start_cell_occluder() {
        // GIVEN
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        LineOfSight los = new LineOfSight(dda);
        Vector3 a = new Vector3(0.1, 0.1, 0.1);
        Vector3 b = new Vector3(2.9, 0.1, 0.1);

        // WHEN
        boolean clear = los.clear(a, b, (x, y, z) -> x == 0 && y == 0 && z == 0);

        // THEN
        assertTrue(clear, "Start cell is excluded from LOS occlusion checks");
    }

    @Test
    void clipped_traverser_starts_inside_clip_volume() {
        // GIVEN
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        AxisAlignedBox clip = new AxisAlignedBox(new Vector3(2, 0, 0), new Vector3(4, 1, 1));
        VoxelTraverser clipped = VoxelTraversers.clipped(dda, clip);
        Ray ray = new Ray(new Vector3(0.2, 0.2, 0.2), new Vector3(1, 0, 0));
        final int[] firstVisitedX = {Integer.MIN_VALUE};

        // WHEN
        clipped.traverse(ray, 10.0, (x, y, z, t0, t1) -> {
            firstVisitedX[0] = x;
            return false;
        });

        // THEN
        assertEquals(2, firstVisitedX[0], "Traversal should start at clip entry");
    }

    @Test
    void clipped_traverser_reports_t_in_original_ray_parameterization() {
        // GIVEN
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        AxisAlignedBox clip = new AxisAlignedBox(new Vector3(2, 0, 0), new Vector3(4, 1, 1));
        VoxelTraverser clipped = VoxelTraversers.clipped(dda, clip);
        Ray ray = new Ray(new Vector3(0.2, 0.2, 0.2), new Vector3(1, 0, 0));
        final double[] firstTEnter = {Double.NaN};
        final double[] firstTExit = {Double.NaN};

        // WHEN
        clipped.traverse(ray, 10.0, (x, y, z, t0, t1) -> {
            firstTEnter[0] = t0;
            firstTExit[0] = t1;
            return false;
        });

        // THEN
        assertEquals(1.8, firstTEnter[0], 1e-9);
        assertTrue(firstTExit[0] > firstTEnter[0]);
    }
}
