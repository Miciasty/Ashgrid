package nsk.nu.ashgrid.api.grid.bounds;

/**
 * Integer axis-aligned box in cell coordinates.
 * Half-open by default: [minX, maxX) x [minY, maxY) x [minZ, maxZ)
 * so width = maxX - minX etc. Use inclusive() for inclusive ranges.
 * Size/arithmetic helpers throw ArithmeticException when their int result would overflow.
 */
public record IntBox3(int minX, int minY, int minZ,
                      int maxX, int maxY, int maxZ) {

    public IntBox3 {
        if (maxX < minX || maxY < minY || maxZ < minZ)
            throw new IllegalArgumentException("max < min");
    }

    public int width()  { return Math.subtractExact(maxX, minX); }
    public int height() { return Math.subtractExact(maxY, minY); }
    public int depth()  { return Math.subtractExact(maxZ, minZ); }
    public boolean empty() { return maxX == minX || maxY == minY || maxZ == minZ; }

    public boolean contains(int x,int y,int z) {
        return x >= minX && x < maxX &&
                y >= minY && y < maxY &&
                z >= minZ && z < maxZ;
    }

    public IntBox3 expand(int dx,int dy,int dz) {
        return new IntBox3(Math.subtractExact(minX, dx), Math.subtractExact(minY, dy), Math.subtractExact(minZ, dz),
                Math.addExact(maxX, dx), Math.addExact(maxY, dy), Math.addExact(maxZ, dz));
    }

    public IntBox3 translate(int dx,int dy,int dz) {
        return new IntBox3(Math.addExact(minX, dx), Math.addExact(minY, dy), Math.addExact(minZ, dz),
                Math.addExact(maxX, dx), Math.addExact(maxY, dy), Math.addExact(maxZ, dz));
    }

    public IntBox3 intersect(IntBox3 o) {
        int nx0 = Math.max(minX, o.minX), ny0 = Math.max(minY, o.minY), nz0 = Math.max(minZ, o.minZ);
        int nx1 = Math.min(maxX, o.maxX), ny1 = Math.min(maxY, o.maxY), nz1 = Math.min(maxZ, o.maxZ);
        if (nx1 <= nx0 || ny1 <= ny0 || nz1 <= nz0) return new IntBox3(0,0,0,0,0,0);
        return new IntBox3(nx0, ny0, nz0, nx1, ny1, nz1);
    }

    /** Inclusive constructor helper: [x0..x1], etc. */
    public static IntBox3 inclusive(int x0,int y0,int z0, int x1,int y1,int z1) {
        return new IntBox3(x0, y0, z0, Math.addExact(x1, 1), Math.addExact(y1, 1), Math.addExact(z1, 1));
    }
}
