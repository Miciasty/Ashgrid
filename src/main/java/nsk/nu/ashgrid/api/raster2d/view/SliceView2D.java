package nsk.nu.ashgrid.api.raster2d.view;

import nsk.nu.ashgrid.api.raster.Grid3i;
import nsk.nu.ashgrid.api.raster2d.Grid2i;

/**
 * 2D slice view of a 3D grid. Axis order:
 *  - XY at fixed z
 *  - XZ at fixed y
 *  - YZ at fixed x
 */
public final class SliceView2D implements Grid2i {
    public enum Plane { XY, XZ, YZ }

    private final Grid3i src;
    private final Plane plane;
    private final int fixed;

    public SliceView2D(Grid3i src, Plane plane, int fixedIndex) {
        this.src = src; this.plane = plane; this.fixed = fixedIndex;
    }

    @Override public int get(int u,int v) {
        switch (plane) {
            case XY -> { return src.get(u, v, fixed); }
            case XZ -> { return src.get(u, fixed, v); }
            case YZ -> { return src.get(fixed, u, v); }
        }
        throw new IllegalStateException();
    }

    @Override public void set(int u,int v,int value) {
        switch (plane) {
            case XY -> src.set(u, v, fixed, value);
            case XZ -> src.set(u, fixed, v, value);
            case YZ -> src.set(fixed, u, v, value);
        }
    }

    @Override public boolean inside(int u,int v) {
        return switch (plane) {
            case XY -> src.inside(u, v, fixed);
            case XZ -> src.inside(u, fixed, v);
            case YZ -> src.inside(fixed, u, v);
        };
    }

    @Override public int width()  { return plane == Plane.YZ ? src.height() : src.width(); }
    @Override public int height() { return plane == Plane.XZ ? src.depth()  : (plane == Plane.XY ? src.height() : src.depth()); }
}
