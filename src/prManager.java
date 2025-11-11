public class prManager {
    
    private long internalClock;
    private queue readyQ = new queue("scheduler");
    private queue submitQ = new queue("waiting");
    private queue HQ1 = new queue("rm");
    private queue HQ2 = new queue("fifo");

    public prManager(long internalClock) {
        this.internalClock = internalClock;
    }

    private void dispatch() {}

    public void procArivvingRoutine() {}

    public void cpuTimeAdvanced(long duration) {}

    public long getNextDecisionTime() {return 0;}

    public long getRunningProcId() {return 0;}

}
