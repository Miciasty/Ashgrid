package nsk.nu.ashgrid.implementation.voxel.ops.components;

import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.voxel.neighborhood.Neighborhood3D;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;

import java.util.ArrayDeque;
import java.util.function.IntPredicate;

/**
 * BFS-based connected components labeling with flattened loops and guard-style branching.
 * Clears labelsOut up front to avoid per-cell zero writes.
 */
public final class ConnectedComponentsBFS implements ConnectedComponents {

    private record P(int x, int y, int z) {}

    @Override
    public int label(Grid3i src, IntPredicate isForeground, Grid3i labelsOut, Neighborhood nh) {
        final int w = src.width(), h = src.height(), d = src.depth();
        final int wh = w * h, total = wh * d;
        final int[][] offs = switch (nh) {
            case N6  -> Neighborhood3D.N6;
            case N18 -> Neighborhood3D.N18;
            case N26 -> Neighborhood3D.N26;
        };

        clear(labelsOut);

        int current = 0;
        final ArrayDeque<P> q = new ArrayDeque<>();

        for (int i = 0; i < total; i++) {
            final int z = i / wh;
            final int rem = i - z * wh;
            final int y = rem / w;
            final int x = rem - y * w;

            if (labelsOut.get(x, y, z) != 0) continue;
            if (!isForeground.test(src.get(x, y, z))) continue;

            current++;
            labelsOut.set(x, y, z, current);
            q.add(new P(x, y, z));

            bfsExpand(src, labelsOut, isForeground, offs, w, h, d, current, q);
        }

        return current;
    }

    private static void bfsExpand(Grid3i src,
                                  Grid3i labelsOut,
                                  IntPredicate isForeground,
                                  int[][] offs,
                                  int w, int h, int d,
                                  int label,
                                  ArrayDeque<P> q) {
        while (!q.isEmpty()) {
            P p = q.removeFirst();
            for (int[] o : offs) {
                int nx = p.x + o[0], ny = p.y + o[1], nz = p.z + o[2];
                if (nx < 0 || nx >= w || ny < 0 || ny >= h || nz < 0 || nz >= d) continue;
                if (labelsOut.get(nx, ny, nz) != 0) continue;
                if (!isForeground.test(src.get(nx, ny, nz))) continue;

                labelsOut.set(nx, ny, nz, label);
                q.add(new P(nx, ny, nz));
            }
        }
    }

    private static void clear(Grid3i g) {
        final int w = g.width(), h = g.height(), d = g.depth();
        final int wh = w * h, total = wh * d;
        for (int i = 0; i < total; i++) {
            final int z = i / wh;
            final int rem = i - z * wh;
            final int y = rem / w;
            final int x = rem - y * w;
            g.set(x, y, z, 0);
        }
    }
}