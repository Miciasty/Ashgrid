package nsk.nu.ashgrid.implementation.voxel.ops.floodfill;

import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.voxel.ops.floodfill.FloodFill;

import java.util.ArrayDeque;
import java.util.function.IntPredicate;

/** Iterative queue-based flood fill in 6-neighborhood. */
public final class FloodFillQueue implements FloodFill {
    @Override public String id() { return "floodfill-queue"; }

    private record P(int x,int y,int z){}
    private static final int[][] N6 = { {1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1} };

    @Override
    public int fill(ReadableGrid3i g, int sx, int sy, int sz,
                    IntPredicate canVisit, CellConsumer visit) {
        if (!g.inside(sx,sy,sz)) return 0;

        final int w = g.width(), h = g.height(), d = g.depth();
        final int wh = w * h;
        final boolean[] seen = new boolean[w * h * d];

        final ArrayDeque<P> q = new ArrayDeque<>();
        seen[idx(w,wh,sx,sy,sz)] = true;
        q.add(new P(sx,sy,sz));

        int count = 0;
        while (!q.isEmpty()) {
            P p = q.removeFirst();
            int val = g.get(p.x, p.y, p.z);
            if (!canVisit.test(val)) continue;

            visit.accept(p.x, p.y, p.z, val);
            count++;

            for (int[] o : N6) {
                int nx = p.x + o[0], ny = p.y + o[1], nz = p.z + o[2];
                if (!g.inside(nx,ny,nz)) continue;
                int i = idx(w,wh,nx,ny,nz);
                if (seen[i]) continue;
                seen[i] = true;
                q.add(new P(nx,ny,nz));
            }
        }
        return count;
    }

    private static int idx(int w, int wh, int x, int y, int z) {
        return z * wh + y * w + x;
    }
}