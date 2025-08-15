package nsk.nu.ashgrid.api.raster;

/**
 * Write-only part of an integer 3D grid interface.
 */
public interface WritableGrid3i {

    /**
     * Set the cell value at coordinates.
     * @throws IndexOutOfBoundsException if outside the grid
     */
    void set(int x, int y, int z, int value);
}