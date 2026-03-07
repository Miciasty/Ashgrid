package nsk.nu.ashgrid.api.grid.indexing;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashgrid.api.grid.bounds.IntBox3;
import nsk.nu.ashgrid.api.grid.bounds.IntRect2;

/**
 * Maps continuous world coordinates to discrete chunk and cell indices
 * and provides neighborhood utilities.
 */
public interface ChunkScheme {

    /** Size of a chunk along X and Z axes in cells. */
    int chunkSize();

    /** Map a world-space point to the containing chunk (floor division). */
    ChunkIndex2 chunkOfPoint(Vector3 world);

    /** Map a world-space point to the containing cell (floor). */
    CellIndex3 cellOfPoint(Vector3 world);

    /** Axis-aligned world-space bounds of a chunk. */
    AxisAlignedBox chunkBounds(ChunkIndex2 c);

    /** 4-neighborhood over chunks (N,E,S,W). */
    Iterable<ChunkIndex2> neighbors4(ChunkIndex2 c);

    /** 8-neighborhood over chunks. */
    Iterable<ChunkIndex2> neighbors8(ChunkIndex2 c);

    /**
     * Returns chunk range intersected by a finite world-space AABB on XZ.
     * The result is half-open in chunk space: {@code [cx0,cx1) x [cz0,cz1)}.
     */
    default IntRect2 chunksInAABB(AxisAlignedBox box) {
        double minX = box.min().x();
        double minZ = box.min().z();
        double maxX = box.max().x();
        double maxZ = box.max().z();

        requireFinite(minX, "box.min.x");
        requireFinite(minZ, "box.min.z");
        requireFinite(maxX, "box.max.x");
        requireFinite(maxZ, "box.max.z");

        ChunkIndex2 c0 = chunkOfPoint(new Vector3(minX, 0.0, minZ));
        if (maxX <= minX || maxZ <= minZ) {
            return new IntRect2(c0.cx(), c0.cz(), c0.cx(), c0.cz());
        }

        ChunkIndex2 c1 = chunkOfPoint(new Vector3(Math.nextDown(maxX), 0.0, Math.nextDown(maxZ)));
        return new IntRect2(c0.cx(), c0.cz(), c1.cx() + 1, c1.cz() + 1);
    }

    /**
     * Returns cell range intersected by a finite world-space AABB.
     * The result is half-open in cell space: {@code [min,max)}.
     */
    default IntBox3 cellsInAABB(AxisAlignedBox box) {
        double minX = box.min().x();
        double minY = box.min().y();
        double minZ = box.min().z();
        double maxX = box.max().x();
        double maxY = box.max().y();
        double maxZ = box.max().z();

        int x0 = floorToInt(minX, "box.min.x");
        int y0 = floorToInt(minY, "box.min.y");
        int z0 = floorToInt(minZ, "box.min.z");

        if (maxX <= minX || maxY <= minY || maxZ <= minZ) {
            return new IntBox3(x0, y0, z0, x0, y0, z0);
        }

        int x1 = exclusiveUpperBound(maxX, "box.max.x");
        int y1 = exclusiveUpperBound(maxY, "box.max.y");
        int z1 = exclusiveUpperBound(maxZ, "box.max.z");
        return new IntBox3(x0, y0, z0, x1, y1, z1);
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }

    private static int floorToInt(double value, String name) {
        requireFinite(value, name);
        double f = Math.floor(value);
        if (f < Integer.MIN_VALUE || f > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(name + " out of int range: " + value);
        }
        return (int) f;
    }

    private static int exclusiveUpperBound(double value, String name) {
        int lastIncluded = floorToInt(Math.nextDown(value), name);
        if (lastIncluded == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(name + " out of int range: " + value);
        }
        return lastIncluded + 1;
    }
}
