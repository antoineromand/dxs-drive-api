package com.dxs.DriveProject.domain.exceptions;

public class FolderNotFoundException extends RuntimeException {
    public FolderNotFoundException(String folderId) {
        super("Folder with ID " + folderId + " not found.");
    }
}