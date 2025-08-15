package nsk.nu.ashgrid.implementation.voxel.ops.components;

import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.voxel.neighborhood.Neighborhood3D;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;

import java.util.ArrayDeque;
import java.util.function.IntPredicate;

/** BFS-based connected components labelling. */
public final class ConnectedComponentsBFS implements ConnectedComponents {

    private record P(int x,int y,int z){}

    @Override
    public int label(Grid3i src, IntPredicate isForeground, Grid3i labelsOut, Neighborhood nh) {
        int w = src.width(), h = src.height(), d = src.depth();
        int[][] offs = switch (nh) {
            case N6 -> Neighborhood3D.N6;
            case N18 -> Neighborhood3D.N18;
            case N26 -> Neighborhood3D.N26;
        };

        int current = 0;
        ArrayDeque<P> q = new ArrayDeque<>();
        for (int z=0; z<d; z++)
            for (int y=0; y<h; y++)
                for (int x=0; x<w; x++) {
                    if (labelsOut.get(x,y,z) != 0) continue;
                    int val = src.get(x,y,z);
                    if (!isForeground.test(val)) { labelsOut.set(x,y,z, 0); continue; }

                    current++;
                    labelsOut.set(x,y,z, current);
                    q.add(new P(x,y,z));
                    while (!q.isEmpty()) {
                        P p = q.removeFirst();
                        for (int[] o : offs) {
                            int nx=p.x+o[0], ny=p.y+o[1], nz=p.z+o[2];
                            if (nx<0||nx>=w||ny<0||ny>=h||nz<0||nz>=d) continue;
                            if (labelsOut.get(nx,ny,nz) != 0) continue;
                            int nv = src.get(nx,ny,nz);
                            if (!isForeground.test(nv)) { labelsOut.set(nx,ny,nz, 0); continue; }
                            labelsOut.set(nx,ny,nz, current);
                            q.add(new P(nx,ny,nz));
                        }
                    }
                }
        return current;
    }
}