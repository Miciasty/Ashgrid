package nsk.nu.ashgrid.implementation.raster.chunked;

import nsk.nu.ashcore.api.math.DivMod;
import nsk.nu.ashgrid.api.raster.SparseGrid3i;
import nsk.nu.ashgrid.api.raster.StoredGrid3i;
import nsk.nu.ashgrid.api.raster.util.GridMath;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Unbounded sparse grid backed by lazily allocated fixed-size chunks.
 * Cells from non-materialized chunks return {@code defaultValue}.
 */
public final class ChunkedGrid3i implements SparseGrid3i, StoredGrid3i {
    private final int cw;
    private final int ch;
    private final int cd;
    private final int defaultValue;
    private final Map<ChunkKey3, ArrayGrid3i> chunks = new HashMap<>();

    public ChunkedGrid3i(int chunkW, int chunkH, int chunkD) {
        this(chunkW, chunkH, chunkD, 0);
    }

    public ChunkedGrid3i(int chunkW, int chunkH, int chunkD, int defaultValue) {
        GridMath.cellCount(chunkW, chunkH, chunkD);
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

    public int chunkCount(){ return chunks.size(); }

    /** Allocated int slots, including defaults and inaccessible edge-chunk padding. Not JVM heap bytes. */
    public long allocatedCellCount(){ return (long)chunks.size()*GridMath.cellCount(cw,ch,cd); }

    @Override public void clear(){ chunks.clear(); }

    /** Removes a chunk by chunk coordinates (floor division of cell coordinates). */
    public boolean removeChunk(int cx,int cy,int cz){ return chunks.remove(new ChunkKey3(cx,cy,cz))!=null; }

    /** O(allocated cells). Setting cells to default does not automatically remove their chunk. */
    public int pruneEmptyChunks(){
        int before=chunks.size();
        chunks.values().removeIf(chunk -> countStored(chunk)==0);
        return before-chunks.size();
    }

    /** O(allocated cells). */
    @Override public long storedCellCount(){
        long count=0;
        for (ArrayGrid3i chunk : chunks.values()) count+=countStored(chunk);
        return count;
    }

    /** Deterministic chunk Z,Y,X order, including empty materialized chunks. No mutation in callbacks. */
    public void forEachChunk(ChunkConsumer consumer){
        Objects.requireNonNull(consumer);
        for (ChunkKey3 k : orderedChunks()) consumer.accept(k.cx(),k.cy(),k.cz());
    }

    /** O(c log c + allocated cells) time and O(c) temporary keys for c chunks. */
    @Override public void forEachStored(CellConsumer consumer){
        Objects.requireNonNull(consumer);
        for (ChunkKey3 k : orderedChunks()) {
            ArrayGrid3i chunk=chunks.get(k);
            for (int z=0;z<cd;z++) for (int y=0;y<ch;y++) for (int x=0;x<cw;x++) {
                int v=chunk.get(x,y,z);
                if (v==defaultValue) continue;
                consumer.accept(Math.toIntExact((long)k.cx()*cw+x),Math.toIntExact((long)k.cy()*ch+y),
                        Math.toIntExact((long)k.cz()*cd+z),v);
            }
        }
    }

    @FunctionalInterface
    public interface ChunkConsumer { void accept(int cx,int cy,int cz); }

    private List<ChunkKey3> orderedChunks(){
        return chunks.keySet().stream().sorted(Comparator.comparingInt(ChunkKey3::cz)
                .thenComparingInt(ChunkKey3::cy).thenComparingInt(ChunkKey3::cx)).toList();
    }

    private int countStored(ArrayGrid3i chunk){
        int count=0;
        for (int z=0;z<cd;z++) for (int y=0;y<ch;y++) for (int x=0;x<cw;x++)
            if (chunk.get(x,y,z)!=defaultValue) count++;
        return count;
    }

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
