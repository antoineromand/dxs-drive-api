package com.dxs.DriveProject.application.usecases;

import com.dxs.DriveProject.domain.Folder;
import com.dxs.DriveProject.domain.exceptions.ParentFolderNotFoundException;
import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;

import java.util.Optional;

public class CreateFolderUseCase {
    private IStorageService storageService;
    private ICustomMongoFolderRepository folderRepository;

    public CreateFolderUseCase(IStorageService iStorageService, ICustomMongoFolderRepository iCustomMongoFolderRepository) {
        this.storageService = iStorageService;
        this.folderRepository = iCustomMongoFolderRepository;
    }
    public Folder execute(String userId, String foldername, String parentId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User not found !");
        }
        if (foldername == null || foldername.isEmpty()) {
            throw new IllegalArgumentException("A foldername must be provided !");
        }

        String parentPath;

        if (parentId != null && !parentId.isEmpty()) {
            Optional<MongoFolderEntity> parentFolder = this.folderRepository.findByFolderIdAndUserId(parentId, userId);
            if (parentFolder.isEmpty()) {
                throw new ParentFolderNotFoundException(parentId);
            }
            parentPath = parentFolder.get().toDomain().getPath();
        }

        return null;
    }
}
