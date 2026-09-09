package nsk.nu.ashgrid.implementation.voxel.ops.floodfill;

import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.util.GridMath;
import nsk.nu.ashgrid.api.voxel.ops.VoxelTask;
import nsk.nu.ashgrid.api.voxel.ops.floodfill.FloodFill;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import java.util.function.IntPredicate;

/** Iterative queue-based flood fill in 6-neighborhood. */
public final class FloodFillQueue implements FloodFill {
    @Override public String id() { return "floodfill-queue"; }

    private static final int[][] N6 = { {1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1} };

    @Override
    public int fill(ReadableGrid3i g,int sx,int sy,int sz,IntPredicate canVisit,CellConsumer visit){
        Task task=begin(g,sx,sy,sz,canVisit,visit);
        task.runToCompletion();
        return task.count();
    }

    public Task begin(ReadableGrid3i g,int sx,int sy,int sz,IntPredicate canVisit,CellConsumer visit){
        return begin(g,sx,sy,sz,canVisit,visit,new Workspace());
    }

    /**
     * One work unit dequeues one candidate and examines at most six neighbors.
     * Dimensions, unvisited cells and the predicate must stay stable between steps, as in fill.
     * The callback may edit its current cell. Close/cancel a task that will not be resumed.
     */
    public Task begin(ReadableGrid3i g,int sx,int sy,int sz,IntPredicate canVisit,CellConsumer visit,Workspace workspace){
        return new Task(g,sx,sy,sz,canVisit,visit,workspace);
    }

    /**
     * Reusable primitive queue and visited bits. One active task at a time; not thread-safe.
     * Retains peak capacity, O(volume). Begin clears previously used visited words;
     * this clearing and buffer growth are outside the cell-work budget.
     */
    public static final class Workspace {
        private final BitSet seen=new BitSet();
        private int[] queue=new int[0];
        private int head,tail;
        private boolean busy;

        private void add(int i,int total){
            if (tail==queue.length) queue=Arrays.copyOf(queue,(int)Math.min(total,Math.max(16L,2L*queue.length)));
            queue[tail++]=i;
        }
    }

    public static final class Task extends VoxelTask {
        private final ReadableGrid3i g;
        private final IntPredicate canVisit;
        private final CellConsumer visit;
        private final Workspace ws;
        private final int w,h,d,wh,total;
        private int count;

        private Task(ReadableGrid3i g,int sx,int sy,int sz,IntPredicate canVisit,CellConsumer visit,Workspace ws){
            this.g=Objects.requireNonNull(g); this.canVisit=Objects.requireNonNull(canVisit);
            this.visit=Objects.requireNonNull(visit); this.ws=Objects.requireNonNull(ws);
            w=g.width(); h=g.height(); d=g.depth(); total=GridMath.cellCount(w,h,d); wh=w*h;
            if (ws.busy) throw new IllegalStateException("workspace is in use");
            boolean inside=sx>=0&&sx<w&&sy>=0&&sy<h&&sz>=0&&sz<d && g.inside(sx,sy,sz);
            ws.seen.clear(); ws.head=0; ws.tail=0;
            if (inside) {
                int i=(sz*h+sy)*w+sx;
                ws.seen.set(i); ws.add(i,total);
            }
            ws.busy=true;
            if (!inside) complete();
        }

        /** Number of successful visits so far; final only when status is COMPLETED. */
        public int count(){ return count; }

        @Override protected boolean advance(){
            int i=ws.queue[ws.head++], z=i/wh, rem=i-z*wh, y=rem/w, x=rem-y*w;
            int val=g.get(x,y,z);
            if (canVisit.test(val)) {
                visit.accept(x,y,z,val); count++;
                for (int[] o : N6) {
                    int nx=x+o[0], ny=y+o[1], nz=z+o[2];
                    if (nx<0||nx>=w||ny<0||ny>=h||nz<0||nz>=d || !g.inside(nx,ny,nz)) continue;
                    int ni=(nz*h+ny)*w+nx;
                    if (!ws.seen.get(ni)) { ws.seen.set(ni); ws.add(ni,total); }
                }
            }
            return ws.head==ws.tail;
        }

        @Override protected void onClose(){ ws.busy=false; ws.head=0; ws.tail=0; }
    }
}
