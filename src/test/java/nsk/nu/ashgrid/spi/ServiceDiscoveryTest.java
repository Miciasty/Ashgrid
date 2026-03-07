package nsk.nu.ashgrid.spi;

import nsk.nu.ashcore.api.spi.ServiceRegistry;
import nsk.nu.ashgrid.api.voxel.draw.Line3D;
import nsk.nu.ashgrid.api.voxel.draw.Line3DSupercover;
import nsk.nu.ashgrid.api.voxel.ops.components.ConnectedComponents;
import nsk.nu.ashgrid.api.voxel.ops.distance.DistanceTransform;
import nsk.nu.ashgrid.api.voxel.ops.floodfill.FloodFill;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ServiceDiscoveryTest {

    @Test
    void all_core_services_are_discoverable_via_spi() {
        // GIVEN / WHEN / THEN
        assertFalse(ServiceRegistry.of(VoxelTraverser.class).isEmpty());
        assertFalse(ServiceRegistry.of(Line3D.class).isEmpty());
        assertFalse(ServiceRegistry.of(Line3DSupercover.class).isEmpty());
        assertFalse(ServiceRegistry.of(FloodFill.class).isEmpty());
        assertFalse(ServiceRegistry.of(ConnectedComponents.class).isEmpty());
        assertFalse(ServiceRegistry.of(Morphology.class).isEmpty());
        assertFalse(ServiceRegistry.of(DistanceTransform.class).isEmpty());
    }
}
