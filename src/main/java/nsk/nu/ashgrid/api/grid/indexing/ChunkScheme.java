package nsk.nu.ashgrid.api.grid.indexing;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;

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
}