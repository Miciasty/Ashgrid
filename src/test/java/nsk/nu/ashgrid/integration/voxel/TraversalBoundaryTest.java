package nsk.nu.ashgrid.integration.voxel;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.voxel.query.LineOfSight;
import nsk.nu.ashgrid.api.voxel.query.Raycast;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraversers;
import nsk.nu.ashgrid.implementation.voxel.traversal.DDA3DTraverser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TraversalBoundaryTest {
    private final DDA3DTraverser dda = new DDA3DTraverser();
    private record Visit(int x,int y,int z,double enter,double exit) {}

    @Test
    void six_axis_directions_cover_the_segment_without_gaps() {
        // GIVEN / WHEN
        for (int axis=0; axis<3; axis++) for (int sign : new int[]{-1,1}) {
            double[] d = new double[3]; d[axis] = sign;
            List<Visit> visits = trace(new Vector3(0.2,0.2,0.2), new Vector3(d[0],d[1],d[2]), 1);
            int[] last = new int[3]; last[axis] = sign;
            double crossing = sign < 0 ? 0.2 : 0.8;

            // THEN
            assertEquals(2, visits.size());
            assertVisit(visits.get(0), 0,0,0,0,crossing);
            assertVisit(visits.get(1), last[0],last[1],last[2],crossing,1);
        }
    }

    @Test
    void mixed_directions_and_negative_origins_match_interior_points() {
        // GIVEN / WHEN
        for (int sx : new int[]{-1,1}) for (int sy : new int[]{-1,1}) for (int sz : new int[]{-1,1}) {
            Ray ray = new Ray(new Vector3(-2.25,-0.75,1.125), new Vector3(sx,2*sy,3*sz));
            double[] previous = {0};
            dda.traverse(ray, 8, (x,y,z,t0,t1) -> {
                // THEN: a point strictly inside every positive interval belongs to that cell.
                assertEquals(previous[0], t0, 1e-14);
                assertTrue(t1 >= t0 && t1 <= 8);
                double middle = t0 + (t1-t0)*0.5;
                // Adjacent doubles have no representable interior sample; ties are tested separately.
                if (middle > t0 && middle < t1) {
                    Vector3 p = ray.at(middle);
                    assertEquals((int)Math.floor(p.x()), x);
                    assertEquals((int)Math.floor(p.y()), y);
                    assertEquals((int)Math.floor(p.z()), z);
                }
                previous[0] = t1;
                return true;
            });
            assertEquals(8, previous[0]);
        }
    }

    @Test
    void face_edge_and_corner_ties_preserve_x_y_z_order() {
        // GIVEN / WHEN
        List<Visit> corner = trace(new Vector3(0,0,0), new Vector3(-1,-1,-1), 0.5);
        List<Visit> edge = trace(new Vector3(0,0,0.5), new Vector3(-1,-1,0), 0.5);

        // THEN
        assertEquals(4, corner.size());
        assertVisit(corner.get(0),0,0,0,0,0);
        assertVisit(corner.get(1),-1,0,0,0,0);
        assertVisit(corner.get(2),-1,-1,0,0,0);
        assertVisit(corner.get(3),-1,-1,-1,0,0.5);
        assertEquals(3, edge.size());
        assertVisit(edge.get(1),-1,0,0,0,0);
        List<Visit> face = trace(new Vector3(0,0,0), new Vector3(-1,0,0), 0.5);
        assertEquals(2, face.size());
        assertVisit(face.get(1),-1,0,0,0,0.5);
    }

    @Test
    void positive_corner_and_exact_endpoint_do_not_add_an_endpoint_cell() {
        // GIVEN
        Vector3 origin = new Vector3(0.5,0.5,0.5), direction = new Vector3(1,1,1);
        Ray ray = new Ray(origin, direction);
        double boundary = 0.5 / ray.direction().x();

        // WHEN / THEN
        assertEquals(1, trace(origin,direction,boundary).size());
        List<Visit> beyond = trace(origin,direction,boundary+0.1);
        assertEquals(4, beyond.size());
        assertVisit(beyond.get(1),1,0,0,boundary,boundary);
        assertVisit(beyond.get(2),1,1,0,boundary,boundary);
        assertVisit(beyond.get(3),1,1,1,boundary,boundary+0.1);
    }

    @Test
    void zero_infinite_invalid_limits_and_index_exhaustion_are_explicit() {
        // GIVEN
        Ray ray = new Ray(new Vector3(0.2,0.2,0.2), new Vector3(-1,0,0));
        int[] count = {0};

        // WHEN / THEN
        dda.traverse(ray, 0, (x,y,z,a,b) -> { fail("zero limit must not visit"); return true; });
        dda.traverse(ray, Double.POSITIVE_INFINITY, (x,y,z,a,b) -> ++count[0] < 3);
        assertEquals(3, count[0]);
        for (double limit : new double[]{-1,Double.NaN,Double.NEGATIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> dda.traverse(ray, limit, (x,y,z,a,b) -> true));
        Ray edge = new Ray(new Vector3(Integer.MAX_VALUE+0.5,0,0), new Vector3(1,0,0));
        assertEquals(1, trace(edge.origin(),edge.direction(),0.5).size());
        assertThrows(ArithmeticException.class, () -> dda.traverse(edge, 1, (x,y,z,a,b) -> true));
        dda.traverse(edge, Double.POSITIVE_INFINITY, (x,y,z,a,b) -> false);
        assertThrows(IllegalArgumentException.class, () -> trace(new Vector3(0x1p31,0,0), new Vector3(1,0,0), 1));
    }

    @Test
    void tiny_nonzero_components_on_a_face_do_not_create_nan_times() {
        // GIVEN / WHEN
        List<Visit> visits = trace(new Vector3(0.5,0,0.5),new Vector3(1,-1e-310,0),0.5);
        // THEN
        assertEquals(2,visits.size());
        assertVisit(visits.get(0),0,0,0,0,0);
        assertVisit(visits.get(1),0,-1,0,0,0.5);
    }

    @Test
    void invalid_rays_are_rejected_even_with_the_legacy_core_dependency() {
        // GIVEN / WHEN / THEN
        Vector3 valid = new Vector3(1,0,0);
        for (Vector3 invalid : new Vector3[]{new Vector3(Double.NaN,0,0),new Vector3(Double.POSITIVE_INFINITY,0,0)}) {
            assertThrows(IllegalArgumentException.class, () -> trace(invalid,valid,1));
            assertThrows(IllegalArgumentException.class, () -> trace(valid,invalid,1));
        }
        assertThrows(IllegalArgumentException.class, () -> trace(valid,new Vector3(0,0,0),1));
    }

    @Test
    void negative_raycast_and_line_of_sight_use_correct_distances_and_endpoints() {
        // GIVEN / WHEN
        Ray ray = new Ray(new Vector3(0.2,0.2,0.2), new Vector3(-1,0,0));
        Raycast.Hit hit = new Raycast(dda).first(ray, 1, (x,y,z) -> x == -1);
        LineOfSight los = new LineOfSight(dda);

        // THEN
        assertNotNull(hit);
        assertEquals(0.2, hit.tEnter(), 1e-15);
        assertEquals(1, hit.tExit());
        assertFalse(los.clear(ray.origin(), new Vector3(-1,0.2,0.2), (x,y,z) -> x == -1));
        assertTrue(los.clear(ray.origin(), new Vector3(-1,0.2,0.2), (x,y,z) -> x == -2));
        assertTrue(los.clear(ray.origin(), ray.origin(), (x,y,z) -> true));
        assertNull(new Raycast(dda).first(ray, 0.2, (x,y,z) -> x == -1));
    }

    @Test
    void clipping_preserves_negative_entry_and_half_open_parallel_faces() {
        // GIVEN
        var clipped = VoxelTraversers.clipped(dda, new AxisAlignedBox(new Vector3(0,0,0),new Vector3(2,1,1)));
        List<Visit> visits = new ArrayList<>();

        // WHEN
        clipped.traverse(new Ray(new Vector3(3,0.5,0.5),new Vector3(-1,0,0)), 10,
                (x,y,z,a,b) -> { visits.add(new Visit(x,y,z,a,b)); return true; });

        // THEN
        assertEquals(3, visits.size());
        assertVisit(visits.get(0),2,0,0,1,1);
        assertVisit(visits.get(1),1,0,0,1,2);
        assertVisit(visits.get(2),0,0,0,2,3);
        clipped.traverse(new Ray(new Vector3(0,1,0.5),new Vector3(1,0,0)), 10,
                (x,y,z,a,b) -> { fail("upper parallel face is excluded"); return true; });
    }

    private List<Visit> trace(Vector3 origin,Vector3 direction,double max) {
        List<Visit> visits = new ArrayList<>();
        dda.traverse(new Ray(origin,direction), max, (x,y,z,a,b) -> {
            visits.add(new Visit(x,y,z,a,b)); return true;
        });
        return visits;
    }

    private static void assertVisit(Visit v,int x,int y,int z,double a,double b) {
        assertEquals(x,v.x()); assertEquals(y,v.y()); assertEquals(z,v.z());
        // Absolute tolerance for these unit-scale examples, not a membership epsilon.
        assertEquals(a,v.enter(),1e-14); assertEquals(b,v.exit(),1e-14);
    }
}
