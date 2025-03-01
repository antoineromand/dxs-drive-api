package com.dxs.DriveProject.application.usecases;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.dxs.DriveProject.application.usecases.dto.UploadError;
import com.dxs.DriveProject.application.usecases.dto.UploadErrorType;
import com.dxs.DriveProject.application.usecases.dto.UploadResponse;
import com.dxs.DriveProject.domain.Folder;
import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;
import com.dxs.DriveProject.web.controllers.dto.FileDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.domain.File;
import com.dxs.DriveProject.domain.object_values.FileType;
import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.file.ICustomMongoFileRepository;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;

@Service
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

    public UploadResponse<List<FileDTO>> execute(List<MultipartFile> files, String userId, String folderId)
            throws IOException {
        List<FileDTO> uploadedFiles = new ArrayList<>();
        List<UploadError> errors = new ArrayList<>();

        if (userId == null) {
            errors.add(new UploadError("parameter:userId", "UserId parameter is missing ! Cannot authenticate user.", UploadErrorType.INVALID_PARAMETER));
            return new UploadResponse<>(uploadedFiles, errors);
        }
        if (files == null || files.isEmpty()) {
            errors.add(new UploadError("parameter:files", "files parameter is missing ! Files must be provided.", UploadErrorType.INVALID_PARAMETER));
            return new UploadResponse<>(uploadedFiles, errors);
        }

        String parentPath;

        if (folderId != null) {
            if (!folderRepository.isExist(folderId)) {
                errors.add(new UploadError("parameter:folderId", "Folder does not exist.", UploadErrorType.FOLDER_NOT_FOUND));
                return new UploadResponse<>(uploadedFiles, errors);
            }
            if (!folderRepository.isOwnedById(folderId, userId)) {
                errors.add(new UploadError("parameter:folderId", "User is not authorize to add files in this folder.", UploadErrorType.FORBIDDEN_ACCESS));
                return new UploadResponse<>(uploadedFiles, errors);
            }
            Optional<MongoFolderEntity> folderMongoEntity = this.folderRepository.findByFolderIdAndUserId(folderId, userId);
            if (folderMongoEntity.isPresent()) {
                Folder folder = folderMongoEntity.get().toDomain();
                parentPath = folder.getPath();
            } else {
                parentPath = null;
            }
        } else {
            parentPath = null;
        }

        List<MongoFileEntity> filesToInsert = files.stream()
                .filter(file -> {
                    long maxSizeFile = 50L * 1024 * 1024;
                    if (!FileType.isTypeValide(file.getContentType())) {
                        errors.add(new UploadError("files:" + file.getOriginalFilename(), "File type is not valid!", UploadErrorType.FILE_VALIDATION_ERROR));
                        return false;
                    }
                    if (file.getSize() <= 0) {
                        errors.add(new UploadError("files:" + file.getOriginalFilename(), "File size must be positive!", UploadErrorType.FILE_VALIDATION_ERROR));
                        return false;
                    }
                    if (file.getSize() > maxSizeFile) {
                        errors.add(new UploadError("files:" + file.getOriginalFilename(), "File size is too large!", UploadErrorType.FILE_VALIDATION_ERROR));
                        return false;
                    }
                    return true;
                })
                .map(file -> {
                    try {
                        String path = storageService.writeFile(file, userId, parentPath);
                        return Optional.of(MongoFileEntity.fromDomain(this.convertToFile(userId, folderId, path, file)));
                    } catch (IOException e) {
                        errors.add(new UploadError("files:" + file.getOriginalFilename(), "File writing failed!", UploadErrorType.FILE_WRITE_ERROR));
                        return Optional.<MongoFileEntity>empty();
                    }
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(ArrayList::new));


        if (!filesToInsert.isEmpty()) {
            List<MongoFileEntity> insertedFiles = this.fileRepository.insertMany(filesToInsert);
            return new UploadResponse<>(this.convertDBFileToDomainFile(insertedFiles), errors);
        }

        return new UploadResponse<>(uploadedFiles, errors);

    }


    private File convertToFile(String userId, String folderId, String path, MultipartFile file) {
        return new File(null, userId, folderId, file.getOriginalFilename(), path, false,
                file.getSize(), file.getContentType(), false, new Date());
    }

    private List<FileDTO> convertDBFileToDomainFile(List<MongoFileEntity> insertedFiles) {
        return insertedFiles.stream().map(MongoFileEntity::toDomain).map(FileDTO::fromEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
