package nsk.nu.ashgrid.integration.voxel;

import nsk.nu.ashgrid.api.grid.indexing.CellIndex3;
import nsk.nu.ashgrid.implementation.voxel.draw.SupercoverLine3D;
import nsk.nu.ashgrid.implementation.voxel.draw.BresenhamLine3D;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SupercoverContractTest {
    @Test
    void every_small_segment_matches_closed_cell_intersections() {
        // GIVEN / WHEN
        SupercoverLine3D line = new SupercoverLine3D();
        for (int x=-3;x<=3;x++) for (int y=-3;y<=3;y++) for (int z=-3;z<=3;z++) {
            Set<CellIndex3> actual = new HashSet<>(), expected = new HashSet<>();
            line.trace(0,0,0,x,y,z,(a,b,c) -> { assertTrue(actual.add(new CellIndex3(a,b,c))); return true; });
            for (int a=-3;a<=3;a++) for (int b=-3;b<=3;b++) for (int c=-3;c<=3;c++)
                if (intersects(x,y,z,a,b,c)) expected.add(new CellIndex3(a,b,c));
            // THEN
            assertEquals(expected,actual,"endpoint="+x+","+y+","+z);
            Set<CellIndex3> reverse = new HashSet<>();
            line.trace(x,y,z,0,0,0,(a,b,c) -> { reverse.add(new CellIndex3(a,b,c)); return true; });
            assertEquals(expected,reverse);
        }
    }

    @Test
    void bresenham_handles_full_int_differences_before_early_exit() {
        // GIVEN / WHEN
        int[] count = {0};
        new BresenhamLine3D().trace(Integer.MIN_VALUE,0,0,Integer.MAX_VALUE,1,0,(x,y,z) -> {
            // THEN
            assertEquals(Integer.MIN_VALUE+count[0],x);
            assertEquals(0,y);
            return ++count[0] < 2;
        });
        assertEquals(2,count[0]);
    }

    @Test
    void extreme_endpoints_support_immediate_cancellation() {
        // GIVEN / WHEN / THEN
        int[] calls = {0};
        new SupercoverLine3D().trace(Integer.MIN_VALUE,0,0,Integer.MAX_VALUE,1,0,
                (x,y,z) -> { calls[0]++; assertEquals(Integer.MIN_VALUE,x); return false; });
        assertEquals(1,calls[0]);
    }

    private static boolean intersects(int dx,int dy,int dz,int x,int y,int z) {
        double lo=0, hi=1;
        int[] dir={dx,dy,dz}, cell={x,y,z};
        for (int axis=0;axis<3;axis++) {
            if (dir[axis]==0) { if (cell[axis]!=0) return false; continue; }
            double a=(cell[axis]-0.5)/dir[axis], b=(cell[axis]+0.5)/dir[axis];
            lo=Math.max(lo,Math.min(a,b)); hi=Math.min(hi,Math.max(a,b));
            if (hi<lo) return false;
        }
        return true;
    }
}
