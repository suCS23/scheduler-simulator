// simulationController.java
import java.io.*;
import java.util.*;

public class simulationController {

    // ==============================
    //           Attributes
    // ==============================

    private static long curTime;
    private static Scanner in;
    private static PrintWriter out;
    private static prManager procManager;
    private static otherKerServices kerServices;

    private static String schedulerType = "dynamic"; //sat "dynamic" as default becuase it is mostly used
    private static final int TEAM_NUMBER = 8; //8 for our gorup's number "G8", use 6 to see if the output is accurate to the expected output

    // ==============================
    //             Main
    // ==============================
     public static void main(String[] args) {
        
        //part 1 : intialize io variables in a try/catch method
        //do not forget to change the pathing
        try {
            in = new Scanner(new File("C:\\Users\\PC\\OneDrive\\Desktop\\CODING-GARAGE\\scheduelSimulator\\src\\IOfiles\\inputS.txt"));
            out = new PrintWriter(new File("C:\\Users\\PC\\OneDrive\\Desktop\\CODING-GARAGE\\scheduelSimulator\\src\\IOfiles\\output.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("There is an error, maybe its in your files pathing :(");
        }

        //part 2 : run the events
        while (in.hasNext()) {

            String command = in.next();
            String line = in.hasNext() ? in.nextLine().trim() : "";

            switch (command) {
                case "C" -> sysGen(line);
                case "A" -> parseCmd(line);
                case "D" -> displayFinalStatistics(line);
            }
        }

        //part 3 : finalize
        out.printf("%n--- Simulation finished at time %.1f --- %n%n", (double)curTime);
        out.flush();
        out.close();
        in.close();

        System.out.println("""
                           \nDone, thanks for running "suOS" version 5.12 
                           Checkout your "output.txt" file for results
                           Stay tuned for future updates on our system :3

                           Made by : G8 (Saud Alfhaid, Faris Alzahrani)
                           Made for : Dr.As3d (CPCS361 - CS2)
                           """);
    }

    // ==============================
    //   C  — System Configuration
    // ==============================
    public static void sysGen(String line) {

        //part 1 : remove all the letters and symbols, and place all the numbers in an array 
        String[] numbers = line.replaceAll("[=A-Za-z]", "").split(" ");
        
        //part 2 : Check for scheduel type 
        //the hole here is, in all the input files it only gives out scheduleType if it is static.
        //therefor, if the length of the array is 4, the scheduleType is static because all the 
        //dynamic input files do not give out the scheduelType
        String schedulerName = "DynamicRR";
        if(numbers.length == 4){
            schedulerType = "static"; 
            schedulerName = "StaticRR";
        }
        
        //part 3 : reset current time and intialize kernel and process manager's instances
        curTime = 0;
        kerServices = new otherKerServices( Long.parseLong(numbers[1]), Integer.parseInt(numbers[2]));
        procManager = new prManager(kerServices, schedulerType, TEAM_NUMBER);

        //part 4 : print out configuration status
        out.printf("CONFIG at %s.00 ---> mem = %s || devices = %s || scheduler = %s", 
                           numbers[0], numbers[1], numbers[2], schedulerName);
    }

    // ==============================
    //       A  — Job Arrival
    // ==============================
    public static void parseCmd(String line) {

        //part 1 : remove all the letters and symbols, and place all the numbers in an array
        String[] numbers = line.replaceAll("[=A-Za-z]", "").split(" ");

        //part 2 : check for internal events, after that intialize a process by calling prManager instance 
        eventHandling(Long.parseLong(numbers[0]));
        procManager.procArrivalRoutine(Long.parseLong(numbers[1]), Long.parseLong(numbers[0]), 
                                       Long.parseLong(numbers[4]), Integer.parseInt(numbers[5]), 
                                       Long.parseLong(numbers[2]), Integer.parseInt(numbers[3]));
    }

    // ==============================
    //        Handling events
    // ==============================
    // check which process comes first (AT wise), and
    // sets the time for both current and internal clock
    private static void eventHandling(long targetTime) {

        //part 1 : 
        while (curTime < targetTime) {

            //part a : returns arriving time of next internal event
            long nextInternal = procManager.getNextDecisionTime();

            //part b : if Internal is less than arrival time of external event, loop
            //else, do it one last time
            if (nextInternal <= targetTime) {
                long duration = nextInternal - curTime;
                procManager.cpuTimeAdvance(duration);
                curTime = nextInternal;
            } else {
                long duration = targetTime - curTime;
                procManager.cpuTimeAdvance(duration);
                curTime = targetTime;
            }
        }

        //part 2 : 
        procManager.setInternalClock(curTime);
    }

    // ==============================
    //   D  — Display
    // ==============================
    public static void displayFinalStatistics(String line) {
        long displayTime = Long.parseLong(line);
        eventHandling(displayTime);

        out.println();
        out.println("-------------------------------------------------------");
        out.println("System Status:                                         ");
        out.println("-------------------------------------------------------");
        out.printf("          Time: %.2f%n", (double) displayTime);
        out.printf("  Total Memory: %d%n", kerServices.getTotalMemorySize());
        out.printf(" Avail. Memory: %d%n", kerServices.getMemorySize());
        out.printf(" Total Devices: %d%n", kerServices.getTotalNoDevs());
        out.printf("Avail. Devices: %d%n", kerServices.getNoDevs());
        out.println();

        out.println("Jobs in Ready List                                      ");
        out.println("--------------------------------------------------------");
        out.print(procManager.getReadyQueueContentString());
        out.println();

        out.println("Jobs in Long Job List                                   ");
        out.println("--------------------------------------------------------");
        out.println("  EMPTY\n");

        out.println("Jobs in Hold List 1                                     ");
        out.println("--------------------------------------------------------");
        out.print(procManager.getHQ1ContentString());
        out.println();

        out.println("Jobs in Hold List 2                                     ");
        out.println("--------------------------------------------------------");
        out.print(procManager.getHQ2ContentString());
        out.println();

        out.println();
        out.println("Finished Jobs (detailed)                                ");
        out.println("--------------------------------------------------------");
        out.println("  Job    ArrivalTime     CompleteTime     TurnaroundTime    WaitingTime");
        out.println("------------------------------------------------------------------------");
        out.print(procManager.getFinishedProcessesString());

        int finished = procManager.getFinishedProcessCount();
        if (finished == 0)
            out.println("  EMPTY");
        else
            out.println("Total Finished Jobs:             " + finished);

        out.println();
    }
}
