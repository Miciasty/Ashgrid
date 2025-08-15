package nsk.nu.ashgrid.api.voxel.ops.distance;

/**
 * Distance transform interface (binary mask to float distances).
 */
public interface DistanceTransform {
    /**
     * Compute distance field into {@code out} from binary mask {@code srcIsForeground}.
     * Distances are in chamfer units; scale appropriately if you need metric units.
     *
     * @param w grid width
     * @param h grid height
     * @param d grid depth
     * @param src value accessor: returns true for foreground voxels (distance=0 at foreground)
     * @param out write distance per voxel (float array of size w*h*d)
     */
    void compute(int w,int h,int d, Mask src, float[] out);

    @FunctionalInterface interface Mask { boolean isForeground(int x,int y,int z); }
}