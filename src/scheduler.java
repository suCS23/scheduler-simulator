// scheduler.java (UPDATED)

public abstract class scheduler {
    
    // REMOVED: protected queue readyQ;
    protected long SR;       // Sum of remaining burst times
    protected long AR;       // Average of remaining burst times
    
    public scheduler() { // MODIFIED: No longer takes readyQ
        this.SR = 0;
        this.AR = 0;
    }
    
    // Abstract method - now requires the readyQ to be passed for inspection
    protected abstract long selectNextProcess(process currentProcess, queue readyQ);
    
    // Update SR and AR based on ready queue. Now takes readyQ as argument.
    protected void updateMetrics(queue readyQ) {
        SR = 0;
        int count = 0;
        
        node current = readyQ.getFront();
        while (current != null) {
            SR += current.p.getRemainingTime();
            count++;
            current = current.next;
        }
        
        if (count > 0) {
            AR = SR / count;
        } else {
            AR = 0;
        }
    }
    
    // Getters
    public long getSR() {
        return SR;
    }
    
    public long getAR() {
        return AR;
    }
}