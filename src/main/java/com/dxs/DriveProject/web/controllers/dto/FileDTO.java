package com.dxs.DriveProject.web.controllers.dto;

import com.dxs.DriveProject.domain.File;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FileDTO {
    private String id;
    private String filename;
    private Boolean bookmark;
    private Long size;
    private String type;
    private Date createdAt;

    public FileDTO(String id, String filename, Boolean bookmark, Long size, String type, Date createdAt) {
        this.id = id;
        this.filename = filename;
        this.bookmark = bookmark;
        this.size = size;
        this.type = type;
        this.createdAt = createdAt;
    }

    public static FileDTO fromEntity(File file) {
        return new FileDTO(
                file.getId(),
                file.getFilename(),
                file.isBookmarked(),
                file.getSize(),
                file.getType(),
                file.getCreatedAt()
        );
    }

}

