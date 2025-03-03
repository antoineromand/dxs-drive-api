package com.dxs.DriveProject.application.usecases.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UploadResponse<T> {
    private T data;
    private List<UploadError> errors;

    public UploadResponse(T uploadedData, List<UploadError> errors) {
        this.data = uploadedData;
        this.errors = errors;
    }

}
