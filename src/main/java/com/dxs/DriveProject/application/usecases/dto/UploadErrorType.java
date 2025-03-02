package com.dxs.DriveProject.application.usecases.dto;

public enum UploadErrorType {
    INVALID_PARAMETER,
    FOLDER_NOT_FOUND,
    FORBIDDEN_ACCESS,
    FILE_VALIDATION_ERROR,
    FILE_WRITE_ERROR,
    FOLDER_WRITE_ERROR,
    FOLDER_ALREADY_EXISTS,
    DATABASE_ERROR,
}
