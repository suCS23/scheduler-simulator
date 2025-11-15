public class process {
    // Existing attributes from UML
    private long pid;
    private long at;  // arrival time
    private long bt;  // burst time
    private int priority;
    private long mr;  // memory required
    private int dr;   // devices required
    private int state;
    
    // Necessary additions for tracking (not in UML but required by spec)
    private long remainingTime;  // for scheduling
    private long finishTime;     // for turnaround calculation
    private long turnaroundTime; // required by output spec
    
    public process(long pid, long at, long bt, int priority, long mr, int dr, int state) {
        this.pid = pid;
        this.at = at;
        this.bt = bt;
        this.priority = priority;
        this.mr = mr;
        this.dr = dr;
        this.state = state;
        this.remainingTime = bt;  // initially equals burst time
        this.finishTime = -1;
        this.turnaroundTime = 0;
    }

    // Existing getters/setters from your code
    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public long getAt() {
        return at;
    }

    public void setAt(long at) {
        this.at = at;
    }

    public long getBt() {
        return bt;
    }

    public void setBt(long bt) {
        this.bt = bt;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getMr() {
        return mr;
    }

    public void setMr(long mr) {
        this.mr = mr;
    }

    public int getDr() {
        return dr;
    }

    public void setDr(int dr) {
        this.dr = dr;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
    
    // Additional getters/setters for necessary tracking
    public long getRemainingTime() {
        return remainingTime;
    }
    
    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
    }
    
    public long getFinishTime() {
        return finishTime;
    }
    
    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
        // Calculate turnaround when finish time is set
        this.turnaroundTime = finishTime - at;
    }
    
    public long getTurnaroundTime() {
        return turnaroundTime;
    }
}

// State constants (recommended to add at top of file or separate class)
// 0 = NEW/SUBMIT
// 1 = HOLD
// 2 = READY
// 3 = RUNNING
// 4 = TERMINATED
// 5 = REJECTED
