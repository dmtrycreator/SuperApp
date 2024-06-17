package superapp.kr_superapp;

import java.io.IOException;

/**
 * Класс LinuxAppLauncher отвечает за запуск различных приложений в среде GNOME на Linux.
 *
 * The LinuxAppLauncher class is responsible for launching various applications in the GNOME environment on Linux.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class LinuxAppLauncher {

    /**
     * Открывает системный монитор.
     *
     * Opens the system monitor.
     */
    public void openSystemMonitor() {
        log("Opening System Monitor / Открытие системного монитора");
        launch("gnome-system-monitor");
    }

    /**
     * Открывает терминал.
     *
     * Opens the terminal.
     */
    public void openTerminal() {
        log("Opening Terminal / Открытие терминала");
        launch("gnome-terminal");
    }

    /**
     * Открывает настройки.
     *
     * Opens the settings.
     */
    public void openSettings() {
        log("Opening Settings / Открытие настроек");
        launch("gnome-control-center");
    }

    /**
     * Открывает сетевые подключения.
     *
     * Opens the network connections.
     */
    public void openNetworkConnections() {
        log("Opening Network Connections / Открытие сетевых подключений");
        launch("nm-connection-editor");
    }

    /**
     * Открывает драйверы.
     *
     * Opens the drivers.
     */
    public void openDrivers() {
        log("Opening Drivers / Открытие драйверов");
        launch("software-properties-gtk");
    }

    /**
     * Запускает указанную команду.
     *
     * Launches the specified command.
     *
     * @param command команда для запуска / command to launch
     */
    private void launch(String command) {
        try {
            new ProcessBuilder(command).start();
            log("Command launched: " + command + " / Команда запущена: " + command);
        } catch (IOException e) {
            e.printStackTrace();
            log("Error launching command: " + command + " / Ошибка запуска команды: " + command);
        }
    }

    /**
     * Логирует сообщение с отметкой времени.
     *
     * Logs a message with a timestamp.
     *
     * @param message сообщение для логирования / message to log
     */
    private static void log(String message) {
        Controller.log(message);
    }
}
