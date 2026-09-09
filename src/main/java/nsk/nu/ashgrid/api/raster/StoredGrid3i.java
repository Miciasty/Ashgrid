package nsk.nu.ashgrid.api.raster;

/** Optional sparse storage inspection and lifecycle operations. Not thread-safe. */
public interface StoredGrid3i extends SparseGrid3i {
    /** Number of cells whose value differs from defaultValue, not allocated slots. */
    long storedCellCount();

    /**
     * Visits non-default cells. Hash storage uses Z,Y,X order; chunked storage uses
     * chunk Z,Y,X then local Z,Y,X. Do not mutate this storage during a callback.
     */
    void forEachStored(CellConsumer consumer);

    /** Releases all entries/chunks. Subsequent reads return defaultValue. */
    void clear();

    @FunctionalInterface
    interface CellConsumer {
        void accept(int x,int y,int z,int value);
    }
}
