package nsk.nu.ashgrid.implementation.raster.sparse;

import nsk.nu.ashgrid.api.raster.Grid3i;

import java.util.HashMap;
import java.util.Map;

/**
 * Sparse grid with a default value. Logical space is unbounded; 'inside' means "cell has explicit value".
 */
public final class HashSparseGrid3i implements Grid3i {
    private final Map<Key,Integer> map = new HashMap<>();
    private final int defaultValue;

    public HashSparseGrid3i(int defaultValue){ this.defaultValue = defaultValue; }

    @Override public int get(int x,int y,int z){ return map.getOrDefault(new Key(x,y,z), defaultValue); }
    @Override public void set(int x,int y,int z,int v){ map.put(new Key(x,y,z), v); }
    @Override public boolean inside(int x,int y,int z){ return map.containsKey(new Key(x,y,z)); }

    @Override public int width(){ return 0; }
    @Override public int height(){ return 0; }
    @Override public int depth(){ return 0; }

    private record Key(int x,int y,int z){}
}