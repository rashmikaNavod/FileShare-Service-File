package lk.ijse.eca.fileservice.handler;

import jakarta.servlet.http.HttpServletRequest;
import lk.ijse.eca.fileservice.exception.FileNotFoundException;
import lk.ijse.eca.fileservice.exception.FileOperationException;
import lk.ijse.eca.fileservice.exception.FileStorageException;
import lk.ijse.eca.fileservice.exception.UnauthorizedFileAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
@Order(-1)
public class GlobalExceptionHandler {

    @ExceptionHandler(FileOperationException.class)
    public ResponseEntity<ProblemDetail> handleFileOperation(
            FileOperationException ex, HttpServletRequest request) {
        log.error("File operation failed: {}", ex.getMessage(), ex);
        ProblemDetail problem = buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "File Operation Error",
                ex.getMessage(),
                request.getRequestURI());
        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, problem);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleFileNotFound(
            FileNotFoundException ex, HttpServletRequest request) {
        log.warn("File not found: {}", ex.getMessage());
        ProblemDetail problem = buildProblemDetail(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI());
        return problemResponse(HttpStatus.NOT_FOUND, problem);
    }

    @ExceptionHandler(UnauthorizedFileAccessException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorized(
            UnauthorizedFileAccessException ex, HttpServletRequest request) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        ProblemDetail problem = buildProblemDetail(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                ex.getMessage(),
                request.getRequestURI());
        return problemResponse(HttpStatus.FORBIDDEN, problem);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ProblemDetail> handleFileStorage(
            FileStorageException ex, HttpServletRequest request) {
        log.error("File storage error: {}", ex.getMessage(), ex);
        ProblemDetail problem = buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Storage Error",
                ex.getMessage(),
                request.getRequestURI());
        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, problem);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("File upload size exceeded: {}", ex.getMessage());
        ProblemDetail problem = buildProblemDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Payload Too Large",
                "Uploaded file exceeds the maximum allowed size.",
                request.getRequestURI());
        return problemResponse(HttpStatus.PAYLOAD_TOO_LARGE, problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        ProblemDetail problem = buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI());
        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, problem);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ProblemDetail buildProblemDetail(
            HttpStatus status, String title, String detail, String instance) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(instance));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    private ResponseEntity<ProblemDetail> problemResponse(HttpStatus status, ProblemDetail problem) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

}
