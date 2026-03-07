package nsk.nu.ashgrid;

import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.ops.distance.DistanceTransform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
0 0 0
0 1 0   (center is foreground)
0 0 0
Face-neighbor distance should be 3 with chamfer 3-4-5.
*/
class DistanceTransformTest {

    @Test
    void chamfer345_face_neighbor_distance_is_3() {
        // GIVEN
        DistanceTransform dt = ServiceRegistry.of(DistanceTransform.class).require("Chamfer345Distance");
        int w=3,h=3,d=1;
        DistanceTransform.Mask mask = (x,y,z) -> x==1 && y==1 && z==0;
        float[] out = new float[w*h*d];

        // WHEN
        dt.compute(w,h,d, mask, out);

        // THEN
        int idxFace = (0) + (1)*w + (0)*(w*h);
        assertEquals(3f, out[idxFace], 1e-5);
    }
}
