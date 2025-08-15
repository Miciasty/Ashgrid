package nsk.nu.ashgrid.api.grid.indexing;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/** Square chunks in XZ plane of fixed size (cells), Y is unbounded. */
public final class SquareXZChunkScheme implements ChunkScheme {
    private final int size;

    /**
     * @param size chunk size along X and Z in cells (must be > 0)
     */
    public SquareXZChunkScheme(int size) {
        if (size <= 0) throw new IllegalArgumentException("size > 0 required");
        this.size = size;
    }

    @Override public int chunkSize() { return size; }

    @Override
    public ChunkIndex2 chunkOfPoint(Vector3 w) {
        int cx = (int)Math.floor(w.x() / size);
        int cz = (int)Math.floor(w.z() / size);
        return new ChunkIndex2(cx, cz);
    }

    @Override
    public CellIndex3 cellOfPoint(Vector3 w) {
        return new CellIndex3((int)Math.floor(w.x()),
                (int)Math.floor(w.y()),
                (int)Math.floor(w.z()));
    }

    @Override
    public AxisAlignedBox chunkBounds(ChunkIndex2 c) {
        double x0 = (double)c.cx() * size;
        double z0 = (double)c.cz() * size;
        double x1 = x0 + size;
        double z1 = z0 + size;
        return new AxisAlignedBox(x0, Double.NEGATIVE_INFINITY, z0,
                x1, Double.POSITIVE_INFINITY, z1);
    }

    @Override
    public Iterable<ChunkIndex2> neighbors4(ChunkIndex2 c) {
        List<ChunkIndex2> out = new ArrayList<>(4);
        out.add(new ChunkIndex2(c.cx()+1, c.cz()));
        out.add(new ChunkIndex2(c.cx()-1, c.cz()));
        out.add(new ChunkIndex2(c.cx(), c.cz()+1));
        out.add(new ChunkIndex2(c.cx(), c.cz()-1));
        return out;
    }

    @Override
    public Iterable<ChunkIndex2> neighbors8(ChunkIndex2 c) {
        List<ChunkIndex2> out = new ArrayList<>(8);
        for (int dz=-1; dz<=1; dz++)
            for (int dx=-1; dx<=1; dx++)
                if (dx!=0 || dz!=0) out.add(new ChunkIndex2(c.cx()+dx, c.cz()+dz));
        return out;
    }
}
