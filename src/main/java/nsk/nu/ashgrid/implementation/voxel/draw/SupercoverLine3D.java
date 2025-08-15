package nsk.nu.ashgrid.implementation.voxel.draw;

import nsk.nu.ashgrid.api.voxel.draw.Line3DSupercover;

/** Integer 3D supercover line with early exit. */
public final class SupercoverLine3D implements Line3DSupercover {
    @Override public String id() { return "supercover3d"; }

    @Override
    public void trace(int x0,int y0,int z0, int x1,int y1,int z1, CellVisitor v) {
        int dx = Math.abs(x1-x0), dy = Math.abs(y1-y0), dz = Math.abs(z1-z0);
        int sx = Integer.compare(x1,x0), sy = Integer.compare(y1,y0), sz = Integer.compare(z1,z0);

        int x=x0,y=y0,z=z0;
        int ax = 2*dx, ay=2*dy, az=2*dz;

        if (!v.visit(x,y,z)) return;

        if (dx >= dy && dx >= dz) {
            V(x1, dx, sx, sy, sz, x, y, z, ax, ay, az, v.visit(x, y, z), v);
        } else if (dy >= dx && dy >= dz) {
            V(y1, dy, sy, sx, sz, y, x, z, ay, ax, az, v.visit(x, y, z), v);
        } else {
            V(z1, dz, sz, sx, sy, z, x, y, az, ax, ay, v.visit(x, y, z), v);
        }
    }

    private void V(int x1, int dx, int sx, int sy, int sz, int x, int y, int z, int ax, int ay, int az, boolean visit, CellVisitor v) {
        int yd = ay - dx, zd = az - dx;
        while (x != x1) {
            if (yd >= 0) { y += sy; yd -= ax; if (!visit) return; }
            if (zd >= 0) { z += sz; zd -= ax; if (!visit) return; }
            x += sx; yd += ay; zd += az;
            if (!visit) return;
        }
    }
}
