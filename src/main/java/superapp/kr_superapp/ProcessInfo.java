package superapp.kr_superapp;

public class ProcessInfo {
    private int pid;
    private String name;
    private double cpuUsage;
    private double memoryUsage;
    private String state;
    private int priority;
    private long startTime;
    private String executablePath;
    private String userName;

    public ProcessInfo(int pid, String name, double cpuUsage, double memoryUsage, String state, int priority, long startTime, String executablePath, String userName) {
        this.pid = pid;
        this.name = name;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.state = state;
        this.priority = priority;
        this.startTime = startTime;
        this.executablePath = executablePath;
        this.userName = userName;
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public String getState() {
        return state;
    }

    public int getPriority() {
        return priority;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public String getUserName() {
        return userName;
    }
}
