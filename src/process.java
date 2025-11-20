public class process {

    // ==============================
    //           Attributes
    // ==============================
    private long pid;
    private long at;
    private long bt; 
    private int priority;
    private long mr; 
    private int dr; 
    private int state;
    private long turnaroundTime;
    private long remainingTime;
    private long finishTime;
    
    // ==============================
    //          Constructor
    // ==============================
    public process(long pid, long at, long bt, int priority, long mr, int dr, int state) {
        this.pid = pid;
        this.at = at;
        this.bt = bt;
        this.priority = priority;
        this.mr = mr;
        this.dr = dr;
        this.state = state;
        this.remainingTime = bt;
        this.turnaroundTime = 0;
    }

    // ==============================
    //       Setters & Getters
    // ==============================
    public long getPid() { return pid; }
    public void setPid(long pid) { this.pid = pid; }
    public long getAt() { return at; }
    public void setAt(long at) { this.at = at; }
    public long getBt() { return bt; }
    public void setBt(long bt) { this.bt = bt; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public long getMr() { return mr; }
    public void setMr(long mr) { this.mr = mr; }
    public int getDr() { return dr; }
    public void setDr(int dr) { this.dr = dr; }
    public int getState() { return state; }
    public void setState(int state) { this.state = state; }
    public long getRemainingTime() { return remainingTime; }
    public void setRemainingTime(long remainingTime) { this.remainingTime = remainingTime; }
    public long getFinishTime() { return finishTime; }
    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
        this.turnaroundTime = finishTime - at;
    }
    public long getTurnaroundTime() { return turnaroundTime; }
}