package nsk.nu.ashgrid.api.voxel.query;

import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashgrid.api.voxel.traversal.VoxelTraverser;

public final class Raycast {
    private final VoxelTraverser traverser;
    public record Hit(int x,int y,int z,double tEnter,double tExit){}
    public Raycast(VoxelTraverser traverser){ this.traverser = traverser; }

    /** Returns first hit or null. */
    public Hit first(Ray ray, double tMax, Occupancy occ) {
        final Hit[] out = new Hit[1];
        traverser.traverse(ray, tMax, (x,y,z,t0,t1) -> {
            if (occ.test(x,y,z)) { out[0] = new Hit(x,y,z,t0,t1); return false; }
            return true;
        });
        return out[0];
    }

    @FunctionalInterface public interface Occupancy { boolean test(int x,int y,int z); }
}