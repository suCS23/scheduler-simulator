
public class dRRscheduler extends scheduler {
    
    // ==============================
    //           Attributes
    // ==============================
    protected long SR; //sum of remianing time
    protected long AR; //mean of remaining time

    // ==============================
    //            Selector
    // ==============================
    @Override
    protected long selectNextProcess(process currentProcess) {
        return (this.AR > 0)? Math.min(this.AR, currentProcess.getRemainingTime()) : currentProcess.getRemainingTime();
    }
    
    // ==============================
    //            Mutator
    // ==============================
    public void setter(queue readyQ) {

        //step 1 : sum all the remaining times of all processes
        node current = readyQ.peek(); this.SR = 0;
        while (current != null) {
            this.SR += current.p.getRemainingTime();
            current = current.next;
        }
        
        //step 2 : get the mean 
        this.AR = (readyQ.getSize() > 0)? (long)(Math.ceil((double)this.SR / readyQ.getSize())) : 0;
    }
}