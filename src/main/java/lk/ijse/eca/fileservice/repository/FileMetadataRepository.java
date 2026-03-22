package lk.ijse.eca.fileservice.repository;

import lk.ijse.eca.fileservice.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByShareId(String shareId);
    List<FileMetadata> findAllByOwnerUsername(String ownerUsername);
}

