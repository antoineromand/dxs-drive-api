package com.dxs.DriveProject.application.usecases.dto;

import lombok.Getter;

@Getter
public class UploadError {
    private String target;
    private String message;
    private UploadErrorType errorType;


    public UploadError(String target, String message, UploadErrorType errorType) {
        this.target = target;
        this.message = message;
        this.errorType = errorType;
    }
}
