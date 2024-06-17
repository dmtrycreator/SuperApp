package superapp.kr_superapp;

public class ProcessorDetails {
    private String name;
    private String vendor;
    private String family;
    private String model;
    private String stepping;
    private String processorID;
    private String identifier;
    private String microarchitecture;
    private int logicalProcessorCount;
    private int physicalProcessorCount;
    private long maxFreq;
    private long currentFreq;

    // Конструктор
    public ProcessorDetails(String name, String vendor, String family, String model, String stepping, String processorID,
                            String identifier, String microarchitecture, int logicalProcessorCount, int physicalProcessorCount,
                            long maxFreq, long currentFreq) {
        this.name = name;
        this.vendor = vendor;
        this.family = family;
        this.model = model;
        this.stepping = stepping;
        this.processorID = processorID;
        this.identifier = identifier;
        this.microarchitecture = microarchitecture;
        this.logicalProcessorCount = logicalProcessorCount;
        this.physicalProcessorCount = physicalProcessorCount;
        this.maxFreq = maxFreq;
        this.currentFreq = currentFreq;
    }

    // Геттеры
    public String getName() { return name; }
    public String getVendor() { return vendor; }
    public String getFamily() { return family; }
    public String getModel() { return model; }
    public String getStepping() { return stepping; }
    public String getProcessorID() { return processorID; }
    public String getIdentifier() { return identifier; }
    public String getMicroarchitecture() { return microarchitecture; }
    public int getLogicalProcessorCount() { return logicalProcessorCount; }
    public int getPhysicalProcessorCount() { return physicalProcessorCount; }
    public long getMaxFreq() { return maxFreq; }
    public long getCurrentFreq() { return currentFreq; }
}
