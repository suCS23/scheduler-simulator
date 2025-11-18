import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class simulationController {

    private long curTime;
    
    //declaring I/O files, reader, writer, system components, and variables used for statistics
    private File inputFile;
    private File outputFile;
    private Scanner in;
    private PrintWriter out;
    private prManager procManager;
    private otherKerServices kerServices;
    private double totalTurnaroundTime;
    private int completedProcesses;

    //setting up some stuff needed for future use
    private String schedulerType = "dynamic";  //holds the type of scheduel, sat "dynamic" as default because most input files are dynamic
    private final int teamNumber = 8;  //the variable is set to 8 due to our group's number

    //constructor used to abstract the main method
    public simulationController(String inputPath, String outputPath) throws Exception {
        this.inputFile = new File(inputPath);
        this.outputFile = new File(outputPath);
        this.in = new Scanner(inputFile);
        this.out = new PrintWriter(outputFile);
        this.curTime = 0;
        this.totalTurnaroundTime = 0;
        this.completedProcesses = 0;
    }
    
    //runs the processes
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

    //intializes "kernel" & "manager" classes
    public void sysGen(String line) {
     
        //step 1 : splits the line into parts & distribute them into variables
        String[] parts = line.trim().split("\\s+");
        long startTime = Long.parseLong(parts[0]);
        long memory = Long.parseLong(parts[1].split("=")[1]);
        int devices = Integer.parseInt(parts[2].split("=")[1]);
        curTime = startTime;

        //step 2 : intialize the classes
        kerServices = new otherKerServices(memory, devices);
        procManager = new prManager(kerServices);
        procManager.setScheduler(schedulerType, teamNumber);
        procManager.setInternalClock(startTime);
        
        //step 3 : use teritary if to check for the scheduler type used
        String schedulerName = schedulerType.equalsIgnoreCase("dynamic") ? "DynamicRR" : "StaticRR";
        out.printf("CONFIG at %.2f: mem=%d devices=%d scheduler=%s%n%n", 
            (double)startTime, memory, devices, schedulerName);
    }

    //reads all info needed for the process 
    public void parseCmd(String line) {
        
        //step 1 : splits the line into parts & distribute them into variables
        String[] parts = line.trim().split("\\s+");
        long arrivalTime = Long.parseLong(parts[0]);
        long jobId = Long.parseLong(parts[1].split("=")[1]);
        long memory = Long.parseLong(parts[2].split("=")[1]);
        int devices = Integer.parseInt(parts[3].split("=")[1]);
        long runtime = Long.parseLong(parts[4].split("=")[1]);
        int priority = Integer.parseInt(parts[5].split("=")[1]);
        
        //step 2 : process all events until arrival time using min(i,e)
        processEventsUntil(arrivalTime);
        
        //step 3 : start new process procedures
        procManager.procArrivalRoutine(jobId, arrivalTime, runtime, priority, memory, devices);
    }
    
    //process events until target time using min(i,e) logic
    private void processEventsUntil(long targetTime) {
        while (curTime < targetTime) {

            //step 1 : declare & intialize variables 
            long i = procManager.getNextDecisionTime();
            long e = targetTime;
            
            //step 2 : check conditions
            if (i <= e) { //if next internal event process a.t is less than an next external event process 
                          //a.t, enter this critical section. Otherwise, let external event process.  

                if (i == Long.MAX_VALUE) { //if next internal event process a.t is too huge, jump to next external
                    curTime = e;
                    break;
                }
                
                long duration = i - curTime;
                if (duration > 0) { //if duration is more than 0, update curTime with next internal event and check with CPU to get next internal event (if exists)
                    curTime = i;
                    procManager.cpuTimeAdvance(duration);
                }
            } else { 
                curTime = e;
                break;
            }
        }
        
        //step 3 : sync internal clock
        procManager.setInternalClock(curTime);
    }

    //used to display everything in the output.txt file
    public void displayFinalStatistics(String line) {
        long displayTime = Long.parseLong(line.trim());

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
        
        out.println("Jobs in Ready List                                      ");
        out.println("--------------------------------------------------------");
        printReadyQueue(procManager.getReadyQ());
        out.println();
        
        out.println("Jobs in Long Job List                                   ");
        out.println("--------------------------------------------------------");
        out.println("  EMPTY");
        out.println();
        
        out.println("Jobs in Hold List 1                                     ");
        out.println("--------------------------------------------------------");
        printHoldQueue(procManager.getHQ1());
        out.println();
        
        out.println("Jobs in Hold List 2                                     ");
        out.println("--------------------------------------------------------");
        printHoldQueue(procManager.getHQ2());
        out.println();
        out.println();
        
        out.println("Finished Jobs (detailed)                                ");
        out.println("--------------------------------------------------------");
        out.println("  Job    ArrivalTime     CompleteTime     TurnaroundTime    WaitingTime");
        out.println("------------------------------------------------------------------------");
        
        //since user system "simulation controller" cannot acess process, a method in kernel system "prManager" is used
        out.print(procManager.getFinishedProcessesString());
        
        int finishedCount = procManager.getFinishedProcessCount();
        
        if (finishedCount == 0) {
            out.println("  EMPTY");
        }else{
            out.println("Total Finished Jobs:             " + finishedCount);
        }
        out.println();
        out.println();
    }

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
    
    private void close() {
        out.println("\n--- Simulation finished at time " + curTime + ".0 ---\n");
        in.close();
        out.flush();
        out.close();
    }
    
    public static void main(String[] args) {

        //use try & catch" method to handle errors 
        try {

            //step 1 : input the paths here from the folder "i/ofiles"
            String inputPath = "C:\\Users\\PC\\OneDrive\\Desktop\\CODING-GARAGE\\scheduelSimulator\\src\\IOfiles\\inputD0.txt";
            String outputPath = "C:\\Users\\PC\\OneDrive\\Desktop\\CODING-GARAGE\\scheduelSimulator\\src\\IOfiles\\\\output.txt";
            
            //step 2 : declare & initialize the constructor to start the run
            simulationController sim = new simulationController(inputPath, outputPath);
            sim.run();
            
            //step 3 : conformation message
            System.out.println("Simulation complete! Check output.txt for results.");
            
        } catch (Exception e) {
            System.err.println("Error running simulation:");
            e.printStackTrace();
        }
    }
}