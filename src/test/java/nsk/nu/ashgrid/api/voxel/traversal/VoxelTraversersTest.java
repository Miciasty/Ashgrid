package nsk.nu.ashgrid.api.voxel.traversal;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VoxelTraversersTest {

    @Test
    void clipped_traverser_uses_delegate_id_suffix() {
        // GIVEN
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        AxisAlignedBox clip = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(1, 1, 1));

        // WHEN
        VoxelTraverser clipped = VoxelTraversers.clipped(dda, clip);

        // THEN
        assertEquals("dda-clipped", clipped.id());
    }

    @Test
    void clipped_traverser_skips_when_ray_misses_clip_box() {
        // GIVEN
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        AxisAlignedBox clip = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(1, 1, 1));
        VoxelTraverser clipped = VoxelTraversers.clipped(dda, clip);
        Ray ray = new Ray(new Vector3(5, 5, 5), new Vector3(1, 0, 0));
        final int[] visited = {0};

        // WHEN
        clipped.traverse(ray, 10.0, (x, y, z, t0, t1) -> {
            visited[0]++;
            return true;
        });

        // THEN
        assertEquals(0, visited[0]);
    }

    @Test
    void clipped_traverser_rejects_negative_tmax() {
        // GIVEN
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        AxisAlignedBox clip = new AxisAlignedBox(new Vector3(0, 0, 0), new Vector3(1, 1, 1));
        VoxelTraverser clipped = VoxelTraversers.clipped(dda, clip);
        Ray ray = new Ray(new Vector3(0.1, 0.1, 0.1), new Vector3(1, 0, 0));

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class,
                () -> clipped.traverse(ray, -1.0, (x, y, z, t0, t1) -> true));
    }
}
