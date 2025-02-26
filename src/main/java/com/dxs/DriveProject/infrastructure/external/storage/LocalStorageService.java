package com.dxs.DriveProject.infrastructure.external.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

import com.dxs.DriveProject.infrastructure.external.storage.files.FilesWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageService implements IStorageService {

    private FilesWrapper filesWrapper;

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

        Path pathFile = uploadPath.resolve(file.getOriginalFilename());

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

        Path folderPath;

        if (parentPath != null && !parentPath.isEmpty()) {
            Path _parentPath = Path.of(parentPath);
            if (!filesWrapper.exists(_parentPath)) {
                throw new NoSuchFileException("Parent folder could not be founded !");
            }
            folderPath = Path.of(parentPath, folderId);
        } else {
            folderPath = Path.of("uploads", userId, folderId);
        }

        if (filesWrapper.exists(folderPath)) {
            throw new FileAlreadyExistsException("Folder already exists !");
        }

        Path result = filesWrapper.createDirectories(folderPath);

        return result.toString();
    }

}
