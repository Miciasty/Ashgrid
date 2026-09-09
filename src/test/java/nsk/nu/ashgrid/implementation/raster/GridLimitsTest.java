package nsk.nu.ashgrid.implementation.raster;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.grid.bounds.IntRect2;
import nsk.nu.ashgrid.api.grid.indexing.ChunkIndex2;
import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.raster.util.GridMath;
import nsk.nu.ashgrid.api.raster.view.ConstGrid3i;
import nsk.nu.ashgrid.api.raster.view.SubGrid3i;
import nsk.nu.ashgrid.api.voxel.region.RegionIterators;
import nsk.nu.ashgrid.api.voxel.space.VoxelSpace;
import nsk.nu.ashgrid.implementation.grid.indexing.SquareXZChunkScheme;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import nsk.nu.ashgrid.implementation.raster.bitset.BitGrid3;
import nsk.nu.ashgrid.implementation.raster.chunked.ChunkedGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.components.ConnectedComponentsBFS;
import nsk.nu.ashgrid.implementation.voxel.ops.floodfill.FloodFillQueue;
import nsk.nu.ashgrid.implementation.voxel.ops.morphology.MorphologyBasic;
import nsk.nu.ashgrid.implementation.voxel.region.AABBVoxelIterator;
import org.junit.jupiter.api.Test;

import static nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents.Neighborhood.N6;
import static org.junit.jupiter.api.Assertions.*;

class GridLimitsTest {
    @Test
    void invalid_or_overflowing_volumes_are_rejected_before_allocation() {
        // GIVEN / WHEN / THEN
        for (int[] s : new int[][]{{0,1,1},{1,-1,1},{65536,65536,1},{Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE}}) {
            assertThrows(IllegalArgumentException.class, () -> new ArrayGrid3i(s[0],s[1],s[2]));
            assertThrows(IllegalArgumentException.class, () -> new BitGrid3(s[0],s[1],s[2]));
            assertThrows(IllegalArgumentException.class, () -> new ChunkedGrid3i(s[0],s[1],s[2]));
        }
    }

    @Test
    void algorithms_check_custom_grid_volume_before_reading_or_writing() {
        // GIVEN
        Grid3i huge = new Grid3i() {
            @Override public int width() { return 65536; }
            @Override public int height() { return 65536; }
            @Override public int depth() { return 1; }
            @Override public boolean inside(int x,int y,int z) { return true; }
            @Override public int get(int x,int y,int z) { fail("read before shape validation"); return 0; }
            @Override public void set(int x,int y,int z,int v) { fail("write before shape validation"); }
        };
        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> new FloodFillQueue().fill(huge,0,0,0,v -> true,(x,y,z,v) -> {}));
        assertThrows(IllegalArgumentException.class, () -> new ConnectedComponentsBFS().label(huge,v -> true,huge,N6));
        assertThrows(IllegalArgumentException.class, () -> new MorphologyBasic().dilate(huge,v -> true,huge,
                nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology.Neighborhood.N6));
    }

    @Test
    void flattening_roundtrips_non_cubic_shapes_and_checks_extremes() {
        // GIVEN / WHEN / THEN
        for (int z=0;z<4;z++) for (int y=0;y<3;y++) for (int x=0;x<2;x++)
            assertArrayEquals(new int[]{x,y,z},GridMath.unindex(GridMath.linearIndex(x,y,z,2,3),2,3));
        assertThrows(IllegalArgumentException.class, () -> GridMath.linearIndex(0,0,Integer.MAX_VALUE,2,2));
        assertThrows(IllegalArgumentException.class, () -> GridMath.unindex(0,65536,65536));
        assertThrows(IndexOutOfBoundsException.class, () -> GridMath.linearIndex(2,0,0,2,3));
    }

    @Test
    void views_validate_bounds_and_remain_live() {
        // GIVEN
        ArrayGrid3i source = new ArrayGrid3i(3,2,1);
        SubGrid3i sub = new SubGrid3i(source,1,0,0,2,2,1);
        ConstGrid3i readOnly = new ConstGrid3i(sub);
        // WHEN / THEN
        source.set(2,1,0,7); assertEquals(7,readOnly.get(1,1,0));
        sub.set(0,0,0,9); assertEquals(9,source.get(1,0,0));
        assertThrows(UnsupportedOperationException.class, () -> readOnly.set(0,0,0,1));
        assertThrows(IllegalArgumentException.class, () -> new SubGrid3i(source,-1,0,0,1,1,1));
        assertThrows(IllegalArgumentException.class, () -> new SubGrid3i(source,Integer.MAX_VALUE,0,0,Integer.MAX_VALUE,1,1));
        assertThrows(IndexOutOfBoundsException.class, () -> sub.get(2,0,0));
    }

    @Test
    void int_bound_arithmetic_never_wraps_or_misclassifies_nonempty_ranges() {
        // GIVEN / WHEN / THEN
        IntBox3 large = new IntBox3(Integer.MIN_VALUE,0,0,Integer.MAX_VALUE,1,1);
        assertFalse(large.empty());
        assertThrows(ArithmeticException.class,large::width);
        assertFalse(new IntRect2(Integer.MIN_VALUE,0,Integer.MAX_VALUE,1).empty());
        assertThrows(ArithmeticException.class, () -> IntBox3.inclusive(0,0,0,Integer.MAX_VALUE,0,0));
        assertThrows(ArithmeticException.class, () -> large.translate(-1,0,0));
        SquareXZChunkScheme scheme = new SquareXZChunkScheme(1);
        assertThrows(ArithmeticException.class, () -> scheme.neighbors4(new ChunkIndex2(Integer.MAX_VALUE,0)));
        assertThrows(IllegalArgumentException.class, () -> scheme.chunksInAABB(new AxisAlignedBox(
                new Vector3(Integer.MAX_VALUE,0,0),new Vector3(0x1p31,1,1))));
    }

    @Test
    void inclusive_iteration_terminates_at_int_max_and_empty_fractional_aabbs() {
        // GIVEN / WHEN / THEN
        int[] count = {0};
        RegionIterators.forEachCellBox(Integer.MAX_VALUE,0,0,Integer.MAX_VALUE,0,0,(x,y,z) -> {
            assertEquals(Integer.MAX_VALUE,x); assertEquals(1,++count[0]);
        });
        assertEquals(1,count[0]);
        new AABBVoxelIterator().forEachCell(new AxisAlignedBox(new Vector3(0.2,0.2,0.2),new Vector3(0.2,0.8,0.8)),
                (x,y,z) -> fail("empty AABB must not visit"));
        assertThrows(IllegalArgumentException.class, () -> RegionIterators.forEachCellSphere(new Vector3(0,0,0),Double.NaN,(x,y,z) -> {}));
        assertThrows(IllegalArgumentException.class, () -> RegionIterators.forEachCellSphere(new Vector3(0,0,0),-1,(x,y,z) -> {}));
    }

    @Test
    void mapping_rejects_invalid_values_and_preserves_negative_boundaries() {
        // GIVEN
        SquareXZChunkScheme scheme = new SquareXZChunkScheme(16);
        VoxelSpace space = new VoxelSpace(2,new Vector3(10,20,30));
        // WHEN / THEN
        assertEquals(-1,scheme.cellOfPoint(new Vector3(-0.2,0,0)).x());
        assertEquals(-1,scheme.chunkOfPoint(new Vector3(-Double.MIN_VALUE,0,0)).cx());
        assertEquals(-1,scheme.chunkOfPoint(new Vector3(-16,0,0)).cx());
        assertEquals(-2,scheme.chunkOfPoint(new Vector3(Math.nextDown(-16.0),0,0)).cx());
        assertEquals(-1,space.ix(new Vector3(9.8,20,30)));
        assertEquals(0,space.ix(new Vector3(10,20,30)));
        assertEquals(1,space.ix(new Vector3(12,20,30)));
        assertThrows(IllegalArgumentException.class, () -> scheme.cellOfPoint(new Vector3(Double.NaN,0,0)));
        assertThrows(IllegalArgumentException.class, () -> scheme.cellOfPoint(new Vector3(0x1p31,0,0)));
        for (double scale : new double[]{0,-1,Double.NaN,Double.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> new VoxelSpace(scale,new Vector3(0,0,0)));
        assertThrows(IllegalArgumentException.class, () -> space.ix(new Vector3(Double.POSITIVE_INFINITY,0,0)));
    }
}
