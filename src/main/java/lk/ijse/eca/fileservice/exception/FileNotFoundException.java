package lk.ijse.eca.fileservice.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String shareId) {
        super("File not found with Share ID: " + shareId);
    }
}
