// dRRscheduler.java (UPDATED)

public class dRRscheduler extends scheduler {
    
    public dRRscheduler() { // MODIFIED: Removed readyQ from constructor
        super();
    }
    
    @Override
    // MODIFIED: Takes readyQ
    protected long selectNextProcess(process currentProcess, queue readyQ) {
        if (currentProcess == null) {
            return 0;
        }
        
        // Update SR and AR before calculating time quantum, passing the queue
        updateMetrics(readyQ);
        
        long timeQuantum;
        
        if (readyQ.isEmpty()) {
            // If ready queue is empty, TQ = remaining burst time (run to completion)
            timeQuantum = currentProcess.getRemainingTime();
        } else {
            // If ready queue has processes, TQ = AR (average)
            timeQuantum = AR;
            if (timeQuantum <= 0) {
                timeQuantum = currentProcess.getRemainingTime();
            }
        }
        
        // Return actual time to run (min of quantum and remaining time)
        return Math.min(timeQuantum, currentProcess.getRemainingTime());
    }
}