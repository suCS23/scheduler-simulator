public abstract class scheduler {
    
    protected queue readyQ;  // Reference to ready queue
    protected long SR;       // Sum of remaining burst times
    protected long AR;       // Average of remaining burst times
    
    public scheduler(queue readyQ) {
        this.readyQ = readyQ;
        this.SR = 0;
        this.AR = 0;
    }
    
    // Abstract method - each scheduler implements differently
    protected abstract long selectNextProcess(process currentProcess);
    
    // Update SR and AR based on ready queue
    protected void updateMetrics() {
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
