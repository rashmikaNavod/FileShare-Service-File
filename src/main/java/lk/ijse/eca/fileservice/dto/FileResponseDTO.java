package lk.ijse.eca.fileservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FileResponseDTO {
    private String message;
    private String shareId;
    private String title;
}
