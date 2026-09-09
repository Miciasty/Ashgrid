package nsk.nu.ashgrid.api.raster.ops;

import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.raster.BoundedGrid3i;
import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.raster.util.GridMath;
import nsk.nu.ashgrid.api.voxel.ops.VoxelTask;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

import java.util.Objects;

/**
 * Bulk operations on local half-open regions contained in grid dimensions, including clamped views.
 * Source dimensions and contents must stay stable during a task; destination is exclusively owned.
 * Empty fill/copy regions are no-ops. Nonempty volumes must fit an int. Writes are not transactional.
 */
public final class GridOps {
    private GridOps(){}

    public static void fill(Grid3i dst,int value){ fill(dst,extent(dst),value); }

    public static void fill(Grid3i dst,IntBox3 region,int value){ beginFill(dst,region,value).runToCompletion(); }

    /** One unit writes one cell. O(1) scratch. */
    public static VoxelTask beginFill(Grid3i dst,IntBox3 region,int value){
        int total=requireRegion(dst,region), w=region.width(), h=region.height();
        if (total==0) return empty();
        return new VoxelTask() {
            private int i;

            @Override protected boolean advance(){
                int x=i%w, y=(i/w)%h, z=i/(w*h);
                dst.set(region.minX()+x,region.minY()+y,region.minZ()+z,value);
                return ++i==total;
            }
        };
    }

    /** Copies to a same-shaped destination. Overlap, including overlapping views, is supported. */
    public static void copy(ReadableGrid3i src,Grid3i dst){
        if (src.width()!=dst.width() || src.height()!=dst.height() || src.depth()!=dst.depth())
            throw new IllegalArgumentException("grid dimensions must match");
        copy(src,extent(src),dst,0,0,0);
    }

    public static void copy(ReadableGrid3i src,IntBox3 region,Grid3i dst,int dx,int dy,int dz){
        beginCopy(src,region,dst,dx,dy,dz).runToCompletion();
    }

    public static void copy(ReadableGrid3i src,IntBox3 region,Grid3i dst,int dx,int dy,int dz,int[] scratch){
        beginCopy(src,region,dst,dx,dy,dz,scratch).runToCompletion();
    }

    public static VoxelTask beginCopy(ReadableGrid3i src,IntBox3 region,Grid3i dst,int dx,int dy,int dz){
        int total=requireCopy(src,region,dst,dx,dy,dz);
        return beginCopy(src,region,dst,dx,dy,dz,new int[total]);
    }

    /**
     * Reads the entire region before writing, so aliasing is safe. 2*volume units; one read/write per unit.
     * Scratch must have at least volume entries and must not back either grid or be shared with another
     * active task. Allocation is outside step. Cancellation in the read phase leaves destination untouched.
     */
    public static VoxelTask beginCopy(ReadableGrid3i src,IntBox3 region,Grid3i dst,int dx,int dy,int dz,int[] scratch){
        int total=requireCopy(src,region,dst,dx,dy,dz), w=region.width(), h=region.height();
        Objects.requireNonNull(scratch);
        if (scratch.length<total) throw new IllegalArgumentException("scratch is smaller than region volume");
        if (total==0) return empty();
        return new VoxelTask() {
            private int i;
            private boolean writing;

            @Override protected boolean advance(){
                int x=i%w, y=(i/w)%h, z=i/(w*h);
                if (writing) dst.set(dx+x,dy+y,dz+z,scratch[i]);
                else scratch[i]=src.get(region.minX()+x,region.minY()+y,region.minZ()+z);
                if (++i==total) {
                    if (writing) return true;
                    writing=true; i=0;
                }
                return false;
            }
        };
    }

    public static BoundedGrid3i snapshot(ReadableGrid3i src){ return snapshot(src,extent(src)); }

    /** Independent mutable dense copy, with local origin zero. Empty snapshots are rejected. O(volume). */
    public static BoundedGrid3i snapshot(ReadableGrid3i src,IntBox3 region){
        int total=requireRegion(src,region), w=region.width(), h=region.height();
        if (total==0) throw new IllegalArgumentException("snapshot region must not be empty");
        ArrayGrid3i out=new ArrayGrid3i(w,h,region.depth());
        for (int i=0;i<total;i++) {
            int x=i%w, y=(i/w)%h, z=i/(w*h);
            out.set(x,y,z,src.get(region.minX()+x,region.minY()+y,region.minZ()+z));
        }
        return out;
    }

    private static int requireCopy(ReadableGrid3i src,IntBox3 region,Grid3i dst,int dx,int dy,int dz){
        int total=requireRegion(src,region);
        if (dx<0||dy<0||dz<0 || (long)dx+region.width()>dst.width()
                || (long)dy+region.height()>dst.height() || (long)dz+region.depth()>dst.depth())
            throw new IllegalArgumentException("copy region outside destination dimensions");
        return total;
    }

    private static int requireRegion(ReadableGrid3i grid,IntBox3 region){
        if (region.minX()<0||region.minY()<0||region.minZ()<0 || region.maxX()>grid.width()
                || region.maxY()>grid.height() || region.maxZ()>grid.depth())
            throw new IllegalArgumentException("region outside grid dimensions");
        return region.empty()?0:GridMath.cellCount(region.width(),region.height(),region.depth());
    }

    private static IntBox3 extent(ReadableGrid3i grid){ return new IntBox3(0,0,0,grid.width(),grid.height(),grid.depth()); }

    private static VoxelTask empty(){
        return new VoxelTask() {
            { complete(); }
            @Override protected boolean advance(){ return true; }
        };
    }
}
