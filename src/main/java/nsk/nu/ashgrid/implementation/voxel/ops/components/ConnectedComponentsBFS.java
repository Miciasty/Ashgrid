package nsk.nu.ashgrid.implementation.voxel.ops.components;

import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.raster.util.GridMath;
import nsk.nu.ashgrid.api.voxel.neighborhood.Neighborhood3D;
import nsk.nu.ashgrid.api.voxel.ops.VoxelTask;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntPredicate;

/** BFS-based connected components labeling with flattened loops. */
public final class ConnectedComponentsBFS implements ConnectedComponents {
    @Override public String id() { return "ConnectedComponentsBFS"; }

    @Override
    public int label(Grid3i src,IntPredicate isForeground,Grid3i labelsOut,Neighborhood nh){
        Task task=begin(src,isForeground,labelsOut,nh);
        task.runToCompletion();
        return task.count();
    }

    public Task begin(Grid3i src,IntPredicate fg,Grid3i out,Neighborhood nh){
        return begin(src,fg,out,nh,new Workspace());
    }

    /**
     * One unit clears/scans one cell or dequeues one cell with at most 26 neighbors.
     * Source, dimensions, predicate and neighborhood must stay stable between steps.
     * Output must use separate storage, including views, and must not be changed by the caller.
     * Close/cancel a task that will not be resumed; partial labels do not describe final components.
     */
    public Task begin(Grid3i src,IntPredicate fg,Grid3i out,Neighborhood nh,Workspace workspace){
        return new Task(src,fg,out,nh,workspace);
    }

    /** Reusable primitive BFS queue, O(largest component) retained capacity. One active task at a time. */
    public static final class Workspace {
        private int[] queue=new int[0];
        private int head,tail;
        private boolean busy;

        private void add(int i,int total){
            if (tail==queue.length) queue=Arrays.copyOf(queue,(int)Math.min(total,Math.max(16L,2L*queue.length)));
            queue[tail++]=i;
        }
    }

    public static final class Task extends VoxelTask {
        private final Grid3i src,out;
        private final IntPredicate fg;
        private final Workspace ws;
        private final int w,h,d,wh,total;
        private final int[][] offs;
        private int clearIndex,scanIndex,count;

        private Task(Grid3i src,IntPredicate fg,Grid3i out,Neighborhood nh,Workspace ws){
            this.src=Objects.requireNonNull(src); this.out=Objects.requireNonNull(out);
            this.fg=Objects.requireNonNull(fg); this.ws=Objects.requireNonNull(ws);
            w=src.width(); h=src.height(); d=src.depth(); total=GridMath.cellCount(w,h,d); wh=w*h;
            if (src==out) throw new IllegalArgumentException("labels must not alias source");
            if (out.width()!=w || out.height()!=h || out.depth()!=d)
                throw new IllegalArgumentException("labels dimensions must match source");
            offs=switch (nh) { case N6 -> Neighborhood3D.N6; case N18 -> Neighborhood3D.N18; case N26 -> Neighborhood3D.N26; };
            if (ws.busy) throw new IllegalStateException("workspace is in use");
            ws.head=0; ws.tail=0; ws.busy=true;
        }

        /** Components discovered so far; final only when status is COMPLETED. */
        public int count(){ return count; }

        @Override protected boolean advance(){
            if (clearIndex<total) {
                int i=clearIndex++, z=i/wh, rem=i-z*wh, y=rem/w, x=rem-y*w;
                out.set(x,y,z,0);
            } else if (ws.head<ws.tail) {
                int i=ws.queue[ws.head++], z=i/wh, rem=i-z*wh, y=rem/w, x=rem-y*w;
                for (int[] o : offs) {
                    int nx=x+o[0], ny=y+o[1], nz=z+o[2];
                    if (nx<0||nx>=w||ny<0||ny>=h||nz<0||nz>=d) continue;
                    if (out.get(nx,ny,nz)!=0 || !fg.test(src.get(nx,ny,nz))) continue;
                    out.set(nx,ny,nz,count); ws.add((nz*h+ny)*w+nx,total);
                }
                if (ws.head==ws.tail) { ws.head=0; ws.tail=0; }
            } else {
                int i=scanIndex++, z=i/wh, rem=i-z*wh, y=rem/w, x=rem-y*w;
                if (out.get(x,y,z)==0 && fg.test(src.get(x,y,z))) {
                    count++; out.set(x,y,z,count); ws.add(i,total);
                }
            }
            return clearIndex==total && scanIndex==total && ws.head==ws.tail;
        }

        @Override protected void onClose(){ ws.busy=false; ws.head=0; ws.tail=0; }
    }
}
