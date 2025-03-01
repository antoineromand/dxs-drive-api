package com.dxs.DriveProject.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dxs.DriveProject.application.usecases.dto.UploadResponse;
import com.dxs.DriveProject.infrastructure.config.security.CustomAuthenticationToken;
import com.dxs.DriveProject.web.controllers.dto.ApiSuccessResponse;
import com.dxs.DriveProject.web.controllers.dto.FileDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.application.usecases.UploadFileUseCase;
import com.dxs.DriveProject.domain.File;

@RestController()
@RequestMapping("files")
public class FilesController {
    private final UploadFileUseCase uploadFileUseCase;

    public FilesController(UploadFileUseCase uploadUsecase) {
        this.uploadFileUseCase = uploadUsecase;
    }

    @PostMapping(consumes = { "multipart/form-data" }, value = "/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam(value = "files", required = false) List<MultipartFile> files,
                                         @RequestParam(value = "folderId", required = false) String folderId) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getPrincipal().toString();
        UploadResponse<List<FileDTO>> result = this.uploadFileUseCase.execute(files, userId, folderId);

        if (result.getData().isEmpty() && !result.getErrors().isEmpty()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        if (!result.getErrors().isEmpty()) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(result);
        }

        return ResponseEntity.ok(new ApiSuccessResponse<>(result.getData(), "Files uploaded successfully."));
    }

}
