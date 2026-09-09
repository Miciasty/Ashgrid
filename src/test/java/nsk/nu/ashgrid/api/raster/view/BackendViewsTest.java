package nsk.nu.ashgrid.api.raster.view;

import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.implementation.raster.bitset.BitGrid3;
import nsk.nu.ashgrid.implementation.raster.chunked.ChunkedGrid3i;
import nsk.nu.ashgrid.implementation.raster.sparse.HashSparseGrid3i;
import nsk.nu.ashgrid.implementation.voxel.ops.floodfill.FloodFillQueue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BackendViewsTest {
    @Test
    void sparse_windows_are_live_bounded_and_include_missing_cells(){
        // GIVEN
        for (var storage : List.of(new HashSparseGrid3i(7),new ChunkedGrid3i(2,3,4,7))) {
            var view=new SparseGridView3i(storage,new IntBox3(-2,-3,-4,1,-1,-2));
            assertFalse(storage.has(-2,-3,-4)); assertTrue(view.inside(0,0,0));
            assertEquals(7,view.get(0,0,0));
            // WHEN
            int count=new FloodFillQueue().fill(view,0,0,0,v->v==7,(x,y,z,v)->view.set(x,y,z,9));
            // THEN
            assertEquals(12,count); assertEquals(9,storage.get(-2,-3,-4));
            storage.set(0,-2,-3,11); assertEquals(11,view.get(2,1,1));
            assertFalse(view.inside(-1,0,0)); assertFalse(view.inside(3,0,0));
            assertThrows(IndexOutOfBoundsException.class,()->view.set(3,0,0,1));
            assertEquals(7,storage.get(1,-3,-4));
        }
    }

    @Test
    void windows_validate_extent_without_wrapping(){
        // GIVEN
        var storage=new HashSparseGrid3i(0);
        var min=new SparseGridView3i(storage,Integer.MIN_VALUE,0,0,1,1,1);
        var max=new SparseGridView3i(storage,Integer.MAX_VALUE,0,0,1,1,1);
        // WHEN / THEN
        min.set(0,0,0,3); max.set(0,0,0,4);
        assertEquals(3,storage.get(Integer.MIN_VALUE,0,0)); assertEquals(4,storage.get(Integer.MAX_VALUE,0,0));
        assertThrows(IllegalArgumentException.class,()->new SparseGridView3i(storage,Integer.MAX_VALUE,0,0,2,1,1));
        assertThrows(IllegalArgumentException.class,()->new SparseGridView3i(storage,0,0,0,0,1,1));
    }

    @Test
    void bit_view_preserves_boolean_api_and_rejects_lossy_values(){
        // GIVEN
        var bits=new BitGrid3(2,3,4); var view=new BitGrid3iView(bits);
        // WHEN / THEN
        view.set(1,2,3,1); assertTrue(bits.get(1,2,3));
        bits.set(1,2,3,false); assertEquals(0,view.get(1,2,3));
        assertThrows(IllegalArgumentException.class,()->view.set(0,0,0,2));
        assertThrows(IllegalArgumentException.class,()->view.set(0,0,0,-1));
        assertFalse(bits.get(0,0,0));
        assertThrows(IndexOutOfBoundsException.class,()->view.get(2,0,0));
        assertFalse(view.inside(0,3,0));
        assertEquals(24,new FloodFillQueue().fill(view,0,0,0,v->v==0,(x,y,z,v)->view.set(x,y,z,1)));
    }
}
