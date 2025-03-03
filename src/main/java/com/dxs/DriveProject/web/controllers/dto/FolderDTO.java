package com.dxs.DriveProject.web.controllers.dto;

import com.dxs.DriveProject.domain.Folder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FolderDTO {
    private String id;
    private String foldername;
    private Boolean bookmark;
    private Date createdAt;

    public FolderDTO(String id, String foldername, Boolean bookmark, Date createdAt) {
        this.id = id;
        this.foldername = foldername;
        this.bookmark = bookmark;
        this.createdAt = createdAt;
    }

    public static FolderDTO fromEntity(Folder folder) {
        return new FolderDTO(
                folder.getId(),
                folder.getFoldername(),
                folder.isBookmarked(),
                folder.getCreatedAt()
        );
    }

}

