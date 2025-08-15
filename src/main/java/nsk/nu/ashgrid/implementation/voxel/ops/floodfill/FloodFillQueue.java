package nsk.nu.ashgrid.implementation.voxel.ops.floodfill;

import nsk.nu.ashgrid.api.raster.ReadableGrid3i;
import nsk.nu.ashgrid.api.voxel.ops.floodfill.FloodFill;

import java.util.ArrayDeque;
import java.util.function.IntPredicate;

/** Iterative queue-based flood fill in 6-neighborhood. */
public final class FloodFillQueue implements FloodFill {
    @Override public String id() { return "floodfill-queue"; }

    private record P(int x,int y,int z){}

    @Override
    public int fill(ReadableGrid3i g, int sx, int sy, int sz,
                    IntPredicate canVisit, CellConsumer visit) {
        if (!g.inside(sx,sy,sz)) return 0;

        boolean[] seen = new boolean[g.width() * g.height() * g.depth()];
        ArrayDeque<P> q = new ArrayDeque<>();
        int count = 0;

        q.add(new P(sx,sy,sz));
        seen[index(g, sx,sy,sz)] = true;

        while (!q.isEmpty()) {
            P p = q.removeFirst();
            int val = g.get(p.x, p.y, p.z);
            if (!canVisit.test(val)) continue;

            visit.accept(p.x, p.y, p.z, val);
            count++;

            // 6-neighborhood
            push(g, seen, q, p.x+1, p.y, p.z);
            push(g, seen, q, p.x-1, p.y, p.z);
            push(g, seen, q, p.x, p.y+1, p.z);
            push(g, seen, q, p.x, p.y-1, p.z);
            push(g, seen, q, p.x, p.y, p.z+1);
            push(g, seen, q, p.x, p.y, p.z-1);
        }
        return count;
    }

    private static void push(ReadableGrid3i g, boolean[] seen, ArrayDeque<P> q, int x,int y,int z){
        if (!g.inside(x,y,z)) return;
        int idx = index(g,x,y,z);
        if (!seen[idx]) {
            seen[idx] = true;
            q.add(new P(x,y,z));
        }
    }

    private static int index(ReadableGrid3i g, int x,int y,int z){
        return (z * g.height() + y) * g.width() + x;
    }
}