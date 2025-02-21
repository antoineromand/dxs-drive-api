package com.dxs.DriveProject.application.usecases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.domain.File;
import com.dxs.DriveProject.domain.exceptions.AccessFolderUnauthorizedException;
import com.dxs.DriveProject.domain.exceptions.FolderNotFoundException;
import com.dxs.DriveProject.domain.object_values.FileType;
import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;
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

    public ArrayList<File> execute(List<MultipartFile> files, String userId, String folderId)
            throws IOException {
        this.checkIfInputsAreValid(files, userId);
        if (folderId != null) {
            this.checkIfFolderExistsWhenFolderIdIsSpecified(userId, folderId);
        }

        ArrayList<MongoFileEntity> filesToInsert = files.stream()
                .peek(this::checkIfFileIsValid)
                .map(file -> writeFileAndConvert(file, userId, folderId)) // Méthode séparée pour gérer IOException
                .collect(Collectors.toCollection(ArrayList::new));

        if (!filesToInsert.isEmpty()) {
            ArrayList<MongoFileEntity> insertedFiles = this.fileRepository.insertMany(filesToInsert);
            ArrayList<File> returnedFiles = this.convertDBFileToDomainFile(insertedFiles);
            return returnedFiles;
        }

        return new ArrayList<>();

    }

    private void checkIfInputsAreValid(List<MultipartFile> files, String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID parameter is missing");

        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Files parameter is null or empty");
        }
    }

    private void checkIfFolderExistsWhenFolderIdIsSpecified(String userId, String folderId) {
        if (!folderRepository.isExist(folderId)) {
            throw new FolderNotFoundException(folderId);
        }
        if (!folderRepository.isOwnedById(folderId, userId)) {
            throw new AccessFolderUnauthorizedException(folderId, userId);
        }
    }

    private MongoFileEntity writeFileAndConvert(MultipartFile file, String userId, String folderId) {
        try {
            String path = storageService.writeFile(file, userId, folderId);
            return MongoFileEntity.fromDomain(this.convertToFile(userId, folderId, path, file));
        } catch (IOException e) {
            throw new RuntimeException("File writing failed for " + file.getOriginalFilename(), e);
        }
    }

    private void checkIfFileIsValid(MultipartFile file) {
        long maxSizeFile = 50L * 1024 * 1024;
        if (!FileType.isTypeValide(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file: format is not supported");
        }
        if (file.getSize() <= 0) {
            throw new IllegalArgumentException("Invalid file: size must not be equal to 0 or less");
        }
        if (file.getSize() > maxSizeFile) {
            throw new IllegalArgumentException("Invalid file: size must not exceed 50 mo");
        }
    }

    private File convertToFile(String userId, String folderId, String path, MultipartFile file) {
        return new File(null, userId, folderId, file.getOriginalFilename(), path, false,
                file.getSize(), file.getContentType(), false, new Date());
    }

    private ArrayList<File> convertDBFileToDomainFile(ArrayList<MongoFileEntity> insertedFiles) {
        return insertedFiles.stream().map(MongoFileEntity::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
