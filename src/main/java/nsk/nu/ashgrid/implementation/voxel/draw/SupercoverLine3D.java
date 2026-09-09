package nsk.nu.ashgrid.implementation.voxel.draw;

import nsk.nu.ashgrid.api.voxel.draw.Line3DSupercover;

/** Integer 3D supercover between cell centers, including every edge/corner contact. */
public final class SupercoverLine3D implements Line3DSupercover {
    @Override public String id() { return "supercover3d"; }

    @Override
    public void trace(int x0,int y0,int z0, int x1,int y1,int z1, CellVisitor v) {
        long dx = Math.abs((long)x1-x0), dy = Math.abs((long)y1-y0), dz = Math.abs((long)z1-z0);
        int sx = Integer.compare(x1,x0), sy = Integer.compare(y1,y0), sz = Integer.compare(z1,z0);
        int x=x0, y=y0, z=z0;
        long nx=1, ny=1, nz=1;
        if (!v.visit(x,y,z)) return;

        while (x!=x1 || y!=y1 || z!=z1) {
            // Crossing times are odd/(2*delta). Compare rational values without rounding.
            long n=nx, d=dx;
            if (compare(ny,dy,n,d) < 0) { n=ny; d=dy; }
            if (compare(nz,dz,n,d) < 0) { n=nz; d=dz; }
            int axes = (compare(nx,dx,n,d)==0 ? 1 : 0)
                    | (compare(ny,dy,n,d)==0 ? 2 : 0)
                    | (compare(nz,dz,n,d)==0 ? 4 : 0);
            for (int mask=1; mask<=7; mask++) {
                if ((mask & axes) != mask) continue;
                if (!v.visit(x + ((mask&1)!=0 ? sx : 0),
                        y + ((mask&2)!=0 ? sy : 0), z + ((mask&4)!=0 ? sz : 0))) return;
            }
            if ((axes&1)!=0) { x+=sx; nx+=2; }
            if ((axes&2)!=0) { y+=sy; ny+=2; }
            if ((axes&4)!=0) { z+=sz; nz+=2; }
        }
    }

    private static int compare(long a,long ad,long b,long bd) {
        if (ad==0) return bd==0 ? 0 : 1;
        if (bd==0) return -1;
        // Full int endpoint differences need up to 65 bits in the cross products.
        int high = Long.compare(Math.multiplyHigh(a,bd), Math.multiplyHigh(b,ad));
        return high!=0 ? high : Long.compareUnsigned(a*bd,b*ad);
    }
}
