package nsk.nu.ashgrid.api.raster2d;

/** 2D integer grid interface. */
public interface Grid2i {
    int get(int u,int v);
    void set(int u,int v,int value);
    boolean inside(int u,int v);
    int width();
    int height();
}