package lk.ijse.eca.fileservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
@Builder
public class FileDownloadDTO {
    private Resource resource;
    private String fileName;
    private String contentType;
}
