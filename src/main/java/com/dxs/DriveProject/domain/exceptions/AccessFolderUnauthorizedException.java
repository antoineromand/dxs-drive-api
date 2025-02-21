package com.dxs.DriveProject.domain.exceptions;

public class AccessFolderUnauthorizedException extends RuntimeException {
    public AccessFolderUnauthorizedException(String folderId, String userId) {
        super("User " + userId + " is not authorized to access folder " + folderId);
    }
}
