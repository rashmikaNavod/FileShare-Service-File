package lk.ijse.eca.fileservice.service.impl;

import lk.ijse.eca.fileservice.dto.FileDetailsDTO;
import lk.ijse.eca.fileservice.dto.FileDownloadDTO;
import lk.ijse.eca.fileservice.dto.FileResponseDTO;
import lk.ijse.eca.fileservice.dto.FileUploadRequestDTO;
import lk.ijse.eca.fileservice.entity.FileMetadata;
import lk.ijse.eca.fileservice.exception.FileNotFoundException;
import lk.ijse.eca.fileservice.exception.FileOperationException;
import lk.ijse.eca.fileservice.exception.FileStorageException;
import lk.ijse.eca.fileservice.exception.UnauthorizedFileAccessException;
import lk.ijse.eca.fileservice.repository.FileMetadataRepository;
import lk.ijse.eca.fileservice.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${app.storage.path}")
    private String storagePathStr;

    private final FileMetadataRepository fileMetadataRepository;
    private Path storagePath;

    @Override
    @Transactional
    public FileResponseDTO uploadFile(String username, FileUploadRequestDTO dto) {

        log.debug("Starting file upload for user: {}", username);

        String fileId = UUID.randomUUID().toString();
        String shareId = UUID.randomUUID().toString().substring(0, 8);

        FileMetadata metadata = FileMetadata.builder()
                .id(fileId)
                .title(dto.getTitle())
                .fileType(dto.getFile().getContentType())
                .filePath(getStoragePath().resolve(fileId).toString())
                .ownerUsername(username)
                .shareId(shareId)
                .description(dto.getDescription())
                .fileSize(dto.getFile().getSize())
                .build();

        fileMetadataRepository.save(metadata);
        log.debug("Metadata saved to PostgreSQL: {}", fileId);

        saveFileToDisk(fileId, shareId, dto.getFile());
        log.info("File upload successful for user: {}. Share ID: {}", username, shareId);

        return FileResponseDTO.builder()
                .message("File uploaded successfully")
                .shareId(shareId)
                .title(dto.getTitle())
                .build();

    }

    @Override
    public FileResponseDTO updateFile(String shareId, FileUploadRequestDTO dto, String username) {

        log.debug("Updating file for shareId: {} by user: {}", shareId, username);

        FileMetadata metadata = fileMetadataRepository.findByShareId(shareId)
                .orElseThrow(() -> new FileNotFoundException(shareId));

        if (!metadata.getOwnerUsername().equals(username)) {
            throw new UnauthorizedFileAccessException("You don't have permission to update this file!");
        }

        // Delete the old file from disk before saving the new one
        try{
            Path oldFilePath = Paths.get(metadata.getFilePath());
            Files.deleteIfExists(oldFilePath);
            log.debug("Old file deleted from disk: {}", oldFilePath);
        }catch (IOException e){
            log.error("Failed to delete old file during update: {}", metadata.getFilePath(), e);
            throw new FileStorageException("Could not replace the existing file on disk", e);
        }

        saveFileToDisk(metadata.getId(), shareId, dto.getFile());

        metadata.setTitle(dto.getTitle());
        metadata.setDescription(dto.getDescription());
        metadata.setFileType(dto.getFile().getContentType());

        fileMetadataRepository.save(metadata);
        log.info("File updated successfully for shareId: {} by user: {}", shareId, username);

        return FileResponseDTO.builder()
                .message("File updated successfully")
                .shareId(shareId)
                .title(metadata.getTitle())
                .build();
    }

    @Override
    @Transactional
    public FileResponseDTO deleteFile(String shareId, String username) {

        log.debug("Attempting to delete file with shareId: {} by user: {}", shareId, username);

        FileMetadata metadata = fileMetadataRepository.findByShareId(shareId)
                .orElseThrow(() -> new FileNotFoundException(shareId));

        if (!metadata.getOwnerUsername().equals(username)){
           throw new UnauthorizedFileAccessException("You are not authorized to delete this file!");
        }

        Path filePath = Paths.get(metadata.getFilePath());
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.debug("Physical file deleted from disk: {}", filePath);
            } else {
                log.warn("Physical file not found on disk, but proceeding to delete metadata.");
            }
        } catch (IOException e) {
            log.error("Failed to delete physical file: {}", filePath, e);
            throw new FileStorageException("Could not delete physical file from disk", e);
        }

        fileMetadataRepository.delete(metadata);
        log.info("File metadata deleted successfully for shareId: {}", shareId);

        return FileResponseDTO.builder()
                .message("File deleted successfully")
                .shareId(shareId)
                .title(metadata.getTitle())
                .build();

    }

    @Override
    public FileDownloadDTO getFileForPreview(String shareId) {

        FileMetadata metadata = fileMetadataRepository.findByShareId(shareId)
                .orElseThrow(() -> new FileNotFoundException(shareId));

        try {
            Path filePath = Paths.get(metadata.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return FileDownloadDTO.builder()
                        .resource(resource)
                        .fileName(metadata.getTitle())
                        .contentType(metadata.getFileType())
                        .build();
            } else {
                throw new FileNotFoundException(shareId);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Error while reading the file from disk", e);
        }

    }

    @Override
    public FileDetailsDTO getFileDetails(String shareId) {

        FileMetadata metadata = fileMetadataRepository.findByShareId(shareId)
                .orElseThrow(() -> new FileNotFoundException(shareId));

        return FileDetailsDTO.builder()
                .title(metadata.getTitle())
                .description(metadata.getDescription())
                .fileType(metadata.getFileType())
                .owner(metadata.getOwnerUsername())
                .previewUrl("/api/v1/files/preview/" + shareId)
                .fileSize(metadata.getFileSize())
                .shareId(metadata.getShareId())
                .build();
    }

    @Override
    public List<FileDetailsDTO> getAllFiles(String username) {

        log.info("Fetching all files for user: {}", username);

        List<FileMetadata> metaDataList = fileMetadataRepository.findAllByOwnerUsername(username);

        return metaDataList.stream()
                .map(metadata -> FileDetailsDTO.builder()
                        .title(metadata.getTitle())
                        .shareId(metadata.getShareId())
                        .description(metadata.getDescription())
                        .fileType(metadata.getFileType())
                        .owner(metadata.getOwnerUsername())
                        .previewUrl("/api/v1/files/preview/" + metadata.getShareId())
                        .fileSize(metadata.getFileSize())
                        .build())
                .toList();
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Path getStoragePath() {
        if (storagePath == null) {
            storagePath = Paths.get(storagePathStr);
        }
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new FileOperationException("Failed to create storage directory: " + storagePathStr, e);
        }
        return storagePath;
    }

    private void saveFileToDisk(String fileId, String shareId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileNotFoundException(shareId);
        }

        Path targetPath = getStoragePath().resolve(fileId);
        try {
            Files.write(targetPath, file.getBytes());
            log.debug("File successfully written to: {}", targetPath);
        } catch (IOException e) {
            log.error("IO Exception while saving file: {}", fileId, e);
            throw new FileOperationException("Failed to save picture file: ", e);
        }
    }

}
