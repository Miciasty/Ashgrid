import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.raster.SparseGrid3i;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.api.voxel.ops.distance.DistanceTransform;
import nsk.nu.ashgrid.api.voxel.ops.floodfill.FloodFill;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import nsk.nu.ashgrid.implementation.raster.chunked.ChunkedGrid3i;
import nsk.nu.ashgrid.implementation.raster.sparse.HashSparseGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.components.ConnectedComponentsBFS;
import nsk.nu.ashgrid.implementation.voxel.ops.distance.Chamfer345Distance;
import nsk.nu.ashgrid.implementation.voxel.ops.floodfill.FloodFillQueue;
import nsk.nu.ashgrid.implementation.voxel.ops.morphology.MorphologyBasic;

/** Compile with the pre-extension Ashgrid JAR; run unchanged with the extended JAR. */
public final class BinaryCompatibility {
    public static void main(String[] args){
        var source=new ArrayGrid3i(3,1,1); source.set(0,0,0,7); source.set(2,0,0,7);
        var out=new ArrayGrid3i(3,1,1);
        if (new ConnectedComponentsBFS().label(source,v->v==7,out,ConnectedComponents.Neighborhood.N6)!=2)
            throw new AssertionError("components");
        if (new FloodFillQueue().fill(source,0,0,0,v->v==7,(x,y,z,v)->{})!=1)
            throw new AssertionError("flood fill");
        new MorphologyBasic().dilate(source,v->v==7,out,Morphology.Neighborhood.N6);
        if (out.get(1,0,0)!=1) throw new AssertionError("morphology");
        float[] distance=new float[3];
        new Chamfer345Distance().compute(3,1,1,(x,y,z)->x==0,distance);
        if (distance[2]!=6) throw new AssertionError("chamfer");
        for (SparseGrid3i grid : new SparseGrid3i[]{new HashSparseGrid3i(-1),new ChunkedGrid3i(3,2,4,-1)}) {
            grid.set(-1,-1,-1,7);
            if (grid.get(-1,-1,-1)!=7 || !grid.has(-1,-1,-1) || grid.defaultValue()!=-1)
                throw new AssertionError("sparse");
        }
        ServiceRegistry.of(FloodFill.class).require("floodfill-queue");
        ServiceRegistry.of(ConnectedComponents.class).require("ConnectedComponentsBFS");
        ServiceRegistry.of(DistanceTransform.class).require("Chamfer345Distance");
        ServiceRegistry.of(Morphology.class).require("MorphologyBasic");
        System.out.println("Pre-extension binary client: PASS");
    }
}
