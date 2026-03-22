package lk.ijse.eca.fileservice.exception;

public class FileOperationException extends RuntimeException {

    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
