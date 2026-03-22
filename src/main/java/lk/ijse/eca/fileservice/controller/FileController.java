package lk.ijse.eca.fileservice.controller;

import jakarta.validation.Valid;
import lk.ijse.eca.fileservice.dto.FileDetailsDTO;
import lk.ijse.eca.fileservice.dto.FileDownloadDTO;
import lk.ijse.eca.fileservice.dto.FileResponseDTO;
import lk.ijse.eca.fileservice.dto.FileUploadRequestDTO;
import lk.ijse.eca.fileservice.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Validated
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponseDTO> uploadFile(
            @Valid @ModelAttribute FileUploadRequestDTO dto,
            @RequestHeader("X-Logged-In-User") String username) {

        log.info("POST /api/v1/files/upload - User: {}, Title: {}", username, dto.getTitle());
        FileResponseDTO response = fileService.uploadFile(username, dto);
        return ResponseEntity.ok(response);

    }



    @DeleteMapping("/{shareId}")
    public ResponseEntity<FileResponseDTO> deleteFile(
            @PathVariable String shareId,
            @RequestHeader("X-Logged-In-User") String username) {

        log.info("DELETE /api/v1/files/{} - Requested by User: {}", shareId, username);
        FileResponseDTO response = fileService.deleteFile(shareId, username);
        return ResponseEntity.ok(response);
    }


    @PutMapping(value = "/{shareId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponseDTO> updateFile(
            @PathVariable String shareId,
            @Valid @ModelAttribute FileUploadRequestDTO dto,
            @RequestHeader("X-Logged-In-User") String username) {
        log.info("PUT /api/v1/files/{} - User: {}", shareId, username);
        FileResponseDTO response = fileService.updateFile(shareId, dto, username);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/details/{shareId}")
    public ResponseEntity<FileDetailsDTO> getFileDetails(@PathVariable String shareId){
        log.info("GET /api/v1/files/details/{} - Fetching metadata", shareId);
        FileDetailsDTO details = fileService.getFileDetails(shareId);
        return ResponseEntity.ok(details);
    }


    @GetMapping("/preview/{shareId}")
    public ResponseEntity<Resource> viewFile(
            @PathVariable String shareId,
            @RequestParam(defaultValue = "false") boolean download){
        log.info("GET /api/v1/files/preview/{} - Streaming file (download={})", shareId, download);

        FileDownloadDTO downloadData = fileService.getFileForPreview(shareId);
        String filename = downloadData.getFileName();
        String contentType = downloadData.getContentType();

        if(!filename.contains(".")){
            if(contentType.equals("image/png")) filename += ".png";
            else if (contentType.equals("image/jpeg")) filename += ".jpg";
            else if (contentType.equals("application/pdf")) filename += ".pdf";
            else if (contentType.equals("video/mp4")) filename += ".mp4";
        }

        String disposition = download ? "attachment" : "inline";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadData.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + filename + "\"")
                .body(downloadData.getResource());

    }


    @GetMapping
    public ResponseEntity<List<FileDetailsDTO>> getAllFiles(
            @RequestHeader ("X-Logged-In-User") String username){
        log.info("GET /api/v1/files - Fetching all files for user: {}", username);
        List<FileDetailsDTO> fileList = fileService.getAllFiles(username);
        return ResponseEntity.ok(fileList);
    }

}
