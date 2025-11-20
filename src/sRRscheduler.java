
public class sRRscheduler extends scheduler {

    private final int timeQuantum;
    
    public sRRscheduler(int teamNumber) { // MODIFIED: Removed readyQ from constructor
        this.timeQuantum = 10 + teamNumber;  // Fixed quantum = 10 + team number
    }
    
    @Override
    protected long selectNextProcess(process currentProcess) {
        return Math.min(timeQuantum, currentProcess.getRemainingTime());
    }
    
    public int getTimeQuantum() {
        return timeQuantum;
    }
}