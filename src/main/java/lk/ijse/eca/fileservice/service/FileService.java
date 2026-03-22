package lk.ijse.eca.fileservice.service;

import lk.ijse.eca.fileservice.dto.FileDetailsDTO;
import lk.ijse.eca.fileservice.dto.FileDownloadDTO;
import lk.ijse.eca.fileservice.dto.FileResponseDTO;
import lk.ijse.eca.fileservice.dto.FileUploadRequestDTO;
import org.springframework.core.io.Resource;

import java.util.List;

public interface FileService {
    FileResponseDTO uploadFile(String username, FileUploadRequestDTO dto);
    FileResponseDTO updateFile(String shareId, FileUploadRequestDTO dto, String username);
    FileResponseDTO deleteFile(String shareId, String username);
    FileDownloadDTO getFileForPreview(String shareId);
    FileDetailsDTO getFileDetails(String shareId);
    List<FileDetailsDTO> getAllFiles(String username);
}
