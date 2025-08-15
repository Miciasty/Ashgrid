package nsk.nu.ashgrid.implementation.voxel.draw;

import nsk.nu.ashgrid.api.voxel.draw.Line3D;

/** Integer 3D Bresenham line rasterization with early exit. */
public final class BresenhamLine3D implements Line3D {
    @Override public String id() { return "bresenham3d"; }

    @Override
    public void trace(int x0, int y0, int z0, int x1, int y1, int z1, CellVisitor v) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int dz = Math.abs(z1 - z0), sz = z0 < z1 ? 1 : -1;

        int ax = 2 * dx, ay = 2 * dy, az = 2 * dz;

        int x = x0, y = y0, z = z0;

        if (dx >= dy && dx >= dz) {            // X is dominant
            int yd = ay - dx, zd = az - dx;
            while (true) {
                if (!v.visit(x,y,z)) return;
                if (x == x1) break;
                if (yd >= 0) { y += sy; yd -= ax; }
                if (zd >= 0) { z += sz; zd -= ax; }
                x += sx; yd += ay; zd += az;
            }
        } else if (dy >= dx && dy >= dz) {     // Y is dominant
            int xd = ax - dy, zd = az - dy;
            while (true) {
                if (!v.visit(x,y,z)) return;
                if (y == y1) break;
                if (xd >= 0) { x += sx; xd -= ay; }
                if (zd >= 0) { z += sz; zd -= ay; }
                y += sy; xd += ax; zd += az;
            }
        } else {                               // Z is dominant
            int xd = ax - dz, yd = ay - dz;
            while (true) {
                if (!v.visit(x,y,z)) return;
                if (z == z1) break;
                if (xd >= 0) { x += sx; xd -= az; }
                if (yd >= 0) { y += sy; yd -= az; }
                z += sz; xd += ax; yd += ay;
            }
        }
    }
}