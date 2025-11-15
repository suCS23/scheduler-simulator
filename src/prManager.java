public class prManager {
    
    private long internalClock;
    private queue readyQ;
    private queue submitQ;
    private queue HQ1;
    private queue HQ2;
    private scheduler currentScheduler;
    private process runningProcess;
    private otherKerServices kerServices;
    private process[] processTable;  // Array of all processes
    private int processCount;

    public prManager(otherKerServices kerServices) {
        this.internalClock = 0;
        this.readyQ = new queue("fifo");      // FIFO ready queue
        this.submitQ = new queue("fifo");      // Submit queue
        this.HQ1 = new queue("rm");            // HQ1: sorted by memory (ascending)
        this.HQ2 = new queue("fifo");          // HQ2: FIFO
        this.kerServices = kerServices;
        this.runningProcess = null;
        this.processTable = new process[100];  // As per spec
        this.processCount = 0;
    }
    
    // Set scheduler type (dynamic or static RR)
    public void setScheduler(String type, int teamNumber) {
        if (type.equalsIgnoreCase("dynamic")) {
            this.currentScheduler = new dRRscheduler(readyQ);
        } else if (type.equalsIgnoreCase("static")) {
            this.currentScheduler = new sRRscheduler(readyQ, teamNumber);
        } else {
            // Default to dynamic
            this.currentScheduler = new dRRscheduler(readyQ);
        }
    }

    // Handle arriving process (called by SimulationController)
    public void procArrivalRoutine(process p) {
        // Add to process table
        processTable[processCount++] = p;
        
        // Check if job exceeds TOTAL system resources
        if (kerServices.exceedsTotal(p)) {
            p.setState(5);  // REJECTED
            return;
        }
        
        // Check if enough AVAILABLE resources
        if (kerServices.canAllocate(p)) {
            // Allocate and move to ready queue
            createProcess(p);
        } else {
            // Put in hold queue based on priority
            p.setState(1);  // HOLD state
            if (p.getPriority() == 1) {
                HQ1.enqueue(p);  // Priority 1 -> HQ1 (sorted by memory)
            } else {
                HQ2.enqueue(p);  // Priority 2 -> HQ2 (FIFO)
            }
        }
    }
    
    // Create process and allocate resources
    private void createProcess(process p) {
        kerServices.allocateMemory(p);
        kerServices.reserveDevices(p);
        p.setState(2);  // READY state
        readyQ.enqueue(p);
        
        // Update scheduler metrics
        if (currentScheduler != null) {
            currentScheduler.updateMetrics();
        }
    }
    
    // Dispatch - load process from ready queue to CPU
    private long dispatch() {
        // If no process running, get next from ready queue
        if (runningProcess == null) {
            if (!readyQ.isEmpty()) {
                node<process> nextNode = readyQ.dequeue();
                runningProcess = nextNode.p;
                runningProcess.setState(3);  // RUNNING state
            } else {
                return Long.MAX_VALUE;  // No process to run
            }
        }
        
        // Calculate time quantum using scheduler
        long timeQuantum = currentScheduler.selectNextProcess(runningProcess);
        return timeQuantum;
    }
    
    // Advance CPU time by duration
    public void cpuTimeAdvance(long duration) {
        if (runningProcess != null) {
            // Reduce remaining time
            runningProcess.setRemainingTime(runningProcess.getRemainingTime() - duration);
            
            if (runningProcess.getRemainingTime() <= 0) {
                // Process terminates
                terminateProcess(runningProcess);
            } else {
                // Time quantum expired, return to ready queue
                runningProcess.setState(2);  // READY
                readyQ.enqueue(runningProcess);
                runningProcess = null;
            }
            
            // Update scheduler metrics after process change
            if (currentScheduler != null) {
                currentScheduler.updateMetrics();
            }
        }
        
        // Update internal clock
        internalClock += duration;
    }
    
    // Terminate process and release resources
    private void terminateProcess(process p) {
        p.setState(4);  // TERMINATED
        p.setFinishTime(internalClock);
        
        // Release resources
        kerServices.deallocateMemory(p);
        kerServices.releaseDevices(p);
        runningProcess = null;
        
        // Check hold queues (HQ1 first, then HQ2)
        checkHoldQueues();
    }
    
    // Check if processes in hold queues can move to ready queue
    private void checkHoldQueues() {
        // Check HQ1 first (higher priority)
        while (!HQ1.isEmpty() && kerServices.canAllocate(HQ1.peek().p)) {
            node<process> pNode = HQ1.dequeue();
            createProcess(pNode.p);
        }
        
        // Then check HQ2
        while (!HQ2.isEmpty() && kerServices.canAllocate(HQ2.peek().p)) {
            node<process> pNode = HQ2.dequeue();
            createProcess(pNode.p);
        }
    }
    
    // Get next internal event time (when running process finishes or time slice expires)
    public long getNextDecisionTime() {
        if (runningProcess != null) {
            long timeQuantum = dispatch();
            return internalClock + timeQuantum;
        } else if (!readyQ.isEmpty()) {
            // Start next process immediately
            dispatch();
            if (runningProcess != null) {
                long timeQuantum = currentScheduler.selectNextProcess(runningProcess);
                return internalClock + timeQuantum;
            }
        }
        return Long.MAX_VALUE;  // No internal events
    }
    
    // Get running process ID
    public long getRunningProcId() {
        return runningProcess != null ? runningProcess.getPid() : -1;
    }
    
    // Update internal clock (called by SimulationController)
    public void setInternalClock(long time) {
        this.internalClock = time;
    }
    
    public long getInternalClock() {
        return internalClock;
    }
    
    // Getters for display/statistics
    public process[] getProcessTable() {
        return processTable;
    }
    
    public int getProcessCount() {
        return processCount;
    }
    
    public queue getReadyQ() {
        return readyQ;
    }
    
    public queue getHQ1() {
        return HQ1;
    }
    
    public queue getHQ2() {
        return HQ2;
    }
    
    public process getRunningProcess() {
        return runningProcess;
    }
    
    public scheduler getScheduler() {
        return currentScheduler;
    }
}