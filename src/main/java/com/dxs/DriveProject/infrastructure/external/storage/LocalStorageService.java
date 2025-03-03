package com.dxs.DriveProject.infrastructure.external.storage;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

import com.dxs.DriveProject.infrastructure.external.storage.files.FilesWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageService implements IStorageService {

    private final FilesWrapper filesWrapper;

    public LocalStorageService(FilesWrapper filesWrapper) {
        this.filesWrapper = filesWrapper;
    }

    @Override
    public String writeFile(MultipartFile file, String userId, String folderPath) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File not found !");
        }
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User not provided !");
        }
        Path uploadPath;
        if (folderPath != null && !folderPath.isEmpty()) {
            uploadPath = Path.of(folderPath);
        } else {
            uploadPath = Path.of("uploads", userId);
        }

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        UUID generateId = UUID.randomUUID();

        String ext = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf('.'));

        Path pathFile = uploadPath.resolve(generateId + ext);

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, pathFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return pathFile.toString();

    }

    @Override
    public String writeFolder(String userId, String folderId, String parentPath) throws IOException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User not found !");
        }
        if (folderId == null || folderId.isEmpty()) {
            throw new IllegalArgumentException("Folder not found !");
        }

        Path baseUploadPath = null;

        if (parentPath != null && !parentPath.isEmpty()) {
            baseUploadPath = Path.of(parentPath);
            if (!filesWrapper.exists(baseUploadPath)) {
                throw new NoSuchFileException("Parent folder could not be found !");
            }
            baseUploadPath = baseUploadPath.resolve(folderId);
        } else {
            baseUploadPath = Path.of("uploads").resolve(userId).resolve(folderId);
        }

        if (filesWrapper.exists(baseUploadPath)) {
            throw new FileAlreadyExistsException("Folder already exists !");
        }

        filesWrapper.createDirectories(baseUploadPath);

        return baseUploadPath.toString();
    }


}
