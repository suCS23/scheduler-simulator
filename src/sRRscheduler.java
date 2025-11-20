
public class sRRscheduler extends scheduler {

    // ==============================
    //           Attribute
    // ==============================
    private final int timeQuantum;
    
    // ==============================
    //           Constuctor
    // ==============================
    public sRRscheduler(int teamNumber) { 
        this.timeQuantum = 10 + teamNumber;  
    }
    
    //if the burst time of the process is less than quantum, then return the burst time
    //else return quantum
    @Override
    protected long selectNextProcess(process currentProcess) {
        return Math.min(timeQuantum, currentProcess.getRemainingTime());
    }
}