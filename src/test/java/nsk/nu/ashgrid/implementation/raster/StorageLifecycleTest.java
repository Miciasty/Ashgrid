package nsk.nu.ashgrid.implementation.raster;

import nsk.nu.ashgrid.api.raster.StoredGrid3i;
import nsk.nu.ashgrid.implementation.raster.chunked.ChunkedGrid3i;
import nsk.nu.ashgrid.implementation.raster.sparse.HashSparseGrid3i;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageLifecycleTest {
    @Test
    void stored_cells_exclude_defaults_and_iteration_is_insertion_independent(){
        // GIVEN
        for (StoredGrid3i grid : List.of(new HashSparseGrid3i(7),new ChunkedGrid3i(3,2,4,7))) {
            int[][] cells={{Integer.MIN_VALUE,-1,0},{Integer.MAX_VALUE,0,1},{-4,3,-5},{-3,2,-5},{0,0,0}};
            for (int[] p : cells) grid.set(p[0],p[1],p[2],9);
            grid.set(1,1,1,8); grid.set(1,1,1,7);
            // WHEN
            List<String> first=entries(grid);
            grid.clear();
            for (int i=cells.length-1;i>=0;i--) grid.set(cells[i][0],cells[i][1],cells[i][2],9);
            // THEN
            assertEquals(5,grid.storedCellCount());
            assertEquals(first,entries(grid));
            assertEquals(5,first.size());
            for (int[] p : cells) assertTrue(first.contains(p[0]+","+p[1]+","+p[2]+":9"));
            grid.clear();
            assertEquals(0,grid.storedCellCount()); assertTrue(entries(grid).isEmpty());
            assertEquals(7,grid.get(Integer.MIN_VALUE,-1,0)); assertFalse(grid.has(0,0,0));
        }
    }

    @Test
    void chunk_removal_uses_chunk_coordinates_and_pruning_is_explicit(){
        // GIVEN
        var grid=new ChunkedGrid3i(3,2,4,-7);
        grid.set(-1,-1,-1,5); grid.set(0,0,0,6); grid.set(3,2,4,8);
        grid.set(100,100,100,-7);
        // WHEN
        grid.set(-1,-1,-1,-7);
        // THEN
        assertTrue(grid.has(-2,-2,-4)); assertEquals(3,grid.chunkCount());
        assertEquals(72,grid.allocatedCellCount()); assertEquals(2,grid.storedCellCount());
        List<String> chunks=new ArrayList<>();
        grid.forEachChunk((x,y,z)->chunks.add(x+","+y+","+z));
        assertEquals(List.of("-1,-1,-1","0,0,0","1,1,1"),chunks);
        assertEquals(1,grid.pruneEmptyChunks()); assertEquals(0,grid.pruneEmptyChunks());
        assertFalse(grid.has(-1,-1,-1));
        assertTrue(grid.removeChunk(1,1,1)); assertFalse(grid.removeChunk(1,1,1));
        assertEquals(-7,grid.get(3,2,4)); assertEquals(6,grid.get(0,0,0));
        grid.clear(); assertEquals(0,grid.chunkCount()); assertEquals(0,grid.allocatedCellCount());
    }

    @Test
    void hash_removal_and_order_are_explicit(){
        // GIVEN
        var grid=new HashSparseGrid3i(-1);
        grid.set(2,0,0,2); grid.set(-2,0,0,3); grid.set(0,-1,0,4); grid.set(0,0,-1,5);
        // WHEN / THEN
        assertEquals(List.of("0,0,-1:5","0,-1,0:4","-2,0,0:3","2,0,0:2"),entries(grid));
        assertTrue(grid.remove(-2,0,0)); assertFalse(grid.remove(-2,0,0));
        assertEquals(-1,grid.get(-2,0,0)); assertEquals(3,grid.storedCellCount());
    }

    private static List<String> entries(StoredGrid3i grid){
        List<String> entries=new ArrayList<>();
        grid.forEachStored((x,y,z,v)->entries.add(x+","+y+","+z+":"+v));
        return entries;
    }
}
