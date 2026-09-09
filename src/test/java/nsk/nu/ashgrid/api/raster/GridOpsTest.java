package nsk.nu.ashgrid.api.raster;

import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.raster.ops.GridOps;
import nsk.nu.ashgrid.api.raster.view.SubGrid3i;
import nsk.nu.ashgrid.api.voxel.ops.VoxelTask;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridOpsTest {
    @Test
    void filling_uses_half_open_bounds_and_counts_writes(){
        // GIVEN
        var grid=new ArrayGrid3i(4,3,2);
        var task=GridOps.beginFill(grid,new IntBox3(1,1,0,3,3,2),7);
        // WHEN
        assertEquals(0,task.step(0)); assertEquals(3,task.step(3));
        assertEquals(7,grid.get(1,1,0)); assertEquals(0,grid.get(2,2,0));
        task.runToCompletion();
        // THEN
        assertEquals(8,task.workDone());
        for (int z=0;z<2;z++) for (int y=0;y<3;y++) for (int x=0;x<4;x++)
            assertEquals(x>=1&&x<3&&y>=1?7:0,grid.get(x,y,z));
    }

    @Test
    void overlapping_views_copy_original_values_in_both_directions(){
        // GIVEN / WHEN / THEN
        for (boolean forward : new boolean[]{false,true}) {
            var grid=new ArrayGrid3i(5,1,1);
            for (int x=0;x<5;x++) grid.set(x,0,0,x+1);
            var src=new SubGrid3i(grid,forward?0:1,0,0,4,1,1);
            var dst=new SubGrid3i(grid,forward?1:0,0,0,4,1,1);
            var task=GridOps.beginCopy(src,new IntBox3(0,0,0,4,1,1),dst,0,0,0,new int[8]);
            while (!task.isDone()) assertEquals(1,task.step(1));
            assertEquals(8,task.workDone());
            int[] expected=forward?new int[]{1,1,2,3,4}:new int[]{2,3,4,5,5};
            for (int x=0;x<5;x++) assertEquals(expected[x],grid.get(x,0,0));
        }
    }

    @Test
    void snapshots_are_independent_and_remap_the_region_origin(){
        // GIVEN
        var src=new ArrayGrid3i(4,3,2); src.set(2,1,1,9);
        // WHEN
        var copy=GridOps.snapshot(src,new IntBox3(1,1,1,4,3,2));
        src.set(2,1,1,7); copy.set(0,0,0,5);
        // THEN
        assertEquals(3,copy.width()); assertEquals(9,copy.get(1,0,0));
        assertEquals(0,src.get(1,1,1));
    }

    @Test
    void invalid_regions_and_small_scratch_fail_before_writes(){
        // GIVEN
        var src=new ArrayGrid3i(3,2,1); var dst=new ArrayGrid3i(3,2,1); GridOps.fill(dst,8);
        var all=new IntBox3(0,0,0,3,2,1);
        // WHEN / THEN
        assertThrows(IllegalArgumentException.class,()->GridOps.copy(src,all,dst,1,0,0));
        assertThrows(IllegalArgumentException.class,()->GridOps.copy(src,all,dst,Integer.MAX_VALUE,0,0));
        assertThrows(IllegalArgumentException.class,()->GridOps.copy(src,all,dst,0,0,0,new int[5]));
        assertThrows(IllegalArgumentException.class,()->GridOps.fill(dst,new IntBox3(-1,0,0,1,1,1),0));
        for (int y=0;y<2;y++) for (int x=0;x<3;x++) assertEquals(8,dst.get(x,y,0));
    }

    @Test
    void empty_regions_are_no_ops_and_empty_snapshots_are_rejected(){
        // GIVEN
        var grid=new ArrayGrid3i(2,1,1); var empty=new IntBox3(2,0,0,2,1,1);
        // WHEN / THEN
        var fill=GridOps.beginFill(grid,empty,9);
        var copy=GridOps.beginCopy(grid,empty,grid,2,0,0);
        assertEquals(VoxelTask.Status.COMPLETED,fill.status()); assertEquals(0,copy.step(10));
        assertEquals(0,grid.get(1,0,0));
        assertThrows(IllegalArgumentException.class,()->GridOps.snapshot(grid,empty));
    }

    @Test
    void cancelling_copy_during_reading_preserves_destination_and_scratch_can_be_reused(){
        // GIVEN
        var src=new ArrayGrid3i(3,1,1); GridOps.fill(src,7);
        var dst=new ArrayGrid3i(3,1,1); var all=new IntBox3(0,0,0,3,1,1); int[] scratch=new int[3];
        // WHEN
        var task=GridOps.beginCopy(src,all,dst,0,0,0,scratch); task.step(2); task.close();
        // THEN
        assertEquals(VoxelTask.Status.CANCELLED,task.status()); assertEquals(0,task.step(20));
        assertEquals(0,dst.get(0,0,0));
        GridOps.copy(src,all,dst,0,0,0,scratch);
        for (int x=0;x<3;x++) assertEquals(7,dst.get(x,0,0));
    }
}
