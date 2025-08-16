package nsk.nu.ashgrid.api.voxel.ops.distance;

import nsk.nu.ashcore.api.spi.Identified;

/** Distance transform interface (binary mask to float distances). */
public interface DistanceTransform extends Identified {
    void compute(int w,int h,int d, Mask src, float[] out);
    @FunctionalInterface interface Mask { boolean isForeground(int x,int y,int z); }
}