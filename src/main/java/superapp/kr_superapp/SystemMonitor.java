package superapp.kr_superapp;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import java.util.List;
import java.util.stream.Collectors;

public class SystemMonitor {
    private SystemInfo systemInfo;
    private CentralProcessor processor;
    private GlobalMemory memory;
    private List<NetworkIF> networkIFs;
    private long[] prevTicks;

    public SystemMonitor() {
        systemInfo = new SystemInfo();
        processor = systemInfo.getHardware().getProcessor();
        memory = systemInfo.getHardware().getMemory();
        networkIFs = systemInfo.getHardware().getNetworkIFs();
        prevTicks = processor.getSystemCpuLoadTicks();
    }

    public double getCpuLoad() {
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = ticks;
        return cpuLoad;
    }

    public long getUsedMemory() {
        return memory.getTotal() - memory.getAvailable();
    }

    public long getTotalMemory() {
        return memory.getTotal();
    }

    public long getTotalBytesReceived() {
        return networkIFs.stream().mapToLong(NetworkIF::getBytesRecv).sum();
    }

    public long getTotalBytesSent() {
        return networkIFs.stream().mapToLong(NetworkIF::getBytesSent).sum();
    }

    public void updateNetworkStats() {
        networkIFs.forEach(NetworkIF::updateAttributes);
    }
}
