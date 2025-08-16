package nsk.nu.ashgrid;

import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.util.GridServices;
import nsk.nu.ashgrid.api.voxel.ops.morphology.Morphology;
import nsk.nu.ashgrid.api.voxel.ops.morphology.MorphologyOps;
import nsk.nu.ashgrid.implementation.raster.arrays.ArrayGrid3i;
import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
0 0 0
0 1 0   -> dilate(N6) -> cross (5 ones)
0 0 0
*/
class MorphologyTest {

    @Test
    void dilate_n6_creates_cross_of_five() {
        // GIVEN
        Morphology morph = GridServices.require(Morphology.class, "MorphologyBasic");
        ArrayGrid3i src = new ArrayGrid3i(3,3,1);
        src.set(1,1,0, 1);
        Grid3i dst = new ArrayGrid3i(3,3,1);
        IntPredicate fg = v -> v == 1;

        // WHEN
        morph.dilate(src, fg, dst, Morphology.Neighborhood.N6);

        // THEN
        int ones = 0;
        for (int y=0;y<3;y++) for (int x=0;x<3;x++) if (dst.get(x,y,0)==1) ones++;
        assertEquals(5, ones);
        assertEquals(1, dst.get(1,1,0));
    }

    @Test void opening_removes_noise_and_preserves_3d_cross() {
        // GIVEN
        Morphology morph = GridServices.require(Morphology.class, "MorphologyBasic");
        ArrayGrid3i src = new ArrayGrid3i(3,3,3);
        src.set(0,0,0, 1);
        int cx=1, cy=1, cz=1;
        src.set(cx,cy,cz,1);
        src.set(cx+1,cy,cz,1); src.set(cx-1,cy,cz,1);
        src.set(cx,cy+1,cz,1); src.set(cx,cy-1,cz,1);
        src.set(cx,cy,cz+1,1); src.set(cx,cy,cz-1,1);

        Grid3i tmp = new ArrayGrid3i(3,3,3);
        Grid3i dst = new ArrayGrid3i(3,3,3);

        // WHEN
        MorphologyOps.open(morph, src, v -> v==1, tmp, dst, Morphology.Neighborhood.N6);

        // THEN
        assertEquals(0, dst.get(0,0,0));
        assertEquals(1, dst.get(1,1,1));

        int ones=0;
        for(int z=0;z<3;z++) for(int y=0;y<3;y++) for(int x=0;x<3;x++) if(dst.get(x,y,z)==1) ones++;
        assertEquals(7, ones);
    }
}