package com.dxs.DriveProject.infrastructure.external.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.web.multipart.MultipartFile;

public class LocalStorageService implements IStorageService {

    @Override
    public String writeFile(MultipartFile file, String userId, String folderId) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File not found !");
        }
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User not provided !");
        }
        Path uploadPath;
        if (folderId != null && !folderId.isEmpty()) {
            uploadPath = Path.of("uploads", folderId, userId);
        } else {
            uploadPath = Path.of("uploads", userId);
        }

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path pathFile = uploadPath.resolve(file.getOriginalFilename());

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, pathFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return pathFile.toAbsolutePath().toString();
    }

}
