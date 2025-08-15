package nsk.nu.ashgrid.implementation.raster.chunked;

import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

import java.util.HashMap;
import java.util.Map;

/**
 * Sparse chunked integer grid: lazily allocates fixed-size chunks on write.
 * Infinite logical space; {@code inside} uses allocated chunks only.
 */
public final class ChunkedGrid3i implements Grid3i {
    private final int cw,ch,cd; // chunk dimensions
    private final Map<Long, ArrayGrid3i> chunks = new HashMap<>();

    public ChunkedGrid3i(int chunkW, int chunkH, int chunkD) {
        if (chunkW<=0||chunkH<=0||chunkD<=0) throw new IllegalArgumentException();
        this.cw=chunkW; this.ch=chunkH; this.cd=chunkD;
    }

    @Override public int get(int x,int y,int z) {
        ArrayGrid3i c = chunks.get(key(cx(x), cy(y), cz(z)));
        if (c == null) throw new IndexOutOfBoundsException("no chunk allocated at cell");
        int[] l = local(x,y,z);
        return (int) c.get(l[0], l[1], l[2]);
    }

    @Override public void set(int x,int y,int z,int v) {
        long k = key(cx(x), cy(y), cz(z));
        ArrayGrid3i c = chunks.computeIfAbsent(k, kk -> new ArrayGrid3i(cw,ch,cd));
        int[] l = local(x,y,z);
        c.set(l[0], l[1], l[2], v);
    }

    @Override public boolean inside(int x,int y,int z) {
        return chunks.containsKey(key(cx(x), cy(y), cz(z)));
    }

    // Logical size is not defined for infinite space; return chunk size for convenience
    @Override public int width()  { return cw; }
    @Override public int height() { return ch; }
    @Override public int depth()  { return cd; }

    private int cx(int x){ return floorDiv(x, cw); }
    private int cy(int y){ return floorDiv(y, ch); }
    private int cz(int z){ return floorDiv(z, cd); }

    private int[] local(int x,int y,int z){
        int lx = floorMod(x, cw);
        int ly = floorMod(y, ch);
        int lz = floorMod(z, cd);
        return new int[]{lx,ly,lz};
    }

    private static int floorDiv(int a,int m){ int q = a / m; int r = a % m; return r<0 ? q-1 : q; }
    private static int floorMod(int a,int m){ int r = a % m; return r<0 ? r+m : r; }
    private static long key(int cx,int cy,int cz){
        return (((long)cx & 0x1FFFFFL) << 42) | (((long)cy & 0x3FFFFL) << 22) | ((long)cz & 0x3FFFFFL);
    }
}