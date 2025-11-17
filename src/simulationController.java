import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class simulationController {

    private long curTime;
    
    // Declaring I/O files
    private File inputFile;
    private File outputFile;

    // Declaring I/O reader & writer
    private Scanner in;
    private PrintWriter out;
    
    // System components
    private prManager procManager;
    private otherKerServices kerServices;
    
    // Configuration
    private String schedulerType = "dynamic";  // "dynamic" or "static"
    private int teamNumber = 2;  // Team number 2
    
    // Statistics
    private double totalTurnaroundTime;
    private int completedProcesses;
    
    public simulationController(String inputPath, String outputPath) throws Exception {
        this.inputFile = new File(inputPath);
        this.outputFile = new File(outputPath);
        this.in = new Scanner(inputFile);
        this.out = new PrintWriter(outputFile);
        this.curTime = 0;
        this.totalTurnaroundTime = 0;
        this.completedProcesses = 0;
    }
    
    public void run() {
        while (in.hasNext()) {
            String command = in.next();
            String line = in.hasNext() ? in.nextLine().trim() : "";
            
            switch (command) {
                case "C":
                    sysGen(line);
                    break;
                case "A":
                    parseCmd(line);
                    break;
                case "D":
                    displayFinalStatistics(line);
                    break;
                default:
                    System.out.println("Non-eligible command: " + command);
            }
        }
        
        close();
    }

    public void sysGen(String line) {
        // Parse: time M=memory S=devices
        String[] parts = line.trim().split("\\s+");
        long startTime = Long.parseLong(parts[0]);
        long memory = Long.parseLong(parts[1].split("=")[1]);
        int devices = Integer.parseInt(parts[2].split("=")[1]);
        
        // Initialize system
        curTime = startTime;
        kerServices = new otherKerServices(memory, devices);
        procManager = new prManager(kerServices);
        procManager.setScheduler(schedulerType, teamNumber);
        procManager.setInternalClock(startTime);
        
        // Print configuration header
        String schedulerName = schedulerType.equalsIgnoreCase("dynamic") ? "DynamicRR" : "StaticRR";
        out.printf("CONFIG at %.2f: mem=%d devices=%d scheduler=%s%n%n", 
            (double)startTime, memory, devices, schedulerName);
    }

    public void parseCmd(String line) {
        // Parse: time J=id M=memory S=devices R=runtime P=priority
        String[] parts = line.trim().split("\\s+");
        long arrivalTime = Long.parseLong(parts[0]);
        long jobId = Long.parseLong(parts[1].split("=")[1]);
        long memory = Long.parseLong(parts[2].split("=")[1]);
        int devices = Integer.parseInt(parts[3].split("=")[1]);
        long runtime = Long.parseLong(parts[4].split("=")[1]);
        int priority = Integer.parseInt(parts[5].split("=")[1]);
        
        // Process all events until arrival time using min(i,e)
        processEventsUntil(arrivalTime);
        
        // Create and add new process
        process newProc = new process(jobId, arrivalTime, runtime, priority, memory, devices, 0);
        procManager.procArrivalRoutine(newProc);
    }
    
    // Process events until target time using min(i,e) logic
    private void processEventsUntil(long targetTime) {
        while (curTime < targetTime) {
            long nextInternalEvent = procManager.getNextDecisionTime();
            long nextExternalEvent = targetTime;
            
            // min(i, e) - if equal, process internal first
            if (nextInternalEvent <= nextExternalEvent) {
                // Internal event (process termination or time slice)
                if (nextInternalEvent == Long.MAX_VALUE) {
                    // No internal events, jump to external
                    curTime = nextExternalEvent;
                    break;
                }
                
                long duration = nextInternalEvent - curTime;
                if (duration > 0) {
                    curTime = nextInternalEvent;
                    procManager.cpuTimeAdvance(duration);
                }
            } else {
                // External event (job arrival)
                curTime = nextExternalEvent;
                break;
            }
        }
        
        // Sync internal clock
        procManager.setInternalClock(curTime);
    }

    public void displayFinalStatistics(String line) {
        long displayTime = Long.parseLong(line.trim());
        
        // Process all events until display time
        processEventsUntil(displayTime);
        
        out.println("\n-------------------------------------------------------");
        out.println("System Status:                                         ");
        out.println("-------------------------------------------------------");
        out.printf("          Time: %.2f%n", (double)displayTime);
        out.printf("  Total Memory: %d%n", kerServices.getTotalMemorySize());
        out.printf(" Avail. Memory: %d%n", kerServices.getMemorySize());
        out.printf(" Total Devices: %d%n", kerServices.getTotalNoDevs());
        out.printf("Avail. Devices: %d%n", kerServices.getNoDevs());
        out.println();
        
        // Jobs in Ready List
        out.println("Jobs in Ready List                                      ");
        out.println("--------------------------------------------------------");
        printReadyQueue(procManager.getReadyQ());
        out.println();
        
        // Jobs in Long Job List (not used in our implementation)
        out.println("Jobs in Long Job List                                   ");
        out.println("--------------------------------------------------------");
        out.println("  EMPTY");
        out.println();
        
        // Jobs in Hold List 1
        out.println("Jobs in Hold List 1                                     ");
        out.println("--------------------------------------------------------");
        printHoldQueue(procManager.getHQ1());
        out.println();
        
        // Jobs in Hold List 2
        out.println("Jobs in Hold List 2                                     ");
        out.println("--------------------------------------------------------");
        printHoldQueue(procManager.getHQ2());
        out.println();
        out.println();
        
        // Finished Jobs (detailed)
        out.println("Finished Jobs (detailed)                                ");
        out.println("--------------------------------------------------------");
        out.println("  Job    ArrivalTime     CompleteTime     TurnaroundTime    WaitingTime");
        out.println("------------------------------------------------------------------------");
        
        process[] processes = procManager.getProcessTable();
        int count = procManager.getProcessCount();
        int finishedCount = 0;
        
        for (int i = 0; i < count; i++) {
            process p = processes[i];
            if (p.getState() == 4) { // TERMINATED
                finishedCount++;
                double waitingTime = p.getTurnaroundTime() - p.getBt();
                out.printf("  %-6d %-15.2f %-16.2f %-17.2f %-13.2f%n",
                    p.getPid(), 
                    (double)p.getAt(), 
                    (double)p.getFinishTime(),
                    (double)p.getTurnaroundTime(),
                    waitingTime);
                
                totalTurnaroundTime += p.getTurnaroundTime();
            }
        }
        
        if (finishedCount == 0) {
            out.println("  EMPTY");
        }
        
        completedProcesses = finishedCount;
        out.println("Total Finished Jobs:             " + completedProcesses);
        out.println();
        out.println();
    }
    
    // Helper to print ready queue
    private void printReadyQueue(queue q) {
        if (q.isEmpty()) {
            out.println("  EMPTY");
            return;
        }
        
        node current = q.getFront();
        while (current != null) {
            out.printf("Job ID %d , %.2f Cycles left to completion.%n", 
                current.p.getPid(), (double)current.p.getRemainingTime());
            current = current.next;
        }
    }
    
    // Helper to print hold queues
    private void printHoldQueue(queue q) {
        if (q.isEmpty()) {
            out.println("  EMPTY");
            return;
        }
        
        node current = q.getFront();
        while (current != null) {
            out.printf("Job ID %d , %.2f Cycles left to completion.%n", 
                current.p.getPid(), (double)current.p.getRemainingTime());
            current = current.next;
        }
    }
    
    // Helper to convert state code to string
    private String getStateString(int state) {
        switch (state) {
            case 0: return "NEW";
            case 1: return "HOLD";
            case 2: return "READY";
            case 3: return "RUNNING";
            case 4: return "TERMINATED";
            case 5: return "REJECTED";
            default: return "UNKNOWN";
        }
    }
    
    private void close() {
        out.println("\n--- Simulation finished at time " + curTime + ".0 ---\n");
        in.close();
        out.flush();
        out.close();
    }
    
    public static void main(String[] args) {
        try {
            // Absolute paths
            String inputPath = "C:\\Users\\PC\\OneDrive\\Desktop\\CODING-GARAGE\\scheduelSimulator\\src\\IOfiles\\inputD0.txt";
            String outputPath = "C:\\Users\\PC\\OneDrive\\Desktop\\CODING-GARAGE\\scheduelSimulator\\src\\IOfiles\\\\output.txt";
            
            simulationController sim = new simulationController(inputPath, outputPath);
            sim.run();
            
            System.out.println("Simulation complete! Check output.txt for results.");
            
        } catch (Exception e) {
            System.err.println("Error running simulation:");
            e.printStackTrace();
        }
    }
}