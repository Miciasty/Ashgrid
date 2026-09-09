package nsk.nu.ashgrid.implementation.grid.indexing;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.grid.indexing.CellIndex3;
import nsk.nu.ashgrid.api.grid.indexing.ChunkIndex2;
import nsk.nu.ashgrid.api.grid.indexing.ChunkScheme;
import nsk.nu.ashgrid.api.raster.util.GridMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Supported public square chunk scheme in unit-grid coordinates, retained at this package path.
 * chunkBounds has only the Y span [0,1), not an infinite vertical column.
 * N4 order is +X,-X,+Z,-Z; N8 scans Z then X, omitting the center.
 * Neighbor lists are fresh values; overflowing neighbor indices throw ArithmeticException.
 */
public final class SquareXZChunkScheme implements ChunkScheme {
    private final int size;

    private static final int[][] N4 = { {1,0},{-1,0},{0,1},{0,-1} };
    private static final int[][] N8 = {
            {-1,-1},{0,-1},{1,-1},
            {-1, 0},       {1, 0},
            {-1, 1},{0, 1},{1, 1}
    };

    public SquareXZChunkScheme(int size) {
        if (size <= 0) throw new IllegalArgumentException("size > 0 required");
        this.size = size;
    }

    @Override public int chunkSize() { return size; }

    @Override
    public ChunkIndex2 chunkOfPoint(Vector3 w) {
        return new ChunkIndex2(GridMath.floorToInt(Math.floor(w.x()) / size),
                GridMath.floorToInt(Math.floor(w.z()) / size));
    }

    @Override
    public CellIndex3 cellOfPoint(Vector3 w) {
        return new CellIndex3(GridMath.cellX(w), GridMath.cellY(w), GridMath.cellZ(w));
    }

    @Override
    public AxisAlignedBox chunkBounds(ChunkIndex2 c) {
        double x0 = (double)c.cx() * size;
        double z0 = (double)c.cz() * size;
        return new AxisAlignedBox(
                new Vector3(x0, 0.0, z0),
                new Vector3(x0 + size, 1.0, z0 + size)
        );
    }

    @Override public Iterable<ChunkIndex2> neighbors4(ChunkIndex2 c) { return neighbors(c, N4); }
    @Override public Iterable<ChunkIndex2> neighbors8(ChunkIndex2 c) { return neighbors(c, N8); }

    private static Iterable<ChunkIndex2> neighbors(ChunkIndex2 c, int[][] offs) {
        List<ChunkIndex2> out = new ArrayList<>(offs.length);
        int cx = c.cx(), cz = c.cz();
        for (int[] o : offs) out.add(new ChunkIndex2(Math.addExact(cx, o[0]), Math.addExact(cz, o[1])));
        return out;
    }
}
