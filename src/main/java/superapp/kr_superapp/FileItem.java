package superapp.kr_superapp;

/**
 * Класс FileItem представляет элемент файла или папки с его основными атрибутами.
 *
 * The FileItem class represents a file or folder item with its basic attributes.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class FileItem {
    private String name;
    private long size;
    private String type;
    private String absolutePath;

    /**
     * Конструктор для создания объекта FileItem с заданными параметрами.
     *
     * Constructor to create a FileItem object with the specified parameters.
     *
     * @param name Имя файла или папки / The name of the file or folder
     * @param size Размер файла в байтах / The size of the file in bytes
     * @param type Тип файла (например, "Файл" или "Папка") / The type of the file (e.g., "File" or "Folder")
     * @param absolutePath Абсолютный путь к файлу или папке / The absolute path to the file or folder
     */
    public FileItem(String name, long size, String type, String absolutePath) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.absolutePath = absolutePath;
    }

    /**
     * Возвращает имя файла или папки.
     *
     * Returns the name of the file or folder.
     *
     * @return Имя файла или папки / The name of the file or folder
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает размер файла в байтах.
     *
     * Returns the size of the file in bytes.
     *
     * @return Размер файла в байтах / The size of the file in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Возвращает тип файла.
     *
     * Returns the type of the file.
     *
     * @return Тип файла / The type of the file
     */
    public String getType() {
        return type;
    }

    /**
     * Возвращает абсолютный путь к файлу или папке.
     *
     * Returns the absolute path to the file or folder.
     *
     * @return Абсолютный путь к файлу или папке / The absolute path to the file or folder
     */
    public String getAbsolutePath() {
        return absolutePath;
    }
}
