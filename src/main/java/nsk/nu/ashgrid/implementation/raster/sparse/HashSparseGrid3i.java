package nsk.nu.ashgrid.implementation.raster.sparse;

import nsk.nu.ashgrid.api.raster.SparseGrid3i;

import java.util.HashMap;
import java.util.Map;

/**
 * Unbounded sparse grid backed by a hash map.
 * Missing cells return {@code defaultValue}.
 */
public final class HashSparseGrid3i implements SparseGrid3i {
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

    /**
     * @deprecated Use {@link #has(int, int, int)}. This method checks whether the cell has explicit storage.
     */
    @Deprecated(forRemoval = false)
    public boolean inside(int x, int y, int z) { return has(x, y, z); }

    private record Key(int x, int y, int z) {}
}
