package nsk.nu.ashgrid.implementation.raster.arrays;

/**
 * Dense 3D float grid backed by a 1D array in Z-major order (z,y,x).
 * Coordinates: x in [0,width), y in [0,height), z in [0,depth).
 */
public final class ArrayGrid3i {
    private final int width, height, depth;
    private final float[] data;

    public ArrayGrid3i(int width, int height, int depth) {
        if (width <= 0 || height <= 0 || depth <= 0)
            throw new IllegalArgumentException("All dimensions must be > 0");
        this.width = width; this.height = height; this.depth = depth;
        this.data = new float[width * height * depth];
    }

    public float get(int x, int y, int z) {
        check(x,y,z); return data[index(x,y,z)];
    }

    public void set(int x, int y, int z, float v) {
        check(x,y,z); data[index(x,y,z)] = v;
    }

    public int width()  { return width;  }
    public int height() { return height; }
    public int depth()  { return depth;  }

    private void check(int x,int y,int z){
        if (x<0||x>=width||y<0||y>=height||z<0||z>=depth) throw new IndexOutOfBoundsException();
    }
    private int index(int x,int y,int z){ return (z * height + y) * width + x; }
}