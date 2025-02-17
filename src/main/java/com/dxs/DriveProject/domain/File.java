package com.dxs.DriveProject.domain;

import java.sql.Date;
import java.util.UUID;

public class File {
    private String id;
    private UUID ownerId;
    private String folderId;
    private String filename;
    private String path;
    private Boolean bookmark;
    private Long size;
    private String type;
    private Boolean softDelete;
    private Date createdAt;

    public File(String id, UUID ownerId, String folderId, String filename, String path, Boolean bookmark, Long size,
            String type, Boolean softDelete, Date createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.folderId = folderId;
        this.filename = filename;
        this.path = path;
        this.bookmark = bookmark;
        this.size = size;
        this.type = type;
        this.softDelete = softDelete;
        this.createdAt = createdAt;
    }

    public void moveToFolder(String newFolderId) {
        this.folderId = newFolderId;
    }

    public void renameFile(String newFilename) {
        this.filename = newFilename;
    }

    public void markAsDeleted() {
        this.softDelete = true;
    }

    public void toggleBookmark() {
        this.bookmark = !this.bookmark;
    }

    public Long getSize() {
        return this.size;
    }

    public String getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getFolderId() {
        return folderId;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public Boolean getBookmark() {
        return bookmark;
    }

    public String getType() {
        return type;
    }

    public Boolean getSoftDelete() {
        return softDelete;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

}
