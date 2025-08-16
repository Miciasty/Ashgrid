package nsk.nu.ashgrid.implementation.voxel.draw;

import nsk.nu.ashgrid.api.voxel.draw.Line3DSupercover;

/** Integer 3D supercover line with proper edge/corner coverage. */
public final class SupercoverLine3D implements Line3DSupercover {
    @Override public String id() { return "supercover3d"; }

    @Override
    public void trace(int x0,int y0,int z0, int x1,int y1,int z1, CellVisitor v) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : (x0 > x1 ? -1 : 0);
        int dy = Math.abs(y1 - y0), sy = y0 < y1 ? 1 : (y0 > y1 ? -1 : 0);
        int dz = Math.abs(z1 - z0), sz = z0 < z1 ? 1 : (z0 > z1 ? -1 : 0);

        int x = x0, y = y0, z = z0;
        if (!v.visit(x,y,z)) return;

        int ax = 2*dx, ay = 2*dy, az = 2*dz;

        if (dx >= dy && dx >= dz) {            // drive X
            int ey = ay - dx, ez = az - dx;
            while (x != x1) {
                while (ey >= 0) { y += sy; ey -= ax; if (!v.visit(x,y,z)) return; }
                while (ez >= 0) { z += sz; ez -= ax; if (!v.visit(x,y,z)) return; }
                x += sx; ey += ay; ez += az;
                if (!v.visit(x,y,z)) return;
            }
        } else if (dy >= dx && dy >= dz) {     // drive Y
            int ex = ax - dy, ez = az - dy;
            while (y != y1) {
                while (ex >= 0) { x += sx; ex -= ay; if (!v.visit(x,y,z)) return; }
                while (ez >= 0) { z += sz; ez -= ay; if (!v.visit(x,y,z)) return; }
                y += sy; ex += ax; ez += az;
                if (!v.visit(x,y,z)) return;
            }
        } else {                                // drive Z
            int ex = ax - dz, ey = ay - dz;
            while (z != z1) {
                while (ex >= 0) { x += sx; ex -= az; if (!v.visit(x,y,z)) return; }
                while (ey >= 0) { y += sy; ey -= az; if (!v.visit(x,y,z)) return; }
                z += sz; ex += ax; ey += ay;
                if (!v.visit(x,y,z)) return;
            }
        }
    }
}