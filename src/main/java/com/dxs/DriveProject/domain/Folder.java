package com.dxs.DriveProject.domain;

import java.util.Date;

public class Folder {
    private String id;
    private String ownerId;
    private String foldername;
    private String path;
    private String parentId;
    private Boolean bookmark;
    private Long size;
    private Boolean softDeleted;
    private Date createdAt;

    public Folder(String id, String ownerId, String foldername, String path, String parentId, Boolean bookmark,
            Boolean softDeleted,
            Date createdAt) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("Owner ID is required and cannot be empty");
        }
        if (foldername == null || foldername.isBlank()) {
            throw new IllegalArgumentException("Foldername is required and cannot be empty");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path is required and cannot be empty");
        }
        this.id = id;
        this.ownerId = ownerId;
        this.foldername = foldername;
        this.parentId = parentId;
        this.path = path;
        this.bookmark = bookmark;
        this.softDeleted = softDeleted;
        this.createdAt = createdAt;
    }

    public void moveToFolder(String newParent) {
        this.parentId = newParent;
    }

    public void renameFolder(String newFolderName) {
        this.foldername = newFolderName;
    }

    public void markAsDeleted() {
        this.softDeleted = true;
    }

    public void toggleBookmark() {
        this.bookmark = !this.bookmark;
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
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

    public Boolean isBookmarked() {
        return bookmark;
    }

    public Long getSize() {
        return size;
    }

    public Boolean isSoftDeleted() {
        return softDeleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

}
