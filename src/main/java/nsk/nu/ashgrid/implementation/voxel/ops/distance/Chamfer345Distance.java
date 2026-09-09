package nsk.nu.ashgrid.implementation.voxel.ops.distance;

import nsk.nu.ashgrid.api.raster.util.GridMath;
import nsk.nu.ashgrid.api.voxel.ops.VoxelTask;
import nsk.nu.ashgrid.api.voxel.ops.distance.DistanceTransform;

/**
 * 3-4-5 chamfer distance transform (forward/backward passes).
 * Foreground voxels get distance 0; background receive minimum 26-neighbor path costs.
 * Face/edge/corner steps cost 3/4/5, without normalization. Divide by 3 for an approximation
 * in cell units, not Euclidean distance or a character-clearance guarantee.
 * No foreground yields positive infinity. Large path costs are rounded to float precision.
 */
public final class Chamfer345Distance implements DistanceTransform {
    @Override public String id() { return "Chamfer345Distance"; }

    @Override
    public void compute(int w,int h,int d, Mask src, float[] out) {
        begin(w,h,d,src,out).runToCompletion();
    }

    /**
     * One unit initializes or relaxes one cell (at most 13 neighbors). Three passes, 3*volume units.
     * The caller owns and may reuse out after completion/cancellation; no volume-sized scratch is needed.
     * Source and output must remain exclusively available to this task between steps.
     */
    public VoxelTask begin(int w,int h,int d, Mask src, float[] out) {
        final int total = GridMath.cellCount(w,h,d);
        final int wh = w*h; final float INF=Float.POSITIVE_INFINITY;
        if (out.length != total) throw new IllegalArgumentException("out size mismatch");
        if (src == null) throw new NullPointerException("src");

        final int[][] Nf={{-1,0,0},{0,-1,0},{0,0,-1}};
        final int[][] Ne={{-1,-1,0},{1,-1,0},{-1,0,-1},{1,0,-1},{0,-1,-1},{0,1,-1}};
        final int[][] Nc={{-1,-1,-1},{1,-1,-1},{-1,1,-1},{1,1,-1}};
        final float wf=3f,we=4f,wc=5f;

        final int[][] NfB={{1,0,0},{0,1,0},{0,0,1}};
        final int[][] NeB={{1,1,0},{-1,1,0},{1,0,1},{-1,0,1},{0,1,1},{0,-1,1}};
        final int[][] NcB={{1,1,1},{-1,1,1},{1,-1,1},{-1,-1,1}};
        return new VoxelTask() {
            private int phase,index;

            @Override protected boolean advance(){
                int i=phase==2?total-1-index:index;
                int z=i/wh, rem=i-z*wh, y=rem/w, x=rem-y*w;
                if (phase==0) out[i]=src.isForeground(x,y,z)?0f:INF;
                else if (phase==1) V(w,h,d,out,wh,Nf,Ne,Nc,wf,we,wc,z,y,x);
                else V(w,h,d,out,wh,NfB,NeB,NcB,wf,we,wc,z,y,x);
                if (++index==total) { index=0; phase++; }
                return phase==3;
            }
        };
    }

    private void V(int w, int h, int d, float[] out, int wh, int[][] nf, int[][] ne, int[][] nc, float wf, float we, float wc, int z, int y, int x) {
        int i = z*wh + y*w + x;
        float v = out[i];
        for (int[] o: nf) v = Math.min(v, n(out,w,h,d,wh,x+o[0],y+o[1],z+o[2],wf));
        for (int[] o: ne) v = Math.min(v, n(out,w,h,d,wh,x+o[0],y+o[1],z+o[2],we));
        for (int[] o: nc) v = Math.min(v, n(out,w,h,d,wh,x+o[0],y+o[1],z+o[2],wc));
        out[i]=v;
    }

    private static float n(float[] out,int w,int h,int d,int wh,int x,int y,int z,float wgt){
        if (x<0||x>=w||y<0||y>=h||z<0||z>=d) return Float.POSITIVE_INFINITY;
        return out[z*wh + y*w + x] + wgt;
    }
}
