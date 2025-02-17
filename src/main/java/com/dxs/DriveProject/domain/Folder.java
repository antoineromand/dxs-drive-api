package com.dxs.DriveProject.domain;

import java.sql.Date;
import java.util.ArrayList;
import java.util.UUID;

public class Folder {
    private String id;
    private UUID ownerId;
    private String foldername;
    private String path;
    private String parentId;
    private Boolean isBookmarked;
    private Long size;
    private Boolean isSoftDeleted;
    private Date createdAt;

    public Folder(String id, UUID ownerId, String foldername, String path, String parentId, Boolean isBookmarked, Boolean isSoftDeleted,
            Date createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.foldername = foldername;
        this.parentId = parentId;
        this.path = path;
        this.isBookmarked = isBookmarked;
        this.isSoftDeleted = isSoftDeleted;
        this.createdAt = createdAt;
    }

    public Long getSize(ArrayList<File> filesInFolder) {
        Long res = 0L;
        for (File file : filesInFolder) {
            res += file.getSize();
        }
        return res;
    }

    public void renameFolder(String newFolderName) {
        this.foldername = newFolderName;
    }

    public void markAsDeleted() {
        this.isSoftDeleted = true;
    }

    public void toggleBookmark() {
        this.isBookmarked = !this.isBookmarked;
    }

    public String getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getFoldername() {
        return foldername;
    }

    public String getPath() {
        return path;
    }

    public String getParentId() {
        return parentId;
    }

    public Boolean getIsBookmarked() {
        return isBookmarked;
    }

    public Long getSize() {
        return size;
    }

    public Boolean getIsSoftDeleted() {
        return isSoftDeleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    
}
