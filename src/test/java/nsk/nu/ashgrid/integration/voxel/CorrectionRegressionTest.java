package nsk.nu.ashgrid.integration.voxel;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.implementation.voxel.ops.distance.Chamfer345Distance;
import nsk.nu.ashgrid.implementation.voxel.traversal.DDA3DTraverser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CorrectionRegressionTest {
    @Test
    void negative_ray_reaches_first_face_at_positive_distance() {
        // GIVEN
        Ray ray = new Ray(new Vector3(0.2, 0.2, 0.2), new Vector3(-1, 0, 0));
        double[] interval = new double[2];

        // WHEN
        new DDA3DTraverser().traverse(ray, 1, (x,y,z,t0,t1) -> {
            interval[0] = t0; interval[1] = t1;
            return false;
        });

        // THEN
        assertEquals(0, interval[0]);
        assertEquals(0.2, interval[1], 1e-15);
    }

    @Test
    void chamfer_mirrored_diagonals_have_equal_cost() {
        // GIVEN
        Chamfer345Distance distance = new Chamfer345Distance();
        float[] aligned = new float[4], mirrored = new float[4];

        // WHEN
        distance.compute(2, 2, 1, (x,y,z) -> x == 0 && y == 0, aligned);
        distance.compute(2, 2, 1, (x,y,z) -> x == 0 && y == 1, mirrored);

        // THEN
        assertEquals(4f, aligned[3]);
        assertEquals(4f, mirrored[1]);
    }
}
