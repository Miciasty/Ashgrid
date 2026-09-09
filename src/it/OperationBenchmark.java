import nsk.nu.ashgrid.api.raster.ops.GridOps;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.components.ConnectedComponentsBFS;
import nsk.nu.ashgrid.implementation.voxel.ops.floodfill.FloodFillQueue;

/** Standalone smoke benchmark; not a latency guarantee or a statistically controlled comparison. */
public final class OperationBenchmark {
    private static volatile long sink;

    public static void main(String[] args){
        var source=new ArrayGrid3i(64,32,16); GridOps.fill(source,1);
        var labels=new ArrayGrid3i(64,32,16);
        var fill=new FloodFillQueue(); var fillWorkspace=new FloodFillQueue.Workspace();
        var components=new ConnectedComponentsBFS(); var componentWorkspace=new ConnectedComponentsBFS.Workspace();
        System.out.println("JDK "+System.getProperty("java.version")+", 64x32x16, all foreground, 20 warmups, 100 measured runs");
        measure("flood/new workspace",()->sink=fill.fill(source,0,0,0,v->v==1,(x,y,z,v)->{}));
        measure("flood/reused workspace, step 256",()->{
            try (var task=fill.begin(source,0,0,0,v->v==1,(x,y,z,v)->{},fillWorkspace)) {
                while (!task.isDone()) task.step(256);
                if (task.count()!=32768) throw new AssertionError();
                sink=task.count();
            }
        });
        measure("components/new workspace",()->sink=components.label(source,v->v==1,labels,ConnectedComponents.Neighborhood.N6));
        measure("components/reused workspace, step 256",()->{
            try (var task=components.begin(source,v->v==1,labels,ConnectedComponents.Neighborhood.N6,componentWorkspace)) {
                while (!task.isDone()) task.step(256);
                if (task.count()!=1) throw new AssertionError();
                sink=task.count();
            }
        });
    }

    private static void measure(String name,Runnable run){
        for (int i=0;i<20;i++) run.run();
        long start=System.nanoTime();
        for (int i=0;i<100;i++) run.run();
        System.out.printf("%s: %.3f ms/op%n",name,(System.nanoTime()-start)/100_000_000.0);
    }
}
