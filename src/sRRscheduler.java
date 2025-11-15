public class sRRscheduler extends scheduler {

    private int timeQuantum;
    
    public sRRscheduler(queue readyQ, int teamNumber) {
        super(readyQ);
        this.timeQuantum = 10 + teamNumber;  // Fixed quantum = 10 + team number
    }
    
    @Override
    protected long selectNextProcess(process currentProcess) {
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