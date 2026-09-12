/* Ashgrid 1.3.0: coordinate, storage, view, and bulk-operation contracts. */
(() => {
  const { code, table, note } = window.WIKI_HTML;

  window.WIKI_PAGES.push(
    {
      id: 'coordinates', category: 'Grids & storage', title: 'Coordinate spaces', kind: 'concept', readingTime: 8,
      description: 'Map positions to cells and chunks, define half-open bounds, and preserve negative coordinates.',
      intro: '<p>Ashgrid separates continuous positions, integer cell indices, and chunk indices. Choose the coordinate space before you convert a position or pass it to a query. The library stores numbers; your application supplies their relationship to a Minecraft world.</p>',
      sections: [
        {
          id: 'cells-and-bounds', title: 'Cells and half-open bounds',
          html: `<p>In unit-grid coordinates, cell <code>(x, y, z)</code> occupies <code>[x, x + 1) × [y, y + 1) × [z, z + 1)</code>. Its minimum corner is <code>(x, y, z)</code>; its center is <code>(x + 0.5, y + 0.5, z + 0.5)</code>. Floor conversion assigns <code>-0.2</code> to cell <code>-1</code>, and an exact boundary at <code>0</code> to cell <code>0</code>.</p>
          <p><code>IntBox3</code> uses minimum-inclusive, maximum-exclusive bounds on all three axes. Width, height, and depth are the differences between the matching bounds. A zero span on any axis makes the box empty. <code>IntRect2</code> applies the same convention to XZ chunk ranges.</p>
          ${table(['Type or helper', 'Meaning'], [
            ['<code>CellIndex3(x, y, z)</code>', 'An integer cell index.'],
            ['<code>ChunkIndex2(cx, cz)</code>', 'A chunk index on XZ.'],
            ['<code>ChunkLocal3(lx, ly, lz)</code>', 'A value record for coordinates relative to a chunk; it does not perform conversion.'],
            ['<code>IntBox3.inclusive(x0, y0, z0, x1, y1, z1)</code>', 'Convert inclusive endpoints to half-open bounds by adding one to each maximum.'],
            ['<code>intersect(other)</code>', 'Return the overlapping box, or the empty box at the origin when there is no volume overlap.'],
            ['<code>translate(dx, dy, dz)</code> / <code>expand(dx, dy, dz)</code>', 'Move both bounds, or subtract from each minimum and add to each maximum.']
          ])}
          ${code('java', 'CellBoundsExample.java', `import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.raster.util.GridMath;

public class CellBoundsExample {
    public static void main(String[] args) {
        IntBox3 region = IntBox3.inclusive(-2, 0, 0, 1, 0, 0);
        System.out.println("width=" + region.width()
                + ", minIncluded=" + region.contains(-2, 0, 0)
                + ", maxIncluded=" + region.contains(2, 0, 0));
        System.out.println("negativeCell=" + GridMath.floorToInt(-0.2)
                + ", boundaryCell=" + GridMath.floorToInt(0.0));
    }
}`)}
          ${code('output', 'Expected output', `width=4, minIncluded=true, maxIncluded=false
negativeCell=-1, boundaryCell=0`)}
          <p>Reversed bounds throw <code>IllegalArgumentException</code>. Size and bound arithmetic throw <code>ArithmeticException</code> when an integer result would overflow. An inclusive maximum of <code>Integer.MAX_VALUE</code> cannot become an exclusive integer endpoint.</p>`
        },
        {
          id: 'world-to-voxel', title: 'Convert world positions with VoxelSpace',
          html: `<p><code>VoxelSpace</code> maps a world position to cells with <code>floor((position − origin) / scale)</code> on each axis. The scale is the cell edge length in your world units. Use <code>scale = 1</code> and <code>Vector3.ZERO</code> when one grid cell corresponds to one world unit.</p>
          <p><code>ix</code>, <code>iy</code>, and <code>iz</code> return integer indices. <code>corner</code> and <code>center</code> return world positions. The conversion preserves the X, Y, and Z axes; it applies no rotation. For a Minecraft adapter, keeping Y vertical preserves the usual world-axis mapping.</p>
          <p>Move the position, origin, or cell edge length below to inspect the X conversion. The selected cell includes its lower bound and excludes its upper bound. Y and Z use the same formula independently.</p>
          <div data-diagram="coordinates"></div>
          ${code('java', 'VoxelSpaceExample.java', `import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.voxel.space.VoxelSpace;

public class VoxelSpaceExample {
    public static void main(String[] args) {
        VoxelSpace space = new VoxelSpace(2.0, new Vector3(10, 0, -4));
        Vector3 position = new Vector3(13.9, -0.1, -0.1);
        int x = space.ix(position);
        int y = space.iy(position);
        int z = space.iz(position);
        Vector3 center = space.center(x, y, z);
        System.out.println("cellX=" + x + ", cellY=" + y + ", cellZ=" + z);
        System.out.println("centerX=" + center.x()
                + ", centerY=" + center.y() + ", centerZ=" + center.z());
    }
}`)}
          ${code('output', 'Expected output', `cellX=1, cellY=-1, cellZ=1
centerX=13.0, centerY=-1.0, centerZ=-1.0`)}
          <p>The constructor requires a finite positive scale and a finite, non-null origin. Positions and generated points must be finite, and converted cell indices must fit a signed <code>int</code>. Membership uses floor without an epsilon. Large origins or scales can lose cell detail through floating-point rounding.</p>`
        },
        {
          id: 'chunk-mapping', title: 'Map cells to XZ chunks',
          html: `<p><code>SquareXZChunkScheme(size)</code> divides X and Z into square chunks of <code>size</code> cells. The size must be positive. Its point methods accept continuous <em>unit-grid</em> coordinates, even where a parameter is named <code>world</code>. If you use a scaled or shifted <code>VoxelSpace</code>, convert the position before calling the chunk scheme.</p>
          <p>For an integer cell coordinate, use floor division to find the chunk and floor modulus to find the local coordinate. With size <code>16</code>, cell <code>-1</code> belongs to chunk <code>-1</code> at local coordinate <code>15</code>. Ordinary integer division would put this cell in the wrong chunk.</p>
          ${code('java', 'ChunkMappingExample.java', `import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.grid.indexing.ChunkIndex2;
import nsk.nu.ashgrid.api.grid.bounds.IntRect2;
import nsk.nu.ashgrid.implementation.grid.indexing.SquareXZChunkScheme;

public class ChunkMappingExample {
    public static void main(String[] args) {
        SquareXZChunkScheme scheme = new SquareXZChunkScheme(16);
        ChunkIndex2 chunk = scheme.chunkOfPoint(new Vector3(-0.2, 70, 16));
        System.out.println("chunkX=" + chunk.cx() + ", chunkZ=" + chunk.cz()
                + ", localX=" + Math.floorMod(-1, scheme.chunkSize()));
        AxisAlignedBox box = new AxisAlignedBox(
                new Vector3(0, 0, 0), new Vector3(32, 2, 32));
        IntRect2 range = scheme.chunksInAABB(box);
        System.out.println("chunkWidth=" + range.width()
                + ", chunkDepth=" + range.depth()
                + ", maxChunkIncluded=" + range.contains(2, 0));
    }
}`)}
          ${code('output', 'Expected output', `chunkX=-1, chunkZ=1, localX=15
chunkWidth=2, chunkDepth=2, maxChunkIncluded=false`)}
          <p><code>chunksInAABB</code> returns the half-open chunk range touched on XZ. <code>cellsInAABB</code> returns the half-open cell range touched in XYZ. An exact maximum on a boundary does not include the next chunk or cell. The chunk helper considers XZ thickness; the cell helper requires thickness on all three axes.</p>
          ${note('Chunk bounds cover one cell in Y', '<p><code>SquareXZChunkScheme.chunkBounds</code> returns a unit-grid box with Y range <code>[0, 1)</code>. Supply your own vertical range when querying a taller region.</p>')}
          <p><code>neighbors4</code> visits <code>+X, −X, +Z, −Z</code>. <code>neighbors8</code> scans Z first, then X, skipping the center. Both return fresh lists. Neighbor arithmetic throws <code>ArithmeticException</code> at overflowing integer indices.</p>`
        },
        {
          id: 'metadata-and-indices', title: 'Keep metadata separate from storage',
          html: `<p><code>GridInfo(space, chunks, extent, defaultValue)</code> groups a <code>VoxelSpace</code>, a <code>ChunkScheme</code>, an <code>IntBox3</code> extent, and a default integer value. It requires non-null space and chunk scheme. The extent may be <code>null</code>. Your application decides how to interpret an absent extent and keeps this metadata consistent with its grid.</p>
          <p>Creating <code>GridInfo</code> allocates no grid cells and applies no transform to a backend. A bounded grid still addresses its own cells from zero. A <a href="#/views?section=sparse-window">sparse window</a> is the adapter for a region whose source coordinates start elsewhere.</p>
          <p><code>GridMath.linearIndex(x, y, z, width, height)</code> uses <code>(z × height + y) × width + x</code>; X changes fastest. <code>unindex</code> reverses the mapping. These helpers have no depth argument, so they cannot validate an upper Z bound. <code>cellCount(width, height, depth)</code> requires positive dimensions and a volume that fits an <code>int</code>.</p>`
        }
      ]
    },
    {
      id: 'storage', category: 'Grids & storage', title: 'Grid storage', kind: 'reference', readingTime: 7,
      description: 'Choose dense, bit, hash, or chunked storage and understand defaults, allocation, and cleanup.',
      intro: '<p>Choose storage from the shape and values of your data. Bounded grids expose local dimensions. Sparse grids accept signed integer coordinates and return a configured default for missing cells. Ashgrid provides adapters when an algorithm needs a bounded integer grid.</p>',
      sections: [
        {
          id: 'choose-storage', title: 'Choose a backend',
          html: table(['Backend', 'Construction and initial values', 'Use it for'], [
            ['<code>ArrayGrid3i(width, height, depth)</code>', 'Dense integer storage, initially zero; implements <code>BoundedGrid3i</code>.', 'A fixed region with an integer value for every cell.'],
            ['<code>BitGrid3(width, height, depth)</code>', 'Boolean storage, initially false; exposes boolean <code>get</code> and <code>set</code>.', 'Occupancy values. Use <code>BitGrid3iView</code> for algorithms that read or write integers.'],
            ['<code>HashSparseGrid3i(defaultValue)</code>', 'Stores individual non-default values at signed integer XYZ coordinates.', 'Scattered cells with large gaps.'],
            ['<code>ChunkedGrid3i(chunkW, chunkH, chunkD[, defaultValue])</code>', 'Allocates dense chunks when needed. The three-argument form defaults to zero.', 'Regions where nearby cells are populated together.']
          ]) + `<p>The backend classes are supported public entry points despite their <code>implementation.raster</code> package names. Array and bit grids require positive dimensions and an integer-sized volume. The same checks apply to the dimensions of each chunk. Arithmetic validation does not guarantee that the JVM can allocate a requested volume.</p>
          <p><code>ReadableGrid3i</code> exposes reads, <code>inside</code>, and dimensions. <code>WritableGrid3i</code> exposes writes. <code>Grid3i</code> combines both; <code>BoundedGrid3i</code> identifies the finite local range <code>[0, width) × [0, height) × [0, depth)</code>. Access outside array or bit dimensions throws <code>IndexOutOfBoundsException</code>.</p>
          <p><code>ChunkedGrid3i</code> partitions all three axes using its own chunk width, height, and depth. It uses floor division and floor modulus for negative coordinates. Its storage chunks are independent of <code>SquareXZChunkScheme</code>; choose matching XZ sizes in your application if you need the same partition.</p>`
        },
        {
          id: 'sparse-defaults', title: 'Distinguish values from allocated storage',
          html: `<p><code>SparseGrid3i</code> provides <code>get</code>, <code>set</code>, <code>has</code>, and <code>defaultValue</code>. It has no finite dimensions. A missing cell is readable and returns the default; <code>has</code> tells you whether backing storage exists for its slot.</p>
          <p>For hash storage, setting a cell to the default removes its entry. For chunked storage, the first non-default write creates a whole chunk. Every cell in that chunk then has backing storage, including cells that still contain the default. Writing the default to a missing chunk does not allocate it.</p>
          ${code('java', 'SparseDefaultsExample.java', `import nsk.nu.ashgrid.implementation.raster.sparse.HashSparseGrid3i;
import nsk.nu.ashgrid.implementation.raster.chunked.ChunkedGrid3i;

public class SparseDefaultsExample {
    public static void main(String[] args) {
        HashSparseGrid3i hash = new HashSparseGrid3i(-1);
        ChunkedGrid3i chunks = new ChunkedGrid3i(4, 2, 4, -1);
        hash.set(0, 0, 0, 7);
        chunks.set(0, 0, 0, 7);
        System.out.println("hashNeighborHas=" + hash.has(1, 0, 0)
                + ", chunkNeighborHas=" + chunks.has(1, 0, 0)
                + ", neighborValue=" + chunks.get(1, 0, 0));
        System.out.println("stored=" + chunks.storedCellCount()
                + ", allocated=" + chunks.allocatedCellCount());
        hash.set(0, 0, 0, -1);
        chunks.set(0, 0, 0, -1);
        System.out.println("hashHas=" + hash.has(0, 0, 0)
                + ", chunkHas=" + chunks.has(0, 0, 0)
                + ", pruned=" + chunks.pruneEmptyChunks());
    }
}`)}
          ${code('output', 'Expected output', `hashNeighborHas=false, chunkNeighborHas=true, neighborValue=-1
stored=1, allocated=32
hashHas=false, chunkHas=true, pruned=1`)}
          <p>Test a value predicate, such as <code>value != defaultValue</code>, when you mean occupied or non-default. The deprecated <code>inside</code> methods on hash and chunked storage are aliases for <code>has</code>; they do not describe a finite grid boundary.</p>`
        },
        {
          id: 'storage-lifecycle', title: 'Inspect and release sparse storage',
          html: `<p>Both sparse backends implement <code>StoredGrid3i</code>. <code>storedCellCount</code> counts values that differ from the default. <code>forEachStored</code> visits only those values. <code>clear</code> removes every entry or chunk; later reads return the configured default.</p>
          ${table(['Operation', 'Contract'], [
            ['<code>HashSparseGrid3i.remove(x, y, z)</code>', 'Remove one stored value and return whether an entry existed.'],
            ['<code>ChunkedGrid3i.chunkCount()</code>', 'Count allocated chunks, including chunks containing only defaults.'],
            ['<code>allocatedCellCount()</code>', 'Count allocated integer slots, including default-valued slots and padding at the signed integer coordinate limits. This is not a heap-byte measurement.'],
            ['<code>removeChunk(cx, cy, cz)</code>', 'Remove the chunk at these chunk coordinates. All its values become the default. Return whether the chunk existed.'],
            ['<code>pruneEmptyChunks()</code>', 'Remove chunks containing only defaults and return the number removed. Cleanup is explicit after resetting values.'],
            ['<code>forEachChunk(consumer)</code>', 'Visit allocated chunk coordinates, including empty materialized chunks.']
          ])}
          <p>Hash iteration sorts cells by Z, then Y, then X. Chunked iteration sorts chunks by Z, Y, X and then scans each chunk in local Z, Y, X order. In both cases X changes fastest within a row. The order is independent of insertion order, but the two backends need not produce the same global cell order.</p>
          <p>Hash iteration takes <code>O(n log n)</code> time and <code>O(n)</code> temporary keys for <code>n</code> stored cells. Chunked stored-cell counting and pruning scan allocated cells. Chunked stored-cell iteration also sorts its chunk keys. These operations can do substantial work even when few values differ from the default.</p>
          ${note('Own storage while visiting it', '<p><code>StoredGrid3i</code> is not thread-safe. Do not mutate the same storage inside <code>forEachStored</code> or <code>forEachChunk</code> callbacks. Collect the changes and apply them after iteration.</p>')}
          <p>Storage lifecycle methods manage in-memory entries. They do not save a world, write files, or attach storage to Minecraft chunk loading. Your application owns those integrations.</p>`
        }
      ]
    },
    {
      id: 'views', category: 'Grids & storage', title: 'Grid views', kind: 'guide', readingTime: 8,
      description: 'Expose sparse windows, adapt bit storage, and compose live subgrids, masks, clamps, and slices.',
      intro: '<p>A view retains a reference to existing storage. Reads see later source changes, and writable views send changes to the source. Use views to adapt coordinates or access rules; use a <a href="#/grid-operations?section=snapshots">snapshot</a> when you need an independent copy.</p>',
      sections: [
        {
          id: 'sparse-window', title: 'Expose a bounded sparse window',
          html: `<p><code>SparseGridView3i</code> gives a sparse backend a finite local coordinate range. Its local cell <code>(0, 0, 0)</code> maps to the supplied source origin. Missing source cells are included in that range and read as the sparse default. Constructing a window allocates no source cells.</p>
          ${code('java', 'SparseWindowExample.java', `import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.raster.view.SparseGridView3i;
import nsk.nu.ashgrid.implementation.raster.sparse.HashSparseGrid3i;

public class SparseWindowExample {
    public static void main(String[] args) {
        HashSparseGrid3i storage = new HashSparseGrid3i(-1);
        SparseGridView3i window = new SparseGridView3i(
                storage, new IntBox3(-2, 60, -4, 2, 62, 0));
        System.out.println("width=" + window.width()
                + ", inside=" + window.inside(0, 0, 0)
                + ", value=" + window.get(0, 0, 0));
        window.set(0, 0, 0, 7);
        storage.set(-1, 60, -4, 9);
        System.out.println("sourceValue=" + storage.get(-2, 60, -4)
                + ", liveValue=" + window.get(1, 0, 0));
    }
}`)}
          ${code('output', 'Expected output', `width=4, inside=true, value=-1
sourceValue=7, liveValue=9`)}
          <p>The explicit constructor accepts <code>source, ox, oy, oz, width, height, depth</code>. Dimensions must be positive, and the last mapped cell on every axis must fit an <code>int</code>. View accesses outside its local range throw <code>IndexOutOfBoundsException</code>. Algorithms that allocate by volume may impose tighter limits than the window constructor.</p>`
        },
        {
          id: 'integer-bit-view', title: 'Use bit storage with integer algorithms',
          html: `<p><code>BitGrid3iView</code> wraps <code>BitGrid3</code> as a <code>BoundedGrid3i</code>. Reads return <code>0</code> or <code>1</code>, and writes accept only those two values. A write of any other integer throws <code>IllegalArgumentException</code>.</p>
          ${code('java', 'BitViewExample.java', `import nsk.nu.ashgrid.api.raster.view.BitGrid3iView;
import nsk.nu.ashgrid.implementation.raster.bitset.BitGrid3;

public class BitViewExample {
    public static void main(String[] args) {
        BitGrid3 bits = new BitGrid3(2, 1, 1);
        BitGrid3iView integers = new BitGrid3iView(bits);
        integers.set(1, 0, 0, 1);
        System.out.println("occupied=" + bits.get(1, 0, 0));
        bits.set(1, 0, 0, false);
        System.out.println("integerValue=" + integers.get(1, 0, 0));
    }
}`)}
          ${code('output', 'Expected output', `occupied=true
integerValue=0`)}
          <p>Use a separate integer output for algorithms that produce labels or distances larger than one. The adapter preserves the bit grid's dimensions and bounds checks.</p>`
        },
        {
          id: 'access-views', title: 'Limit a region or change access rules',
          html: table(['View', 'Reads and writes', 'Dimensions and bounds'], [
            ['<code>SubGrid3i(source, ox, oy, oz, width, height, depth)</code>', 'Map local coordinates to the source offset. Both directions remain live.', 'Positive dimensions; the complete region must fit the source dimensions. Local out-of-range access throws.'],
            ['<code>ConstGrid3i(source)</code>', 'Read through to the source. Every <code>set</code> throws <code>UnsupportedOperationException</code>.', 'Delegate dimensions and <code>inside</code> to the source.'],
            ['<code>ClampedGrid3i(source)</code>', 'Clamp each read and write coordinate to the nearest border cell.', 'Expose source dimensions; <code>inside</code> returns true for every integer coordinate.'],
            ['<code>MaskedGrid3i(source, mask, predicate)</code>', 'Read through unchanged. Write only where the mask contains the coordinate and its value passes the predicate; otherwise do nothing.', 'Expose source dimensions and <code>inside</code>. The mask and predicate govern writes only.']
          ]) + `<p>A read-only wrapper does not freeze the source. Keep the source reference private if other code must not change it. A masked view reads the current mask on every write, so later mask changes affect later writes.</p>
          ${note('Clamped reads do not enlarge a region', '<p><code>GridOps</code> validates regions against <code>width</code>, <code>height</code>, and <code>depth</code>. Passing a clamped view does not permit an oversized fill or copy. A direct out-of-range write through that view changes a border cell.</p>')}`
        },
        {
          id: 'two-dimensional-slices', title: 'Expose a two-dimensional slice',
          html: `<p><code>SliceView2D(source, plane, fixedIndex)</code> returns a live <code>Grid2i</code>. The fixed index must lie within the source dimension perpendicular to the plane. The two remaining coordinates are named <code>u</code> and <code>v</code>.</p>
          ${table(['Plane', 'Source coordinate for (u, v)', 'Slice width × height'], [
            ['<code>XY</code>', '<code>(u, v, fixedZ)</code>', 'Source width × source height'],
            ['<code>XZ</code>', '<code>(u, fixedY, v)</code>', 'Source width × source depth'],
            ['<code>YZ</code>', '<code>(fixedX, u, v)</code>', 'Source height × source depth']
          ])}
          ${code('java', 'LiveViewsExample.java', `import nsk.nu.ashgrid.api.raster.view.ClampedGrid3i;
import nsk.nu.ashgrid.api.raster.view.ConstGrid3i;
import nsk.nu.ashgrid.api.raster.view.MaskedGrid3i;
import nsk.nu.ashgrid.api.raster.view.SubGrid3i;
import nsk.nu.ashgrid.api.raster2d.view.SliceView2D;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

public class LiveViewsExample {
    public static void main(String[] args) {
        ArrayGrid3i source = new ArrayGrid3i(4, 3, 2);
        ArrayGrid3i mask = new ArrayGrid3i(4, 3, 2);
        mask.set(2, 1, 1, 1);
        MaskedGrid3i masked = new MaskedGrid3i(source, mask, v -> v == 1);
        masked.set(0, 0, 0, 8);
        masked.set(2, 1, 1, 8);
        SubGrid3i sub = new SubGrid3i(source, 1, 1, 0, 3, 2, 2);
        ConstGrid3i readOnly = new ConstGrid3i(sub);
        System.out.println("maskedValue=" + source.get(0, 0, 0)
                + ", subValue=" + readOnly.get(1, 0, 1));
        SliceView2D floor = new SliceView2D(source, SliceView2D.Plane.XZ, 1);
        floor.set(2, 1, 9);
        new ClampedGrid3i(source).set(-3, -1, -4, 5);
        System.out.println("sliceWidth=" + floor.width()
                + ", sliceHeight=" + floor.height()
                + ", liveValue=" + readOnly.get(1, 0, 1)
                + ", borderValue=" + source.get(0, 0, 0));
    }
}`)}
          ${code('output', 'Expected output', `maskedValue=0, subValue=8
sliceWidth=4, sliceHeight=2, liveValue=9, borderValue=5`)}
          <p>Slice reads, writes, and <code>inside</code> delegate to the source after coordinate mapping. A slice of a clamped source therefore keeps that source's clamping behavior.</p>`
        }
      ]
    },
    {
      id: 'grid-operations', category: 'Grids & storage', title: 'Fill, copy, and combine grids', navTitle: 'Grid operations', kind: 'guide', readingTime: 7,
      description: 'Fill half-open regions, copy overlapping views safely, take snapshots, and apply binary set operations.',
      intro: '<p><code>GridOps</code> fills or copies local grid regions and creates independent snapshots. <code>GridSets</code> combines foreground cells into binary masks. Keep the source stable while an operation runs, and give the operation exclusive access to its destination.</p>',
      sections: [
        {
          id: 'fill-regions', title: 'Fill a local region',
          html: `<p><code>GridOps.fill(destination, value)</code> fills the destination's entire dimensions. Add an <code>IntBox3</code> argument to fill a half-open region. All bounds are local to the supplied grid or view and must fit its dimensions.</p>
          <p><code>beginFill(destination, region, value)</code> returns a <code>VoxelTask</code> for incremental work. Each unit writes one cell in Z, Y, X order, with X changing fastest. A region of eight cells takes eight units and uses constant scratch space. The synchronous <code>fill</code> method runs the same task to completion.</p>
          <p>A valid empty fill is a no-op. A nonempty volume must fit an <code>int</code>. Invalid region bounds fail before any writes. A destination exception after work begins can leave an already-written prefix; writes are not transactional.</p>`
        },
        {
          id: 'copy-overlap', title: 'Copy overlapping regions safely',
          html: `<p><code>GridOps.copy(source, destination)</code> requires matching dimensions. The region overload accepts a source <code>IntBox3</code> and destination origin <code>dx, dy, dz</code>. The region must fit the source, and its translated shape must fit the destination.</p>
          <p>Copy reads the complete source region before its first write. This preserves original values when source and destination overlap, including through separate views onto the same storage.</p>
          ${code('java', 'GridCopyExample.java', `import java.util.Arrays;
import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.raster.ops.GridOps;
import nsk.nu.ashgrid.api.raster.view.SubGrid3i;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

public class GridCopyExample {
    public static void main(String[] args) {
        ArrayGrid3i grid = new ArrayGrid3i(5, 1, 1);
        GridOps.fill(grid, new IntBox3(0, 0, 0, 5, 1, 1), 1);
        for (int x = 1; x < grid.width(); x++) {
            grid.set(x, 0, 0, x + 1);
        }
        SubGrid3i source = new SubGrid3i(grid, 0, 0, 0, 4, 1, 1);
        SubGrid3i destination = new SubGrid3i(grid, 1, 0, 0, 4, 1, 1);
        GridOps.copy(source, destination);
        int[] row = new int[grid.width()];
        for (int x = 0; x < row.length; x++) {
            row[x] = grid.get(x, 0, 0);
        }
        System.out.println("row=" + Arrays.toString(row));
    }
}`)}
          ${code('output', 'Expected output', 'row=[1, 1, 2, 3, 4]')}
          <p><code>beginCopy</code> takes twice the region volume in work units: one source read and one destination write per cell. It allocates an integer scratch array before stepping, unless you provide an array with at least one entry per region cell. Keep scratch separate from both grids and from other active tasks.</p>
          <p>Canceling during the read phase leaves the destination untouched. Canceling during the write phase leaves partial changes. Empty copies are no-ops after region validation. See <a href="#/stepped-tasks">Incremental tasks</a> for stepping and cancellation.</p>`
        },
        {
          id: 'snapshots', title: 'Take an independent snapshot',
          html: `<p><code>GridOps.snapshot(source)</code> copies the whole source into a mutable dense <code>BoundedGrid3i</code>. The region overload copies only a nonempty local box and moves its minimum to <code>(0, 0, 0)</code>. Later changes to either grid do not affect the other.</p>
          ${code('java', 'GridSnapshotExample.java', `import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.raster.BoundedGrid3i;
import nsk.nu.ashgrid.api.raster.ops.GridOps;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

public class GridSnapshotExample {
    public static void main(String[] args) {
        ArrayGrid3i source = new ArrayGrid3i(4, 2, 1);
        source.set(2, 1, 0, 9);
        BoundedGrid3i snapshot = GridOps.snapshot(
                source, new IntBox3(1, 1, 0, 4, 2, 1));
        source.set(2, 1, 0, 7);
        snapshot.set(0, 0, 0, 5);
        System.out.println("width=" + snapshot.width()
                + ", snapshotValue=" + snapshot.get(1, 0, 0)
                + ", sourceValue=" + source.get(2, 1, 0)
                + ", sourceOther=" + source.get(1, 1, 0));
    }
}`)}
          ${code('output', 'Expected output', 'width=3, snapshotValue=9, sourceValue=7, sourceOther=0')}
          <p>A snapshot takes linear time and dense storage proportional to the region volume. Empty regions are rejected. It copies cell values only; it does not carry <code>GridInfo</code>, a world origin, or source-backend lifecycle state. Keep the source stable during the copy if you need a consistent captured state.</p>`
        },
        {
          id: 'set-algebra', title: 'Combine foreground cells',
          html: `<p><code>GridSets</code> applies one <code>IntPredicate</code> to input values and writes <code>1</code> for foreground or <code>0</code> for background. Original labels are not preserved. Choose the predicate to match your data, such as <code>value &gt; 0</code> for positive labels.</p>
          ${table(['Method', 'Output is 1 when'], [
            ['<code>union(a, b, predicate, out)</code>', 'Either input is foreground.'],
            ['<code>intersect(a, b, predicate, out)</code>', 'Both inputs are foreground.'],
            ['<code>subtract(a, b, predicate, out)</code>', 'A is foreground and B is background.'],
            ['<code>invert(a, predicate, out)</code>', 'A is background, within A\'s dimensions.']
          ])}
          ${code('java', 'GridSetsExample.java', `import java.util.Arrays;
import nsk.nu.ashgrid.api.raster.ops.GridSets;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;

public class GridSetsExample {
    public static void main(String[] args) {
        ArrayGrid3i a = new ArrayGrid3i(3, 1, 1);
        ArrayGrid3i b = new ArrayGrid3i(3, 1, 1);
        ArrayGrid3i out = new ArrayGrid3i(3, 1, 1);
        a.set(0, 0, 0, 7);
        a.set(1, 0, 0, 7);
        b.set(1, 0, 0, 9);
        GridSets.subtract(a, b, value -> value > 0, out);
        int[] row = new int[out.width()];
        for (int x = 0; x < row.length; x++) {
            row[x] = out.get(x, 0, 0);
        }
        System.out.println("difference=" + Arrays.toString(row));
    }
}`)}
          ${code('output', 'Expected output', 'difference=[1, 0, 0]')}
          <p>Binary inputs must have identical dimensions. An output that also implements <code>ReadableGrid3i</code> must match that shape. For a write-only output, your implementation must accept every coordinate visited in the input dimensions.</p>
          <p>These methods scan Z, Y, X synchronously and write as they go. They do not buffer an entire source like <code>GridOps.copy</code>. Use independent output storage when views could overlap at different offsets, then copy the result back if needed. Every input cell contributes to the result, including default-valued cells exposed by a bounded sparse window.</p>`
        }
      ]
    }
  );
})();
