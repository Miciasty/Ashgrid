package nsk.nu.ashgrid.implementation.raster.sparse;

import nsk.nu.ashgrid.api.raster.SparseGrid3i;
import nsk.nu.ashgrid.api.raster.StoredGrid3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.Objects;

/**
 * Unbounded sparse grid backed by a hash map.
 * Missing cells return {@code defaultValue}.
 */
public final class HashSparseGrid3i implements SparseGrid3i, StoredGrid3i {
    private final Map<Key, Integer> map = new HashMap<>();
    private final int defaultValue;

    public HashSparseGrid3i(int defaultValue) { this.defaultValue = defaultValue; }

    @Override
    public int get(int x, int y, int z) { return map.getOrDefault(new Key(x, y, z), defaultValue); }

    @Override
    public void set(int x, int y, int z, int v) {
        Key key = new Key(x, y, z);
        if (v == defaultValue) map.remove(key);
        else map.put(key, v);
    }

    @Override
    public boolean has(int x, int y, int z) { return map.containsKey(new Key(x, y, z)); }

    @Override
    public int defaultValue() { return defaultValue; }

    @Override public long storedCellCount(){ return map.size(); }
    @Override public void clear(){ map.clear(); }

    /** O(n log n) ordering and O(n) temporary keys for n stored cells. */
    @Override public void forEachStored(CellConsumer consumer){
        Objects.requireNonNull(consumer);
        var keys = map.keySet().stream().sorted(Comparator.comparingInt(Key::z)
                .thenComparingInt(Key::y).thenComparingInt(Key::x)).toList();
        for (Key k : keys) consumer.accept(k.x,k.y,k.z,map.get(k));
    }

    /** Removes an explicit value, returning whether it was present. */
    public boolean remove(int x,int y,int z){ return map.remove(new Key(x,y,z))!=null; }

    /**
     * @deprecated Use {@link #has(int, int, int)}. This method checks whether the cell has explicit storage.
     */
    @Deprecated(forRemoval = false)
    public boolean inside(int x, int y, int z) { return has(x, y, z); }

    private record Key(int x, int y, int z) {}
}
