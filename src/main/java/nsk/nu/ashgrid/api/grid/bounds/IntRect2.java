package nsk.nu.ashgrid.api.grid.bounds;

/**
 * Integer rectangle in XZ for chunk ranges, half-open: [cx0,cx1) x [cz0,cz1).
 * Size helpers throw ArithmeticException if the span exceeds int.
 */
public record IntRect2(int cx0, int cz0, int cx1, int cz1) {
    public IntRect2 {
        if (cx1 < cx0 || cz1 < cz0) throw new IllegalArgumentException("max < min");
    }
    public int width()  { return Math.subtractExact(cx1, cx0); }
    public int depth()  { return Math.subtractExact(cz1, cz0); }
    public boolean empty(){ return cx1==cx0 || cz1==cz0; }
    public boolean contains(int cx,int cz){ return cx>=cx0 && cx<cx1 && cz>=cz0 && cz<cz1; }
}
