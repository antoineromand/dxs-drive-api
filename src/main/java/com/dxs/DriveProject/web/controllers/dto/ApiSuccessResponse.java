package com.dxs.DriveProject.web.controllers.dto;

import lombok.Getter;

@Getter
public class ApiSuccessResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public ApiSuccessResponse(T data, String message) {
        this.success = true;
        this.data = data;
        this.message = message;
    }
}