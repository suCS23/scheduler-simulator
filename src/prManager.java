@SuppressWarnings("FieldMayBeFinal")
public class prManager {

    // ==============================
    //           Attributes
    // ==============================
    private long internalClock;
    private scheduler currentScheduler;
    private process runningProcess;
    private otherKerServices kerServices;
    private int processCount;
    
    private queue readyQ = new queue("fifo");
    private queue submitQ = new queue("fifo");
    private queue HQ1 = new queue("rm");
    private queue HQ2 = new queue("fifo");
    

    private process[] processTable = new process[100];
    private long currentQuantumEndTime = Long.MAX_VALUE;

    // ==============================
    //         Constructor
    // ==============================
    public prManager(otherKerServices kerServices, String type, int teamNumber) {
        this.kerServices = kerServices;
        this.currentScheduler = ("static".equals(type))? new sRRscheduler(teamNumber): new dRRscheduler();
    }

    // ==============================
    //        Arrival routine
    // ==============================
    public void procArrivalRoutine(long jobId, long arrivalTime, long runtime, int priority, long memory, int devices) {
        
        //part 1 : create a program and put it int the submit queue
        process p = new process(jobId, arrivalTime, runtime, priority, memory, devices, 0);
        processTable[processCount++] = p;
        submitQ.enqueue(p);

        //part 2 : check for memory & device bandwidth
        //if the memory or the device need is more than what the kernel could handle, then drop it
        if (!(kerServices.getTotalMemorySize()< memory || kerServices.getTotalNoDevs() < devices)) {

            //if there is enough space & devices for process, then submit to readyQ
            //else, submit to HQ
            if (kerServices.canAllocate(p.getMr(), p.getDr())) {
                moveToReadyQ(submitQ.dequeue().p);
            }else {

                submitQ.dequeue();
                p.setState(1);

                //if it has higher priority, then pass it to HQ1
                //else, pass it to HQ2
                if (p.getPriority() == 1) HQ1.enqueue(p);
                else HQ2.enqueue(p);
                }
        }else
            submitQ.dequeue();
    }

    // ==============================
    //          Ready queue
    // ==============================
    private void moveToReadyQ(process p) {

        kerServices.allocateMemory(p.getMr());
        kerServices.reserveDevices(p.getDr());
        p.setState(2);
        
        //if the scheduel type is dynamic, then update metrics
        readyQ.enqueue(p);
        if(currentScheduler instanceof dRRscheduler rRscheduler) rRscheduler.updateMetrics(readyQ);
    }

    // ==============================
    //           Dispatch
    // ==============================
    // dispacthes processes to rummimg queue
    private void dispatch() {

        //if there is no running process, run one
        if (runningProcess == null) {
        
            //if exists a program, run it and set the quantum time
            //else, reset quantum time
            if (!readyQ.isEmpty()) {
                runningProcess = readyQ.dequeue().p;
                runningProcess.setState(3);

                currentQuantumEndTime = internalClock + currentScheduler.selectNextProcess(runningProcess);
            }
            else {
                currentQuantumEndTime = Long.MAX_VALUE;
            }
        }

    }

    // ==============================
    //           CPU time
    // ==============================
    // update internal clock and check if the process finished or 
    // reached quantum
    public void cpuTimeAdvance(long duration) {
        
        //if duration is valid, continue 
        if (! (duration < 0)){

            //if there is a running process, check the time scheduel
            //else, if quantum is reached, increment the internal clock
            if (runningProcess != null) {

                runningProcess.setRemainingTime(runningProcess.getRemainingTime() - duration);
                internalClock += duration;
    
                //if the process burst time done, terminate it
                //else, put it in hold queue
                if (runningProcess.getRemainingTime() <= 0) {

                    terminateProcess(runningProcess);
                    
                } else if (internalClock >= currentQuantumEndTime) {
    
                    runningProcess.setState(2);
                    readyQ.enqueue(runningProcess);
    
                    if(currentScheduler instanceof dRRscheduler rRscheduler) rRscheduler.updateMetrics(readyQ);
    
                    runningProcess = null;
                    currentQuantumEndTime = Long.MAX_VALUE;
                }
            }
            else {
                internalClock += duration;
            }
        }

    }

    // ==============================
    //          Termination
    // ==============================
    private void terminateProcess(process p) {
        //step 1 : set the state
        p.setState(4);
        p.setFinishTime(internalClock);

        //step 2 : release the memory & devices held
        kerServices.deallocateMemory(p.getMr());
        kerServices.releaseDevices(p.getDr());

        runningProcess = null;
        currentQuantumEndTime = Long.MAX_VALUE;

        //step 3 : check hold queues for other processes
        checkHoldQueues();
    }

    // ==============================
    //          Check HQ
    // ==============================
    private void checkHoldQueues() {

        //move process to readyQ till condtion aren't met
        while (!HQ1.isEmpty() && kerServices.canAllocate(HQ1.peek().p.getMr(), HQ1.peek().p.getDr())) {
            moveToReadyQ(HQ1.dequeue().p);
        }

        //move process to readyQ till condtion aren't met
        while (!HQ2.isEmpty() && kerServices.canAllocate(HQ2.peek().p.getMr(), HQ2.peek().p.getDr())){
            moveToReadyQ(HQ2.dequeue().p);
        }
    }

    // ==============================
    //        Internal event
    // ==============================
    // return the next process's arrival time
    public long getNextDecisionTime() {

        //step 1 : call dispatch
        dispatch();
        //step 2 : return the arriving time of next event, else return infinite
        return (runningProcess != null)? 
            Math.min(internalClock + runningProcess.getRemainingTime(), currentQuantumEndTime) : Long.MAX_VALUE;
    }

    // ==============================
    //           Display
    // ==============================
    public int getFinishedProcessCount() {
        int c = 0;
        for (int i = 0; i < processCount; i++)
            if (processTable[i].getState() == 4)
                c++;
        return c;
    }

    public String getFinishedProcessesString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < processCount; i++) {
            process p = processTable[i];
            if (p.getState() == 4) {

                double wt = p.getTurnaroundTime() - p.getBt();

                sb.append(String.format(
                        "  %-6d %-15.2f %-16.2f %-17.2f %-13.2f%n",
                        p.getPid(),
                        (double)p.getAt(),
                        (double)p.getFinishTime(),
                        (double)p.getTurnaroundTime(),
                        wt
                ));
            }
        }
        return sb.toString();
    }

    // ==============================
    //       Setters & Getters
    // ==============================
    public String getReadyQueueContentString() { return readyQ.getQueueContentString(); }
    public String getHQ1ContentString() { return HQ1.getQueueContentString(); }
    public String getHQ2ContentString() { return HQ2.getQueueContentString(); }

    public void setInternalClock(long t) { internalClock = t; }
    public long getInternalClock() { return internalClock; }

    public process[] getProcessTable() { return processTable; }
    public int getProcessCount() { return processCount; }
}
