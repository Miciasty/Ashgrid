package nsk.nu.ashgrid.api.raster;

/**
 * Read-only integer 3D grid interface.
 */
public interface ReadableGrid3i {

    /**
     * Get the cell value at coordinates.
     * @throws IndexOutOfBoundsException if outside the grid
     */
    int get(int x, int y, int z);

    /**
     * Check if coordinates are inside the grid bounds.
     */
    boolean inside(int x, int y, int z);

    /**
     * @return grid width in cells (X extent)
     */
    int width();

    /**
     * @return grid height in cells (Y extent)
     */
    int height();

    /**
     * @return grid depth in cells (Z extent)
     */
    int depth();
}