package superapp.kr_superapp;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

    private static final Instant appStartTime = Instant.now();
    private static final SystemInfo systemInfo = new SystemInfo();
    private static final CentralProcessor processor = systemInfo.getHardware().getProcessor();
    private static final OperatingSystem os = systemInfo.getOperatingSystem();

    /**
     * Получает список процессов в Linux.
     *
     * Gets the list of processes in Linux.
     *
     * @return список процессов / list of processes
     */
    public static List<ProcessInfo> getLinuxProcesses() {
        List<ProcessInfo> processList = new ArrayList<>();
        for (OSProcess process : os.getProcesses()) {
            int pid = process.getProcessID();
            String name = process.getName();
            double cpuUsage = 100d * (process.getKernelTime() + process.getUserTime()) / process.getUpTime();
            double memoryUsage = process.getResidentSetSize() / (1024 * 1024); // Convert to MB
            String state = process.getState().name();
            int priority = process.getPriority();
            long startTime = process.getStartTime();
            String executablePath = process.getPath();
            String userName = process.getUser();
            String commandLine = String.join(" ", process.getArguments());
            processList.add(new ProcessInfo(pid, name, cpuUsage, memoryUsage, state, priority, startTime, executablePath, userName, commandLine));
        }
        return processList;
    }

    /**
     * Парсит строку времени в миллисекунды.
     *
     * Parses an elapsed time string into milliseconds.
     *
     * @param elapsedTimeStr строка времени / the elapsed time string
     * @return миллисекунды / milliseconds
     */
    public static long parseElapsedTime(String elapsedTimeStr) {
        String[] timeParts = elapsedTimeStr.split("[-:]");
        long elapsedMillis = 0;
        if (timeParts.length == 3) { // HH:MM:SS
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            int seconds = Integer.parseInt(timeParts[2]);
            elapsedMillis = hours * 3600000L + minutes * 60000L + seconds * 1000L;
        } else if (timeParts.length == 2) { // MM:SS
            int minutes = Integer.parseInt(timeParts[0]);
            int seconds = Integer.parseInt(timeParts[1]);
            elapsedMillis = minutes * 60000L + seconds * 1000L;
        } else if (timeParts.length == 4) { // DD-HH:MM:SS
            int days = Integer.parseInt(timeParts[0]);
            int hours = Integer.parseInt(timeParts[1]);
            int minutes = Integer.parseInt(timeParts[2]);
            int seconds = Integer.parseInt(timeParts[3]);
            elapsedMillis = days * 86400000L + hours * 3600000L + minutes * 60000L + seconds * 1000L;
        }
        return System.currentTimeMillis() - elapsedMillis;
    }

    /**
     * Получает время работы операционной системы.
     *
     * Gets the operating system uptime.
     *
     * @return время работы ОС / OS uptime
     */
    public static Duration getOsUptime() {
        try {
            long uptimeInSeconds = systemInfo.getOperatingSystem().getSystemUptime();
            return Duration.ofSeconds(uptimeInSeconds);
        } catch (Exception e) {
            e.printStackTrace();
            return Duration.ZERO;
        }
    }

    /**
     * Получает время работы приложения.
     *
     * Gets the application uptime.
     *
     * @return время работы приложения / application uptime
     */
    public static Duration getAppUptime() {
        return Duration.between(appStartTime, Instant.now());
    }

    /**
     * Форматирует продолжительность в строку формата HH:MM:SS.
     *
     * Formats the duration into a string in the format HH:MM:SS.
     *
     * @param duration продолжительность / duration
     * @return отформатированная строка / formatted string
     */
    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Получает название процессора.
     *
     * Gets the processor name.
     *
     * @return название процессора / processor name
     */
    public static String getProcessorName() {
        return processor.getProcessorIdentifier().getName();
    }

    /**
     * Получает количество модулей процесса.
     *
     * Gets the module count of a process.
     *
     * @param processName имя процесса / process name
     * @return количество модулей процесса / process module count
     */
    public static int getModuleCount(String processName) {
        int moduleCount = 0;
        try {
            String command = "lsof -p $(pgrep -x " + processName + ") | wc -l";
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                moduleCount = Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return moduleCount;
    }

    /**
     * Отправляет сигнал процессу по PID.
     *
     * Sends a signal to a process by PID.
     *
     * @param pid    идентификатор процесса / process ID
     * @param signal сигнал для отправки / signal to send
     * @throws IOException если возникла ошибка при отправке сигнала / if an error occurs while sending the signal
     */
    public static void sendSignalToProcess(int pid, String signal) throws IOException {
        String command = String.format("kill -%s %d", signal, pid);
        Runtime.getRuntime().exec(command);
    }

    /**
     * Получает детали процессора.
     *
     * Gets the processor details.
     *
     * @return детали процессора / processor details
     */
    public static ProcessorDetails getProcessorDetails() {
        CentralProcessor.ProcessorIdentifier identifier = processor.getProcessorIdentifier();
        return new ProcessorDetails(
                identifier.getName(),
                identifier.getVendor(),
                identifier.getFamily(),
                identifier.getModel(),
                identifier.getStepping(),
                identifier.getProcessorID(),
                identifier.getIdentifier(),
                identifier.getMicroarchitecture(),
                processor.getLogicalProcessorCount(),
                processor.getPhysicalProcessorCount(),
                processor.getMaxFreq(),
                processor.getCurrentFreq()[0]
        );
    }
}
