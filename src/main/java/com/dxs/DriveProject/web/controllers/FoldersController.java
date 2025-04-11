package com.dxs.DriveProject.web.controllers;

import com.dxs.DriveProject.application.usecases.CreateFolderUseCase;
import com.dxs.DriveProject.application.usecases.UploadFileUseCase;
import com.dxs.DriveProject.application.usecases.dto.UploadResponse;
import com.dxs.DriveProject.domain.File;
import com.dxs.DriveProject.domain.Folder;
import com.dxs.DriveProject.infrastructure.config.security.AuthenticatedUserProvider;
import com.dxs.DriveProject.web.controllers.dto.ApiSuccessResponse;
import com.dxs.DriveProject.web.controllers.dto.FileDTO;
import com.dxs.DriveProject.web.controllers.dto.FolderDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController()
@RequestMapping("private/api/v1/drive/folders")
public class FoldersController {
    private final CreateFolderUseCase createFolderUseCase;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    public FoldersController(CreateFolderUseCase createFolderUseCase, AuthenticatedUserProvider authenticatedUserProvider) {
        this.createFolderUseCase = createFolderUseCase;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping(consumes = { "multipart/form-data" }, value = "/create")
    public ResponseEntity<?> uploadFolder(@RequestParam("foldername") String foldername,
                              @RequestParam(value = "parentId", required = false) String parentId) throws IOException {
        String userId = this.authenticatedUserProvider.getAuthenticatedUserId();
        UploadResponse<FolderDTO> result = this.createFolderUseCase.execute(userId, foldername, parentId);

        if (result.getData() == null && !result.getErrors().isEmpty()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        return ResponseEntity.ok(new ApiSuccessResponse<>(result.getData(), "Folder created successfully."));
    }

}
