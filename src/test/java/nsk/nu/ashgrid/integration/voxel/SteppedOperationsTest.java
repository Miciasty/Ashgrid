package nsk.nu.ashgrid.integration.voxel;

import nsk.nu.ashgrid.api.raster.ops.GridOps;
import nsk.nu.ashgrid.api.raster.view.BitGrid3iView;
import nsk.nu.ashgrid.api.raster.view.ClampedGrid3i;
import nsk.nu.ashgrid.api.voxel.ops.VoxelTask;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import nsk.nu.ashgrid.implementation.raster.bitset.BitGrid3;
import nsk.nu.ashgrid.implementation.voxel.ops.components.ConnectedComponentsBFS;
import nsk.nu.ashgrid.implementation.voxel.ops.distance.Chamfer345Distance;
import nsk.nu.ashgrid.implementation.voxel.ops.floodfill.FloodFillQueue;
import nsk.nu.ashgrid.implementation.voxel.ops.morphology.MorphologyBasic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents.Neighborhood.N6;
import static org.junit.jupiter.api.Assertions.*;

class SteppedOperationsTest {
    @Test
    void flood_fill_preserves_bfs_order_across_budgets_and_live_edits(){
        // GIVEN
        var workspace=new FloodFillQueue.Workspace();
        for (int budget : new int[]{1,2,5,Integer.MAX_VALUE}) {
            var grid=new ClampedGrid3i(new ArrayGrid3i(3,2,1)); List<String> visits=new ArrayList<>();
            var task=new FloodFillQueue().begin(grid,0,0,0,v->v==0,(x,y,z,v)->{
                visits.add(x+","+y); grid.set(x,y,z,7);
            },workspace);
            // WHEN / THEN
            drain(task,budget);
            assertEquals(List.of("0,0","1,0","0,1","2,0","1,1","2,1"),visits);
            assertEquals(6,task.count()); assertEquals(6,task.workDone());
        }
    }

    @Test
    void rejected_flood_candidates_consume_budget_and_outside_seed_is_already_complete(){
        // GIVEN
        var grid=new ArrayGrid3i(3,1,1); grid.set(0,0,0,7); var reads=new AtomicInteger();
        var fill=new FloodFillQueue();
        var task=fill.begin(grid,0,0,0,v->{reads.incrementAndGet(); return v==7;},(x,y,z,v)->{});
        // WHEN / THEN
        assertEquals(1,task.step(1)); assertEquals(1,reads.get()); assertFalse(task.isDone());
        assertEquals(1,task.step(1)); assertEquals(2,reads.get()); assertEquals(1,task.count());
        assertEquals(VoxelTask.Status.COMPLETED,task.status());
        var outside=fill.begin(grid,-1,0,0,v->true,(x,y,z,v)->fail());
        assertTrue(outside.isDone()); assertEquals(0,outside.step(1));
    }

    @Test
    void cancellation_is_terminal_and_releases_workspace_without_stale_cursor_interference(){
        // GIVEN
        var fill=new FloodFillQueue(); var ws=new FloodFillQueue.Workspace(); var grid=new ArrayGrid3i(30,2,1);
        var old=fill.begin(grid,0,0,0,v->true,(x,y,z,v)->{},ws);
        // WHEN / THEN
        old.step(2);
        assertThrows(IllegalStateException.class,()->fill.begin(grid,0,0,0,v->true,(x,y,z,v)->{},ws));
        old.cancel(); assertEquals(2,old.count());
        var next=fill.begin(grid,29,1,0,v->true,(x,y,z,v)->{},ws);
        old.close(); assertEquals(0,old.step(100));
        assertThrows(IllegalStateException.class,()->fill.begin(grid,0,0,0,v->true,(x,y,z,v)->{},ws));
        drain(next,3); assertEquals(60,next.count());
        var small=fill.begin(new ArrayGrid3i(1,1,1),0,0,0,v->true,(x,y,z,v)->{},ws);
        drain(small,1); assertEquals(1,small.count());
    }

    @Test
    void failed_callbacks_propagate_and_release_workspace(){
        // GIVEN
        var fill=new FloodFillQueue(); var ws=new FloodFillQueue.Workspace(); var grid=new ArrayGrid3i(2,1,1);
        var failure=new IllegalArgumentException("callback failed");
        var task=fill.begin(grid,0,0,0,v->true,(x,y,z,v)->{throw failure;},ws);
        // WHEN / THEN
        assertSame(failure,assertThrows(IllegalArgumentException.class,()->task.step(1)));
        assertEquals(VoxelTask.Status.FAILED,task.status()); assertEquals(0,task.step(10));
        var next=fill.begin(grid,0,0,0,v->true,(x,y,z,v)->{},ws); drain(next,1);
        assertEquals(2,next.count());
    }

    @Test
    void negative_budgets_and_reentrant_steps_do_not_corrupt_state(){
        // GIVEN
        FloodFillQueue.Task[] holder=new FloodFillQueue.Task[1];
        holder[0]=new FloodFillQueue().begin(new ArrayGrid3i(2,1,1),0,0,0,v->true,(x,y,z,v)->{
            assertThrows(IllegalStateException.class,()->holder[0].step(1));
            assertThrows(IllegalStateException.class,()->holder[0].cancel());
        });
        // WHEN / THEN
        assertThrows(IllegalArgumentException.class,()->holder[0].step(-1));
        assertEquals(0,holder[0].step(0)); assertEquals(0,holder[0].count());
        drain(holder[0],1); assertEquals(2,holder[0].count());
    }

    @Test
    void component_clearing_scanning_and_bfs_all_count_toward_budget(){
        // GIVEN
        var ws=new ConnectedComponentsBFS.Workspace(); var algorithm=new ConnectedComponentsBFS();
        var src=new ArrayGrid3i(3,3,2); src.set(0,0,0,7); src.set(1,1,0,7); src.set(2,2,1,7);
        for (var nh : ConnectedComponents.Neighborhood.values()) for (int budget : new int[]{1,5,100}) {
            var labels=new ArrayGrid3i(3,3,2); GridOps.fill(labels,99);
            var task=algorithm.begin(src,v->v==7,labels,nh,ws);
            // WHEN
            assertEquals(1,task.step(1)); assertEquals(0,labels.get(0,0,0)); assertEquals(99,labels.get(1,0,0));
            drain(task,budget);
            // THEN
            int expected=switch(nh){case N6->3; case N18->2; case N26->1;};
            assertEquals(expected,task.count()); assertEquals(39,task.workDone());
            assertEquals(1,labels.get(0,0,0)); assertEquals(nh==N6?2:1,labels.get(1,1,0));
            assertEquals(expected,labels.get(2,2,1));
            for (int z=0;z<2;z++) for (int y=0;y<3;y++) for (int x=0;x<3;x++)
                if (src.get(x,y,z)==0) assertEquals(0,labels.get(x,y,z));
        }
    }

    @Test
    void component_workspace_is_reusable_after_cancel_and_failure(){
        // GIVEN
        var ws=new ConnectedComponentsBFS.Workspace(); var cc=new ConnectedComponentsBFS();
        var src=new ArrayGrid3i(20,2,1); GridOps.fill(src,1); var out=new ArrayGrid3i(20,2,1);
        var task=cc.begin(src,v->true,out,N6,ws); task.step(45);
        // WHEN / THEN
        assertThrows(IllegalStateException.class,()->cc.begin(src,v->true,out,N6,ws));
        task.close();
        var bad=cc.begin(src,v->{throw new IllegalStateException("predicate");},out,N6,ws);
        assertThrows(IllegalStateException.class,bad::runToCompletion);
        assertEquals(VoxelTask.Status.FAILED,bad.status());
        var next=cc.begin(src,v->true,out,N6,ws); drain(next,7); assertEquals(1,next.count());
        for (int y=0;y<2;y++) for (int x=0;x<20;x++) assertEquals(1,out.get(x,y,0));
    }

    @Test
    void chamfer_passes_resume_with_expected_costs_and_reusable_output(){
        // GIVEN
        var chamfer=new Chamfer345Distance(); float[] out=new float[24];
        for (int budget : new int[]{1,7,100}) {
            var calls=new AtomicInteger();
            var task=chamfer.begin(4,3,2,(x,y,z)->{calls.incrementAndGet(); return x==0&&y==0&&z==0;},out);
            assertEquals(0,calls.get());
            // WHEN
            assertEquals(1,task.step(1)); assertEquals(1,calls.get()); drain(task,budget);
            // THEN: sort absolute deltas and sum 5*c + 4*(b-c) + 3*(a-b).
            assertEquals(72,task.workDone()); assertEquals(24,calls.get());
            for (int z=0;z<2;z++) for (int y=0;y<3;y++) for (int x=0;x<4;x++) {
                int a=Math.max(x,Math.max(y,z)), c=Math.min(x,Math.min(y,z)), b=x+y+z-a-c;
                assertEquals(5*c+4*(b-c)+3*(a-b),out[(z*3+y)*4+x]);
            }
        }
        var empty=chamfer.begin(4,3,2,(x,y,z)->false,out); drain(empty,5);
        for (float v : out) assertEquals(Float.POSITIVE_INFINITY,v);
    }

    @Test
    void morphology_steps_match_neighborhood_geometry_on_bit_storage(){
        // GIVEN
        var morphology=new MorphologyBasic(); var src=new ArrayGrid3i(5,5,5); src.set(2,2,2,7);
        for (var nh : Morphology.Neighborhood.values()) {
            var dst=new BitGrid3iView(new BitGrid3(5,5,5));
            var task=morphology.beginDilate(src,v->v==7,dst,nh);
            // WHEN
            drain(task,3);
            // THEN
            assertEquals(125,task.workDone());
            int limit=switch(nh){case N6->1; case N18->2; case N26->3;};
            for (int z=0;z<5;z++) for (int y=0;y<5;y++) for (int x=0;x<5;x++) {
                int dx=Math.abs(x-2),dy=Math.abs(y-2),dz=Math.abs(z-2);
                assertEquals(dx<=1&&dy<=1&&dz<=1&&dx+dy+dz<=limit?1:0,dst.get(x,y,z));
            }
            var eroded=new ArrayGrid3i(5,5,5);
            drain(morphology.beginErode(dst,v->v==1,eroded,nh),2);
            for (int z=0;z<5;z++) for (int y=0;y<5;y++) for (int x=0;x<5;x++)
                assertEquals(x==2&&y==2&&z==2?1:0,eroded.get(x,y,z));
        }
    }

    @Test
    void cancelling_pointwise_work_leaves_partial_output_and_stops_callbacks(){
        // GIVEN
        var calls=new AtomicInteger(); float[] out={-1,-1,-1};
        var task=new Chamfer345Distance().begin(3,1,1,(x,y,z)->{calls.incrementAndGet(); return true;},out);
        // WHEN
        task.step(1); task.cancel(); task.runToCompletion();
        // THEN
        assertEquals(1,calls.get()); assertArrayEquals(new float[]{0,-1,-1},out);
        assertEquals(VoxelTask.Status.CANCELLED,task.status());
    }

    private static void drain(VoxelTask task,int budget){
        int attempts=0;
        while (!task.isDone()) {
            long before=task.workDone(); int performed=task.step(budget);
            assertTrue(performed>0&&performed<=budget); assertEquals(before+performed,task.workDone());
            assertTrue(++attempts<10000,"task did not finish");
        }
        assertEquals(VoxelTask.Status.COMPLETED,task.status()); assertEquals(0,task.step(1));
    }
}
