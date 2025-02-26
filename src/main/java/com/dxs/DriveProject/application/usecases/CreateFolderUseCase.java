package com.dxs.DriveProject.application.usecases;

import com.dxs.DriveProject.domain.Folder;
import com.dxs.DriveProject.domain.exceptions.ParentFolderNotFoundException;
import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
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
    public Folder execute(String userId, String foldername, String parentId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User not found !");
        }
        if (foldername == null || foldername.isEmpty()) {
            throw new IllegalArgumentException("A foldername must be provided !");
        }

        String parentPath = null;
        if (parentId != null && !parentId.isEmpty()) {
            Optional<MongoFolderEntity> parentFolder = this.folderRepository.findByFolderIdAndUserId(parentId, userId);
            if (parentFolder.isEmpty()) {
                throw new ParentFolderNotFoundException(parentId);
            }
            parentPath = parentFolder.get().toDomain().getPath();
        }
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();

            Folder newFolder = new Folder(null, userId, foldername, null, parentId, false, false, new Date());
            MongoFolderEntity mongoFolderPayload = MongoFolderEntity.fromDomain(newFolder);
            MongoFolderEntity insertedFolder = this.folderRepository.save(mongoFolderPayload);

            Folder insertedFolderDomain = insertedFolder.toDomain();
            String folderId = insertedFolderDomain.getId();

            String folderPath = this.storageService.writeFolder(userId, folderId, parentPath);
            insertedFolderDomain.setPath(folderPath);

            MongoFolderEntity updatedFolderMongo = this.folderRepository.save(MongoFolderEntity.fromDomain(insertedFolderDomain));
            session.commitTransaction();

            return updatedFolderMongo.toDomain();
        } catch (IOException e) {
            session.abortTransaction();
            throw new RuntimeException("Error while writing folder: ", e);
        } finally {
            session.close();
        }
    }

}
