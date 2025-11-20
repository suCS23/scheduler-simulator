
public class otherKerServices {

    // ==============================
    //           attributes
    // ==============================

    private long memorySize;
    private final long totalMemorySize;
    private int noDevs;
    private final int totalNoDevs;

    // ==============================
    //         Constructor
    // ==============================
    public otherKerServices(long memorySize, int noDevs) {
        this.memorySize = memorySize;
        this.totalMemorySize = memorySize;
        this.noDevs = noDevs;
        this.totalNoDevs = noDevs;       
    }

    // ==============================
    //     Allocators & reservers
    // ==============================
    public void allocateMemory(long mr) {this.memorySize -= mr;}
    public void deallocateMemory(long mr) {this.memorySize += mr;}

    public void reserveDevices(int dr) {this.noDevs -= dr;}
    public void releaseDevices(int dr) {this.noDevs += dr;}    
    
    // ==============================
    //           Getters
    // ==============================
    //used for final statistics & display

    public long getMemorySize() {return memorySize;}
    public long getTotalMemorySize() {return totalMemorySize;}

    public int getNoDevs() {return noDevs;}
    public int getTotalNoDevs() {return totalNoDevs;}

    // ==============================
    //           Checker
    // ==============================
    public boolean canAllocate(long mr, int dr) {
        return mr <= memorySize && dr <= noDevs;
    }
}