package com.dxs.DriveProject.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.application.usecases.UploadFileUseCase;
import com.dxs.DriveProject.domain.File;

@RestController()
@RequestMapping()
public class TestController {

    private final UploadFileUseCase uploadFileUseCase;

    public TestController(UploadFileUseCase usecase) {
        this.uploadFileUseCase = usecase;
    }

    @GetMapping()
    private String getHello() {
        return "Hello";
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ArrayList<File> uploadFiles(@RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folderId", required = false) String folderId) throws IOException {
        String userId = UUID.randomUUID().toString();
        ArrayList<File> result = uploadFileUseCase.execute(files, userId, folderId);
        return result;
    }

}
