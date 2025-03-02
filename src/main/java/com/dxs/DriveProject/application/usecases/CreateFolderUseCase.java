package com.dxs.DriveProject.application.usecases;

import com.dxs.DriveProject.application.usecases.dto.UploadError;
import com.dxs.DriveProject.application.usecases.dto.UploadErrorType;
import com.dxs.DriveProject.application.usecases.dto.UploadResponse;
import com.dxs.DriveProject.domain.Folder;
import com.dxs.DriveProject.domain.exceptions.ParentFolderNotFoundException;
import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;
import com.dxs.DriveProject.web.controllers.dto.FileDTO;
import com.dxs.DriveProject.web.controllers.dto.FolderDTO;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class CreateFolderUseCase {
    private IStorageService storageService;
    private ICustomMongoFolderRepository folderRepository;
    private final MongoClient mongoClient;

    public CreateFolderUseCase(IStorageService iStorageService, ICustomMongoFolderRepository iCustomMongoFolderRepository, MongoClient client) {
        this.storageService = iStorageService;
        this.folderRepository = iCustomMongoFolderRepository;
        this.mongoClient = client;
    }

    public UploadResponse<FolderDTO> execute(String userId, String foldername, String parentId) {
        List<UploadError> errors = new ArrayList<>();

        if (userId == null || userId.isEmpty()) {
            errors.add(new UploadError("parameter:userId", "UserId parameter is missing !", UploadErrorType.INVALID_PARAMETER));
            return new UploadResponse<>(null, errors);
        }
        if (foldername == null || foldername.isEmpty()) {
            errors.add(new UploadError("parameter:foldername", "Foldername parameter is missing !", UploadErrorType.INVALID_PARAMETER));
            return new UploadResponse<>(null, errors);
        }

        String parentPath = null;
        if (parentId != null && !parentId.isEmpty()) {
            Optional<MongoFolderEntity> parentFolder = this.folderRepository.findByFolderIdAndUserId(parentId, userId);
            if (parentFolder.isEmpty()) {
                errors.add(new UploadError("parameter:folderId", "Parent folder not found!", UploadErrorType.FOLDER_NOT_FOUND));
                return new UploadResponse<>(null, errors);
            }
            parentPath = parentFolder.get().toDomain().getPath();
        }

        ClientSession session = mongoClient.startSession();
        boolean transactionStarted = false;

        try {
            session.startTransaction();
            transactionStarted = true;

            Folder newFolder = new Folder(null, userId, foldername, null, parentId, false, false, new Date());
            MongoFolderEntity mongoFolderPayload = MongoFolderEntity.fromDomain(newFolder);

            MongoFolderEntity insertedFolder;
            try {
                insertedFolder = this.folderRepository.save(mongoFolderPayload);
            } catch (Exception e) {
                errors.add(new UploadError("database:save", "Database error while saving the folder!", UploadErrorType.DATABASE_ERROR));
                session.abortTransaction();
                return new UploadResponse<>(null, errors);
            }

            Folder insertedFolderDomain = insertedFolder.toDomain();
            String folderId = insertedFolderDomain.getId();

            String folderPath;
            try {
                folderPath = this.storageService.writeFolder(userId, folderId, parentPath);
            } catch (NoSuchFileException e) {
                errors.add(new UploadError("folder:parent", "Parent folder not found on storage!", UploadErrorType.FOLDER_NOT_FOUND));
                session.abortTransaction();
                return new UploadResponse<>(null, errors);
            } catch (IOException e) {
                errors.add(new UploadError("folder:write", "Error writing folder to storage!", UploadErrorType.FOLDER_WRITE_ERROR));
                session.abortTransaction();
                return new UploadResponse<>(null, errors);
            }

            insertedFolderDomain.setPath(folderPath);

            try {
                MongoFolderEntity updatedFolderMongo = this.folderRepository.save(MongoFolderEntity.fromDomain(insertedFolderDomain));
                session.commitTransaction();
                return new UploadResponse<>(FolderDTO.fromEntity(updatedFolderMongo.toDomain()), errors);
            } catch (Exception e) {
                errors.add(new UploadError("database:update", "Error updating folder path in database!", UploadErrorType.DATABASE_ERROR));
                session.abortTransaction();
                return new UploadResponse<>(null, errors);
            }

        } finally {
            if (transactionStarted) {
                session.close();
            }
        }
    }

}
