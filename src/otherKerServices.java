
class otherKerServices {

    private long memorySize;
    private long totalMemorySize;
    private int noDevs;
    private int totalNoDevs;

    public otherKerServices(long memeorySize, int noDevs) {
        this.memorySize = memeorySize;
        this.noDevs = noDevs;
    }

    public void allocateMemory(process p) {
        this.memorySize -= p.getMr();
    }

    public void deallocateMemory(process p) {
        this.memorySize += p.getMr();
    }

    public void reserveDevices(process p) {
        this.noDevs -= p.getDr();
    }

    public void releaseDevices(process p) {
        this.noDevs += p.getDr();
    }

    // Check if job exceeds TOTAL system resources (for rejection)
    public boolean exceedsTotal(process p) {
        return p.getMr() > totalMemorySize || p.getDr() > totalNoDevs;
    }
    
    // Check if enough AVAILABLE resources (for allocation)
    public boolean canAllocate(process p) {
        return p.getMr() <= memorySize && p.getDr() <= noDevs;
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
