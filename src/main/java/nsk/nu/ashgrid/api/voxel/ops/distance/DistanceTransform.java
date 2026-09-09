package nsk.nu.ashgrid.api.voxel.ops.distance;

import nsk.nu.ashcore.api.spi.Identified;

/** Distance transform interface (binary mask to float distances). */
public interface DistanceTransform extends Identified {
    /**
     * Writes distances to foreground (zero at foreground) in x-fastest order: (z*h+y)*w+x.
     * Dimensions must be positive and their product fit int; out must have exactly that length.
     * The provider defines the metric and units; no foreground gives positive infinity.
     * src must be stable throughout the call and must not depend on the changing out buffer.
     */
    void compute(int w,int h,int d, Mask src, float[] out);
    @FunctionalInterface interface Mask { boolean isForeground(int x,int y,int z); }
}
