package com.dxs.DriveProject.web.controllers;

import com.dxs.DriveProject.application.usecases.CreateFolderUseCase;
import com.dxs.DriveProject.application.usecases.UploadFileUseCase;
import com.dxs.DriveProject.domain.File;
import com.dxs.DriveProject.domain.Folder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController()
@RequestMapping("folders")
public class FoldersController {
    private final CreateFolderUseCase createFolderUseCase;
    public FoldersController(CreateFolderUseCase createFolderUseCase) {
        this.createFolderUseCase = createFolderUseCase;
    }

    @PostMapping(consumes = { "multipart/form-data" }, value = "/create")
    public Folder uploadFiles(@RequestParam("userId") String userId,
                              @RequestParam("foldername") String foldername,
                              @RequestParam(value = "parentId", required = false) String parentId) throws IOException {
        return this.createFolderUseCase.execute(userId, foldername, parentId);
    }

}
