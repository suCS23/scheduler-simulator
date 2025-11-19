public class prManager {
    
    private long internalClock;
    private queue readyQ;
    private queue submitQ;
    private queue HQ1;
    private queue HQ2;
    private scheduler currentScheduler;
    private process runningProcess;
    private otherKerServices kerServices;
    private process[] processTable;  
    private int processCount;

    public prManager(otherKerServices kerServices) {
        this.internalClock = 0;
        this.readyQ = new queue("fifo");
        this.submitQ = new queue("fifo");
        this.HQ1 = new queue("rm");
        this.HQ2 = new queue("fifo");
        this.kerServices = kerServices;
        this.runningProcess = null;
        this.processTable = new process[100];
        this.processCount = 0;
    }
    
    public void setScheduler(String type, int teamNumber) {
        if (type.equalsIgnoreCase("dynamic")) {
            this.currentScheduler = new dRRscheduler(); // MODIFIED: No readyQ passed
        } else if (type.equalsIgnoreCase("static")) {
            this.currentScheduler = new sRRscheduler(teamNumber); // MODIFIED: No readyQ passed
        } else {
            this.currentScheduler = new dRRscheduler(); // MODIFIED: No readyQ passed
        }
    }

    public void procArrivalRoutine(long jobId, long arrivalTime, long runtime, int priority, long memory, int devices) {
        process p = new process(jobId, arrivalTime, runtime, priority, memory, devices, 0);
        processTable[processCount++] = p;
        
        // MODIFIED: passing resource values p.getMr(), p.getDr()
        if (kerServices.exceedsTotal(p.getMr(), p.getDr())) {
            p.setState(5);
            return;
        }
        
        // MODIFIED: passing resource values p.getMr(), p.getDr()
        if (kerServices.canAllocate(p.getMr(), p.getDr())) {
            createProcess(p);
        } else {
            p.setState(1);
            if (p.getPriority() == 1) {
                HQ1.enqueue(p);
            } else {
                HQ2.enqueue(p);
            }
        }
    }
    
    private void createProcess(process p) {
        // MODIFIED: passing resource values
        kerServices.allocateMemory(p.getMr());
        kerServices.reserveDevices(p.getDr());
        p.setState(2);
        readyQ.enqueue(p);
        
        if (currentScheduler != null) {
            // MODIFIED: passing readyQ to the method
            currentScheduler.updateMetrics(readyQ);
        }
    }
    
    private long dispatch() {
        if (runningProcess == null) {
            if (!readyQ.isEmpty()) {
                node nextNode = readyQ.dequeue();
                runningProcess = nextNode.p;
                runningProcess.setState(3);
            } else {
                return Long.MAX_VALUE;
            }
        }
        
        // MODIFIED: passing readyQ to the method
        long timeQuantum = currentScheduler.selectNextProcess(runningProcess, readyQ);
        return timeQuantum;
    }
    
    public void cpuTimeAdvance(long duration) {
        if (runningProcess != null) {
            runningProcess.setRemainingTime(runningProcess.getRemainingTime() - duration);
            
            if (runningProcess.getRemainingTime() <= 0) {
                terminateProcess(runningProcess);
            } else {
                runningProcess.setState(2);
                readyQ.enqueue(runningProcess);
                runningProcess = null;
            }
            
            if (currentScheduler != null) {
                // MODIFIED: passing readyQ to the method
                currentScheduler.updateMetrics(readyQ);
            }
        }
        
        internalClock += duration;
    }
    
    private void terminateProcess(process p) {
        p.setState(4);
        p.setFinishTime(internalClock);
        
        // MODIFIED: passing resource values
        kerServices.deallocateMemory(p.getMr());
        kerServices.releaseDevices(p.getDr());
        runningProcess = null;
        
        checkHoldQueues();
    }
    
    private void checkHoldQueues() {
        // MODIFIED: passing resource values
        while (!HQ1.isEmpty() && kerServices.canAllocate(HQ1.peek().p.getMr(), HQ1.peek().p.getDr())) {
            node pNode = HQ1.dequeue();
            createProcess(pNode.p);
        }
        
        // MODIFIED: passing resource values
        while (!HQ2.isEmpty() && kerServices.canAllocate(HQ2.peek().p.getMr(), HQ2.peek().p.getDr())) {
            node pNode = HQ2.dequeue();
            createProcess(pNode.p);
        }
    }
    
    public long getNextDecisionTime() {
        if (runningProcess != null) {
            long timeQuantum = dispatch();
            return internalClock + timeQuantum;
        } else if (!readyQ.isEmpty()) {
            dispatch();
            if (runningProcess != null) {
                // MODIFIED: passing readyQ to the method
                long timeQuantum = currentScheduler.selectNextProcess(runningProcess, readyQ);
                return internalClock + timeQuantum;
            }
        }
        return Long.MAX_VALUE;
    }

    public int getFinishedProcessCount() {
        int finished = 0;
        for (int i = 0; i < processCount; i++) {
            if (processTable[i].getState() == 4) {  // TERMINATED
                finished++;
            }
            }
        return finished;
    }

    public String getFinishedProcessesString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < processCount; i++) {
            process p = processTable[i];

            if (p.getState() == 4) { 

                double waitingTime = p.getTurnaroundTime() - p.getBt();

                sb.append(String.format(
                    "  %-6d %-15.2f %-16.2f %-17.2f %-13.2f%n",
                    p.getPid(),
                    (double) p.getAt(),
                    (double) p.getFinishTime(),
                    (double) p.getTurnaroundTime(),
                    waitingTime
                ));
            }
        }

        return sb.toString();
    }

    
    public long getRunningProcId() {
        return runningProcess != null ? runningProcess.getPid() : -1;
    }
    
    public void setInternalClock(long time) {
        this.internalClock = time;
    }
    
    public long getInternalClock() {
        return internalClock;
    }
    
    public process[] getProcessTable() {
        return processTable;
    }
    
    public int getProcessCount() {
        return processCount;
    }
    
    public process getRunningProcess() {
        return runningProcess;
    }
    
    public scheduler getScheduler() {
        return currentScheduler;
    }

    // NEW GETTER for simulationController
    public String getReadyQueueContentString() {
        // Delegate formatting to the queue object
        return readyQ.getQueueContentString();
    }
    
    // NEW GETTER for simulationController
    public String getHQ1ContentString() {
        // Delegate formatting to the queue object
        return HQ1.getQueueContentString();
    }

    // NEW GETTER for simulationController
    public String getHQ2ContentString() {
        // Delegate formatting to the queue object
        return HQ2.getQueueContentString();
    }
}