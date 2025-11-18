// sRRscheduler.java (UPDATED)

public class sRRscheduler extends scheduler {

    private int timeQuantum;
    
    public sRRscheduler(int teamNumber) { // MODIFIED: Removed readyQ from constructor
        super();
        this.timeQuantum = 10 + teamNumber;  // Fixed quantum = 10 + team number
    }
    
    @Override
    // MODIFIED: Takes readyQ, but ignores it since it's a Static RR
    protected long selectNextProcess(process currentProcess, queue readyQ) {
        if (currentProcess == null) {
            return 0;
        }
        
        // Static RR uses fixed time quantum
        // Return actual time to run (min of quantum and remaining time)
        return Math.min(timeQuantum, currentProcess.getRemainingTime());
    }
    
    public int getTimeQuantum() {
        return timeQuantum;
    }
}