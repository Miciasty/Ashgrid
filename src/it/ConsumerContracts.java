import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.space.VoxelSpace;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;
import nsk.nu.ashgrid.implementation.grid.indexing.SquareXZChunkScheme;
import nsk.nu.ashspace.api.frame.FrameGraph3;
import nsk.nu.ashspace.api.frame.FrameId;
import nsk.nu.ashspace.api.grid.GridSpaceMapper3;
import nsk.nu.ashspace.api.transform.RigidTransform3;
import nsk.nu.ashtrace.api.trace.pipeline.FrameGridRayTracer3;

/** Standalone consumer check; compile only against the identified integration JARs. */
public final class ConsumerContracts {
    public static void main(String[] args) {
        Vector3 origin = new Vector3(10,-20,30);
        for (double scale : new double[]{0.5,1,2}) {
            VoxelSpace voxels = new VoxelSpace(scale,origin);
            GridSpaceMapper3 mapper = new GridSpaceMapper3(scale,origin,new SquareXZChunkScheme(16));
            for (int cell : new int[]{-17,-16,-1,0,1,16,17}) {
                double boundary = origin.x()+cell*scale;
                for (double x : new double[]{Math.nextDown(boundary),boundary,Math.nextUp(boundary)}) {
                    Vector3 point = new Vector3(x,-20.25,30.25);
                    var mapped = mapper.worldToCell(point);
                    check(voxels.ix(point)==mapped.x() && voxels.iy(point)==mapped.y() && voxels.iz(point)==mapped.z(),
                            "SPACE-001 mapping mismatch at " + point + " scale=" + scale);
                }
            }
        }
        FrameGraph3 frames = FrameGraph3.worldRoot();
        FrameId tool = new FrameId("tool");
        frames.define(tool,frames.root(),RigidTransform3.translation(10,0,0));
        VoxelTraverser dda = ServiceRegistry.of(VoxelTraverser.class).require("dda");
        FrameGridRayTracer3 tracer = new FrameGridRayTracer3(frames,dda);
        Ray ray = new Ray(new Vector3(0.2,0.2,0.2),new Vector3(-1,0,0));
        var hit = tracer.firstHit(tool,ray,1,(x,y,z) -> x==9 && y==0 && z==0);
        check(hit!=null && Math.abs(hit.tEnter()-0.2)<1e-14 && hit.tExit()==1,
                "TRACE-002 negative ray distance");
        var hits = tracer.allHits(tool,ray,1,(x,y,z) -> true);
        check(hits.size()==2 && hits.get(0).tEnter()==0 && Math.abs(hits.get(0).tExit()-0.2)<1e-14,
                "TRACE-002 interval order");
        System.out.println("SPACE-001 and TRACE-002 consumer contracts passed.");
    }

    private static void check(boolean condition,String message) {
        if (!condition) throw new AssertionError(message);
    }
}
