package superapp.kr_superapp;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Semaphore;

/**
 * Класс FileMappingHandler отвечает за управление общим доступом к памяти для межпроцессного взаимодействия.
 * Он предоставляет методы для записи и чтения данных из общей памяти.

 * The FileMappingHandler class is responsible for managing shared memory for inter-process communication.
 * It provides methods for writing and reading data from shared memory.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class FileMappingHandler {
    private static final String FILE_PATH = "shared_memory.bin";
    private static final int FILE_SIZE = 1024 * 1024; // 1 MB
    private final MappedByteBuffer mappedByteBuffer;
    private final Semaphore semaphore;

    public FileMappingHandler() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(FILE_PATH, "rw")) {
            file.setLength(FILE_SIZE);
            try (FileChannel fileChannel = file.getChannel()) {
                this.mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);
            }
        }
        this.semaphore = new Semaphore(1);
    }

    public void writeData(byte[] data) throws InterruptedException {
        semaphore.acquire();
        try {
            mappedByteBuffer.position(0);
            mappedByteBuffer.put(data);
        } finally {
            semaphore.release();
        }
    }

    public byte[] readData() throws InterruptedException {
        byte[] data = new byte[FILE_SIZE];
        semaphore.acquire();
        try {
            mappedByteBuffer.position(0);
            mappedByteBuffer.get(data);
        } finally {
            semaphore.release();
        }
        return data;
    }
}
