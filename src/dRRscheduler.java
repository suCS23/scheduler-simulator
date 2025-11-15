public class dRRscheduler extends scheduler {
    
    public dRRscheduler(queue readyQ) {
        super(readyQ);
    }
    
    @Override
    protected long selectNextProcess(process currentProcess) {
        if (currentProcess == null) {
            return 0;
        }
        
        // Update SR and AR before calculating time quantum
        updateMetrics();
        
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