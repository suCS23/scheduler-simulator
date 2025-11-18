// otherKerServices.java

class otherKerServices {

    private long memorySize;
    private long totalMemorySize;
    private int noDevs;
    private int totalNoDevs;

    public otherKerServices(long memorySize, int noDevs) {
        this.memorySize = memorySize;
        this.totalMemorySize = memorySize; // Set total
        this.noDevs = noDevs;
        this.totalNoDevs = noDevs;       // Set total
    }

    // Allocate memory by taking the amount required (mr) directly
    public void allocateMemory(long mr) {
        this.memorySize -= mr;
    }

    // Deallocate memory by taking the amount required (mr) directly
    public void deallocateMemory(long mr) {
        this.memorySize += mr;
    }

    // Reserve devices by taking the count required (dr) directly
    public void reserveDevices(int dr) {
        this.noDevs -= dr;
    }

    // Release devices by taking the count required (dr) directly
    public void releaseDevices(int dr) {
        this.noDevs += dr;
    }

    // Check if job exceeds TOTAL system resources (for rejection)
    // Takes memory (mr) and devices (dr) directly
    public boolean exceedsTotal(long mr, int dr) {
        return mr > totalMemorySize || dr > totalNoDevs;
    }
    
    // Check if enough AVAILABLE resources (for allocation)
    // Takes memory (mr) and devices (dr) directly
    public boolean canAllocate(long mr, int dr) {
        return mr <= memorySize && dr <= noDevs;
    }
    
    // Getters for statistics/display
    public long getMemorySize() {
        return memorySize;
    }
    
    public long getTotalMemorySize() {
        return totalMemorySize;
    }
    
    public int getNoDevs() {
        return noDevs;
    }
    
    public int getTotalNoDevs() {
        return totalNoDevs;
    }
}