package lk.ijse.eca.fileservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
public class FileDetailsDTO {
    private String shareId;
    private String title;
    private String description;
    private String fileType;
    private String owner;
    private String previewUrl;
    private Long fileSize;
}
