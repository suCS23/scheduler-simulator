import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class simulationController {

    private static long currentTime;

    //Declaring I/O files
    private static File inputFile;
    private static File outputFile;

    //Declaring I/O reader & writer
    private static Scanner in;
    private static PrintWriter out;
    public static void main(String[] args) throws Exception {

        //Intializing I/O files
        inputFile = new File ("scheduelSimulator/src/IOfiles/input.txt");   //Type file path here
        outputFile = new File ("scheduelSimulator/src/IOfiles/output.txt"); //Type file path here

        //Intializing I/O reader & writer
        in = new Scanner(inputFile);
        out = new PrintWriter(outputFile);

        //Start reading "inputFile"
        while(in.hasNext()){
            switch (in.next()) {
                case "C" -> {
                }
                case "A" -> {
                }
                case "D" -> {
                }
                default -> {
                    System.out.println("Command does not exist");
                }
            }
        }

        in.close();
        out.flush();
        out.close();  
    }

    public static void sysGen() {
        
    }

    public static void displayFinalStatistics() {

    }

    public static void parseCmd(String line) {
        
    }


}
