package superapp.kr_superapp;

/**
 * Класс ProcessInfo представляет информацию о процессе.
 *
 * The ProcessInfo class represents information about a process.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class ProcessInfo {
    private int pid;
    private String name;
    private double cpuUsage;
    private double memoryUsage;
    private String state;
    private int priority;
    private long startTime;

    public ProcessInfo(int pid, String name, double cpuUsage, double memoryUsage, String state, int priority, long startTime) {
        this.pid = pid;
        this.name = name;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.state = state;
        this.priority = priority;
        this.startTime = startTime;
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
}
