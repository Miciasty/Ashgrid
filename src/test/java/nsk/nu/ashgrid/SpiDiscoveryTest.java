package nsk.nu.ashgrid;

import nsk.nu.ashgrid.api.util.GridServices;
import nsk.nu.ashgrid.api.voxel.draw.Line3D;
import nsk.nu.ashgrid.api.voxel.draw.Line3DSupercover;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.api.voxel.ops.distance.DistanceTransform;
import nsk.nu.ashgrid.api.voxel.ops.floodfill.FloodFill;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpiDiscoveryTest {
    @Test void services_are_discoverable() {
        // GIVEN
        // WHEN
        // THEN
        assertFalse(GridServices.byId(VoxelTraverser.class).isEmpty());
        assertFalse(GridServices.byId(Line3D.class).isEmpty());
        assertFalse(GridServices.byId(Line3DSupercover.class).isEmpty());
        assertFalse(GridServices.byId(FloodFill.class).isEmpty());
        assertFalse(GridServices.byId(ConnectedComponents.class).isEmpty());
        assertFalse(GridServices.byId(Morphology.class).isEmpty());
        assertFalse(GridServices.byId(DistanceTransform.class).isEmpty());
    }
}