package nsk.nu.ashgrid.implementation.raster.chunked;

import nsk.nu.ashcore.api.math.DivMod;
import nsk.nu.ashgrid.api.raster.SparseGrid3i;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

import java.util.HashMap;
import java.util.Map;

/**
 * Unbounded sparse grid backed by lazily allocated fixed-size chunks.
 * Cells from non-materialized chunks return {@code defaultValue}.
 */
public final class ChunkedGrid3i implements SparseGrid3i {
    private final int cw;
    private final int ch;
    private final int cd;
    private final int defaultValue;
    private final Map<ChunkKey3, ArrayGrid3i> chunks = new HashMap<>();

    public ChunkedGrid3i(int chunkW, int chunkH, int chunkD) {
        this(chunkW, chunkH, chunkD, 0);
    }

    public ChunkedGrid3i(int chunkW, int chunkH, int chunkD, int defaultValue) {
        if (chunkW <= 0 || chunkH <= 0 || chunkD <= 0) {
            throw new IllegalArgumentException("chunk dimensions must be > 0");
        }
        this.cw = chunkW;
        this.ch = chunkH;
        this.cd = chunkD;
        this.defaultValue = defaultValue;
    }

    @Override
    public int get(int x, int y, int z) {
        ArrayGrid3i chunk = chunks.get(chunkOfCell(x, y, z));
        if (chunk == null) return defaultValue;
        int[] l = local(x, y, z);
        return chunk.get(l[0], l[1], l[2]);
    }

    @Override
    public void set(int x, int y, int z, int value) {
        ChunkKey3 key = chunkOfCell(x, y, z);
        if (value == defaultValue && !chunks.containsKey(key)) return;

        ArrayGrid3i chunk = chunks.computeIfAbsent(key, ignored -> newChunk());
        int[] l = local(x, y, z);
        chunk.set(l[0], l[1], l[2], value);
    }

    @Override
    public boolean has(int x, int y, int z) {
        return chunks.containsKey(chunkOfCell(x, y, z));
    }

    @Override
    public int defaultValue() {
        return defaultValue;
    }

    /** @return chunk width in cells */
    public int chunkWidth() { return cw; }

    /** @return chunk height in cells */
    public int chunkHeight() { return ch; }

    /** @return chunk depth in cells */
    public int chunkDepth() { return cd; }

    /**
     * @deprecated Use {@link #has(int, int, int)}. This method checks whether the backing chunk is materialized.
     */
    @Deprecated(forRemoval = false)
    public boolean inside(int x, int y, int z) { return has(x, y, z); }

    private ChunkKey3 chunkOfCell(int x, int y, int z) {
        return new ChunkKey3(
                DivMod.floorDiv(x, cw),
                DivMod.floorDiv(y, ch),
                DivMod.floorDiv(z, cd)
        );
    }

    private int[] local(int x, int y, int z) {
        return new int[]{
                DivMod.floorMod(x, cw),
                DivMod.floorMod(y, ch),
                DivMod.floorMod(z, cd)
        };
    }

    private ArrayGrid3i newChunk() {
        ArrayGrid3i chunk = new ArrayGrid3i(cw, ch, cd);
        if (defaultValue == 0) return chunk;

        for (int z = 0; z < cd; z++) {
            for (int y = 0; y < ch; y++) {
                for (int x = 0; x < cw; x++) {
                    chunk.set(x, y, z, defaultValue);
                }
            }
        }
        return chunk;
    }
}
