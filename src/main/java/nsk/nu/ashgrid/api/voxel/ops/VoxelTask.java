package nsk.nu.ashgrid.api.voxel.ops;

/**
 * Synchronous, single-threaded work cursor. Pause by not calling step; resume on the same cursor.
 * Cancellation is terminal and leaves partial output. There is no scheduler or rollback.
 * Budgets count algorithm work units, not time, allocation, or work inside user callbacks.
 */
public abstract class VoxelTask implements AutoCloseable {
    public enum Status { RUNNING, COMPLETED, CANCELLED, FAILED }

    private Status status=Status.RUNNING;
    private long workDone;
    private boolean stepping;

    public final Status status(){ return status; }
    public final boolean isDone(){ return status!=Status.RUNNING; }
    public final long workDone(){ return workDone; }

    /** Performs at most maxWork units; returns the number performed. Zero is a no-op. */
    public final int step(int maxWork){
        if (maxWork<0) throw new IllegalArgumentException("maxWork must be >= 0");
        if (stepping) throw new IllegalStateException("task is already stepping");
        int done=0;
        stepping=true;
        try {
            while (done<maxWork && !isDone()) {
                boolean last=advance();
                done++; workDone++;
                if (last) complete();
            }
            return done;
        } catch (RuntimeException | Error e) {
            status=Status.FAILED;
            onClose();
            throw e;
        } finally { stepping=false; }
    }

    public final void runToCompletion(){ while (!isDone()) step(Integer.MAX_VALUE); }

    public final void cancel(){
        if (stepping) throw new IllegalStateException("cannot cancel inside a task callback");
        if (!isDone()) { status=Status.CANCELLED; onClose(); }
    }

    /** Cancels unfinished work; also releases an exclusively held workspace. */
    @Override public final void close(){ cancel(); }

    /** Performs one bounded algorithm unit and returns true when all work is complete. */
    protected abstract boolean advance();
    protected void onClose(){}

    protected final void complete(){
        if (!isDone()) { status=Status.COMPLETED; onClose(); }
    }
}
