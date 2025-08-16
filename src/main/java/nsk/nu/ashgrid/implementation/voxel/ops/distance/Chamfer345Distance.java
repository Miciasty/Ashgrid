package nsk.nu.ashgrid.implementation.voxel.ops.distance;

import nsk.nu.ashgrid.api.voxel.ops.distance.DistanceTransform;

/**
 * 3-4-5 chamfer distance transform (forward/backward passes).
 * Foreground voxels get distance 0; background receive positive distances.
 */
public final class Chamfer345Distance implements DistanceTransform {
    @Override public String id() { return "Chamfer345Distance"; }

    @Override
    public void compute(int w,int h,int d, Mask src, float[] out) {
        final int wh = w*h; final float INF=1e9f;
        if (out.length != w*h*d) throw new IllegalArgumentException("out size mismatch");

        for (int z=0,i=0; z<d; z++)
            for (int y=0; y<h; y++)
                for (int x=0; x<w; x++, i++)
                    out[i] = src.isForeground(x,y,z) ? 0f : INF;

        final int[][] Nf={{-1,0,0},{0,-1,0},{0,0,-1}};
        final int[][] Ne={{-1,-1,0},{-1,0,-1},{0,-1,-1}};
        final int[][] Nc={{-1,-1,-1}};
        final float wf=3f,we=4f,wc=5f;

        for (int z=0; z<d; z++)
            for (int y=0; y<h; y++)
                for (int x=0; x<w; x++) {
                    V(w, h, d, out, wh, Nf, Ne, Nc, wf, we, wc, z, y, x);
                }

        final int[][] NfB={{1,0,0},{0,1,0},{0,0,1}};
        final int[][] NeB={{1,1,0},{1,0,1},{0,1,1}};
        final int[][] NcB={{1,1,1}};
        for (int z=d-1; z>=0; z--)
            for (int y=h-1; y>=0; y--)
                for (int x=w-1; x>=0; x--) {
                    V(w, h, d, out, wh, NfB, NeB, NcB, wf, we, wc, z, y, x);
                }
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
        if (x<0||x>=w||y<0||y>=h||z<0||z>=d) return 1e9f;
        return out[z*wh + y*w + x] + wgt;
    }
}