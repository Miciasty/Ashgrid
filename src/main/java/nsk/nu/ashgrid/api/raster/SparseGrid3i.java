package nsk.nu.ashgrid.api.raster;

/**
 * Unbounded sparse integer grid.
 * Missing cells return {@link #defaultValue()}.
 * Storage materialization is implementation-defined.
 * For example, chunked implementations may materialize many cell slots at once.
 */
public interface SparseGrid3i extends WritableGrid3i {

    /**
     * Returns the value at coordinates or {@link #defaultValue()} when the cell is not materialized.
     */
    int get(int x, int y, int z);

    /**
     * Returns whether backing storage for this cell slot is materialized.
     * This does not imply that the value differs from {@link #defaultValue()}.
     */
    boolean has(int x, int y, int z);

    /**
     * @return value returned for cells without materialized storage
     */
    int defaultValue();
}
