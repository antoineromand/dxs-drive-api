package com.dxs.DriveProject.application.usecases;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;

public class UploadFile {
    private final IStorageService storageService;

    public UploadFile(IStorageService storageService) {
        this.storageService = storageService;
    }

    public void execute(List<MultipartFile> files, String userId, String folderId) throws IOException {

        for (MultipartFile file : files) {
            try {
                String uploadPath = this.storageService.writeFile(file, userId, folderId);
                System.out.println(uploadPath);
            } catch (IOException e) {
                System.out.println("erreur : " + e.getMessage());
            }
        }
    }
}
