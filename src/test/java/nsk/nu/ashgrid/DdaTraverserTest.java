package nsk.nu.ashgrid;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.util.GridServices;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/*
start ● at (0.2,0.2,0.2), march →→ along (1,1,0)
X and Y must be non-decreasing; we must visit target cell (3,3,0)
*/
class DdaTraverserTest {

    @Test
    void ray_visits_target_cell_and_is_monotonic_on_axes() {
        // GIVEN
        VoxelTraverser dda = GridServices.require(VoxelTraverser.class, "dda");
        Ray ray = new Ray(new Vector3(0.2, 0.2, 0.2), new Vector3(1, 1, 0));
        final int targetX = 3, targetY = 3, targetZ = 0;
        final int[] prev = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        final boolean[] seenTarget = {false};

        // WHEN
        dda.traverse(ray, 1e6, (x,y,z,t0,t1) -> {
            assertTrue(x >= prev[0], "X must be monotonic for +x");
            assertTrue(y >= prev[1], "Y must be monotonic for +y");
            prev[0]=x; prev[1]=y; prev[2]=z;
            if (x==targetX && y==targetY && z==targetZ) { seenTarget[0] = true; return false; }
            return true;
        });

        // THEN
        assertTrue(seenTarget[0], "Target voxel not visited");
    }
}