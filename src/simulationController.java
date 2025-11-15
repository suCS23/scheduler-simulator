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
        
        out.println("========================================");
        out.println("SYSTEM CONFIGURATION");
        out.println("========================================");
        out.println("Start Time: " + startTime);
        out.println("Total Memory: " + memory + " units");
        out.println("Total Devices: " + devices);
        out.println("Scheduler: " + schedulerType.toUpperCase() + " Round Robin");
        if (schedulerType.equalsIgnoreCase("static")) {
            out.println("Time Quantum: " + (10 + teamNumber));
        }
        out.println("========================================\n");
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
        
        out.println("\n========================================");
        out.println("SYSTEM STATE AT TIME: " + displayTime);
        out.println("========================================\n");
        
        // Display process table
        out.println("PROCESS TABLE:");
        out.println(String.format("%-6s %-10s %-10s %-10s %-10s %-12s %-12s",
            "PID", "STATE", "ARRIVAL", "BURST", "REMAINING", "FINISH", "TURNAROUND"));
        out.println("--------------------------------------------------------------------------------");
        
        process[] processes = procManager.getProcessTable();
        int count = procManager.getProcessCount();
        
        for (int i = 0; i < count; i++) {
            process p = processes[i];
            String stateStr = getStateString(p.getState());
            String remaining = p.getState() == 4 ? "0" : String.valueOf(p.getRemainingTime());
            String finish = p.getState() == 4 ? String.valueOf(p.getFinishTime()) : "N/A";
            String turnaround = p.getState() == 4 ? String.valueOf(p.getTurnaroundTime()) : "N/A";
            
            out.println(String.format("%-6d %-10s %-10d %-10d %-10s %-12s %-12s",
                p.getPid(), stateStr, p.getAt(), p.getBt(), remaining, finish, turnaround));
            
            // Track statistics for completed processes
            if (p.getState() == 4) {
                totalTurnaroundTime += p.getTurnaroundTime();
                completedProcesses++;
            }
        }
        
        out.println();
        
        // Display queues
        out.println("\nQUEUE CONTENTS:");
        out.println("--------------------------------------------------------------------------------");
        out.print("Ready Queue: ");
        printQueue(procManager.getReadyQ());
        
        out.print("Hold Queue 1 (Priority 1, Memory Ascending): ");
        printQueue(procManager.getHQ1());
        
        out.print("Hold Queue 2 (Priority 2, FIFO): ");
        printQueue(procManager.getHQ2());
        
        process running = procManager.getRunningProcess();
        out.println("\nCurrently Running: " + 
            (running != null ? "Process " + running.getPid() : "None"));
        
        // Display system resources
        out.println("\n\nSYSTEM RESOURCES:");
        out.println("--------------------------------------------------------------------------------");
        out.println("Available Memory: " + kerServices.getMemorySize() + 
            " / " + kerServices.getTotalMemorySize() + " units");
        out.println("Available Devices: " + kerServices.getNoDevs() + 
            " / " + kerServices.getTotalNoDevs());
        
        // Display scheduler info
        if (procManager.getScheduler() != null) {
            out.println("\n\nSCHEDULER METRICS:");
            out.println("--------------------------------------------------------------------------------");
            out.println("SR (Sum of Remaining Times): " + procManager.getScheduler().getSR());
            out.println("AR (Average/Time Quantum): " + procManager.getScheduler().getAR());
        }
        
        // Final statistics
        out.println("\n\n========================================");
        out.println("FINAL SYSTEM STATISTICS");
        out.println("========================================");
        out.println("Total Processes: " + count);
        out.println("Completed Processes: " + completedProcesses);
        
        if (completedProcesses > 0) {
            double avgTurnaround = totalTurnaroundTime / completedProcesses;
            out.println("Average Turnaround Time: " + String.format("%.2f", avgTurnaround));
        } else {
            out.println("Average Turnaround Time: N/A");
        }
        
        out.println("========================================\n");
    }
    
    // Helper to print queue contents
    private void printQueue(queue q) {
        if (q.isEmpty()) {
            out.println("Empty");
            return;
        }
        
        StringBuilder sb = new StringBuilder("[");
        node<process> current = q.getFront();
        while (current != null) {
            sb.append("P").append(current.p.getPid());
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]");
        out.println(sb.toString());
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
        in.close();
        out.flush();
        out.close();
    }
    
    public static void main(String[] args) {
        try {
            // Update these paths to match your directory structure
            String inputPath = "scheduelSimulator/src/IOfiles/input.txt";
            String outputPath = "scheduelSimulator/src/IOfiles/output.txt";
            
            simulationController sim = new simulationController(inputPath, outputPath);
            sim.run();
            
            System.out.println("Simulation complete! Check output.txt for results.");
            
        } catch (Exception e) {
            System.err.println("Error running simulation:");
            e.printStackTrace();
        }
    }
}