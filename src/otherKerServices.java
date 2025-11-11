
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
}
