package nsk.nu.ashgrid.integration.voxel;

import nsk.nu.ashgrid.implementation.voxel.ops.distance.Chamfer345Distance;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ChamferDistanceTest {
    private final Chamfer345Distance distance = new Chamfer345Distance();

    @Test
    void every_single_source_orientation_matches_shortest_paths() {
        // GIVEN / WHEN / THEN: non-cubic shapes cover axis permutations and reflected sources.
        for (int[] shape : new int[][]{{4,3,2},{2,4,3},{3,2,4}}) {
            int n = shape[0]*shape[1]*shape[2];
            for (int source=0; source<n; source++) {
                boolean[] mask = new boolean[n]; mask[source] = true;
                compare(shape[0],shape[1],shape[2],mask);
            }
        }
    }

    @Test
    void multiple_sources_and_empty_mask_match_dijkstra_reference() {
        // GIVEN / WHEN / THEN
        Random random = new Random(345);
        for (int run=0; run<30; run++) {
            boolean[] mask = new boolean[60];
            for (int i=0; i<mask.length; i++) mask[i] = random.nextInt(7) == 0;
            compare(5,4,3,mask);
        }
        compare(2,2,2,new boolean[8]);
        boolean[] full = new boolean[8]; Arrays.fill(full,true);
        compare(2,2,2,full);
    }

    @Test
    void invalid_dimensions_and_capacity_are_rejected_before_reading_mask() {
        // GIVEN / WHEN / THEN
        for (int[] shape : new int[][]{{0,1,1},{-1,1,1},{65536,65536,1},{Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE}})
            assertThrows(IllegalArgumentException.class, () -> distance.compute(shape[0],shape[1],shape[2],
                    (x,y,z) -> { fail("invalid shape must not read mask"); return false; }, new float[0]));
        assertThrows(IllegalArgumentException.class, () -> distance.compute(2,2,2,(x,y,z) -> false,new float[7]));
        assertThrows(IllegalArgumentException.class, () -> distance.compute(2,2,2,(x,y,z) -> false,new float[9]));
    }

    private void compare(int w,int h,int d,boolean[] mask) {
        float[] actual = new float[mask.length];
        distance.compute(w,h,d,(x,y,z) -> mask[(z*h+y)*w+x],actual);
        assertArrayEquals(reference(w,h,d,mask),actual);
    }

    private record Node(int index,int cost) implements Comparable<Node> {
        @Override public int compareTo(Node o) { return Integer.compare(cost,o.cost); }
    }

    private static float[] reference(int w,int h,int d,boolean[] mask) {
        float[] out = new float[mask.length]; Arrays.fill(out,Float.POSITIVE_INFINITY);
        PriorityQueue<Node> queue = new PriorityQueue<>();
        for (int i=0; i<mask.length; i++) if (mask[i]) { out[i]=0; queue.add(new Node(i,0)); }
        while (!queue.isEmpty()) {
            Node p = queue.remove();
            if (p.cost() != out[p.index()]) continue;
            int x=p.index()%w, y=(p.index()/w)%h, z=p.index()/(w*h);
            for (int dz=-1; dz<=1; dz++) for (int dy=-1; dy<=1; dy++) for (int dx=-1; dx<=1; dx++) {
                int axes = Math.abs(dx)+Math.abs(dy)+Math.abs(dz);
                int nx=x+dx, ny=y+dy, nz=z+dz;
                if (axes==0 || nx<0||nx>=w||ny<0||ny>=h||nz<0||nz>=d) continue;
                int index=(nz*h+ny)*w+nx, cost=p.cost()+axes+2;
                if (cost < out[index]) { out[index]=cost; queue.add(new Node(index,cost)); }
            }
        }
        return out;
    }
}
