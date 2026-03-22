package lk.ijse.eca.fileservice.exception;

public class UnauthorizedFileAccessException extends RuntimeException {
    public UnauthorizedFileAccessException(String message) {
        super(message);
    }
}
