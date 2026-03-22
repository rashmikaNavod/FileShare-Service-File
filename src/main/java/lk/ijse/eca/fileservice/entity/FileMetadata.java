package lk.ijse.eca.fileservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String fileType;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false, unique = true)
    private String shareId;

    @Column(nullable = false)
    private String ownerUsername;

    @Column(nullable = false)
    private Long fileSize;
}
