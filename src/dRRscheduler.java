
public class dRRscheduler extends scheduler {
    
    protected long SR;
    protected long AR;
    
    public long getSR() { return SR; }
    public long getAR() { return AR; }

    @Override
    protected long selectNextProcess(process currentProcess) {
        // Rule 1: "For the first process, it begins with time quantum equals to the burst time"
        if (this.AR == 0) {
            return currentProcess.getRemainingTime();
        }
        
        // Rule 2: Use AR (Average Remaining) as the Time Quantum
        long timeQuantum = this.AR;

        // Safety check: TQ cannot be 0 or negative
        if (timeQuantum <= 0) {
            timeQuantum = currentProcess.getRemainingTime();
        }
        
        return Math.min(timeQuantum, currentProcess.getRemainingTime());
    }
    
    public void updateMetrics(queue readyQ) {
        long totalSR = 0;
        int count = 0;
        
        node current = readyQ.peek();
        while (current != null) {
            totalSR += current.p.getRemainingTime();
            count++;
            current = current.next;
        }
        
        this.SR = totalSR;
        
        if (count > 0) {
            this.AR = (long)(Math.ceil((double)totalSR / count));
        } else {
            this.AR = 0;
        }
    }
}