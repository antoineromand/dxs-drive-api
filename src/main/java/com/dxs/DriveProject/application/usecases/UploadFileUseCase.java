package com.dxs.DriveProject.application.usecases;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.file.ICustomMongoFileRepository;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;

public class UploadFileUseCase {
    private final IStorageService storageService;
    private final ICustomMongoFileRepository fileRepository;
    private final ICustomMongoFolderRepository folderRepository;

    public UploadFileUseCase(IStorageService storageService, ICustomMongoFileRepository fileRepository,
            ICustomMongoFolderRepository folderRepository) {
        this.storageService = storageService;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    public void execute(List<MultipartFile> files, String userId, String folderId) throws IOException {
        try {
            System.out.println("...");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Ecrire les images et retourner pour chaque fichier un path
    private Map<String, String> writeFiles(List<MultipartFile> files, String userId, String folderId)
            throws IOException {
        Map<String, String> paths = new HashMap<>();
        for (MultipartFile file : files) {
            try {
                String uploadPath = this.storageService.writeFile(file, userId, folderId);
                paths.put(file.getOriginalFilename(), uploadPath);
            } catch (IOException e) {
                System.out.println("erreur : " + e.getMessage());
            }
        }
        return paths;
    }
}
