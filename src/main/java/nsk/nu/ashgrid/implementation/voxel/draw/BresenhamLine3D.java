package nsk.nu.ashgrid.implementation.voxel.draw;

import nsk.nu.ashgrid.api.voxel.draw.Line3D;

/**
 * Integer 3D Bresenham line rasterization with early exit.
 * Single-loop implementation parametrized by the dominant axis.
 */
public final class BresenhamLine3D implements Line3D {
    @Override public String id() { return "bresenham3d"; }

    @Override
    public void trace(int x0, int y0, int z0, int x1, int y1, int z1, CellVisitor v) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int dz = Math.abs(z1 - z0), sz = z0 < z1 ? 1 : -1;

        if (dx == 0 && dy == 0 && dz == 0) { v.visit(x0,y0,z0); return; }

        int[] p = { x0, y0, z0 };
        int[] q = { x1, y1, z1 };
        int[] s = { sx, sy, sz };
        int[] d = { dx, dy, dz };

        int m  = dominantAxis(dx, dy, dz);
        int m1 = (m + 1) % 3;
        int m2 = (m + 2) % 3;

        int A1 = d[m1] << 1, A2 = d[m2] << 1, Am = d[m] << 1;
        int err1 = A1 - d[m], err2 = A2 - d[m];

        while (true) {
            if (!v.visit(p[0], p[1], p[2])) return;
            if (p[m] == q[m]) break;

            if (err1 >= 0) { p[m1] += s[m1]; err1 -= Am; }
            if (err2 >= 0) { p[m2] += s[m2]; err2 -= Am; }

            p[m] += s[m];
            err1 += A1;
            err2 += A2;
        }
    }

    private static int dominantAxis(int dx, int dy, int dz) {
        if (dx >= dy && dx >= dz) return 0;
        if (dy >= dz) return 1;
        return 2;
    }
}