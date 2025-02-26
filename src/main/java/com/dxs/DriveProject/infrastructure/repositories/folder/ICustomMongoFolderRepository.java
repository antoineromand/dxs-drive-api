package com.dxs.DriveProject.infrastructure.repositories.folder;

import java.util.Optional;

import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;

public interface ICustomMongoFolderRepository {
    MongoFolderEntity save(MongoFolderEntity folder);

    Optional<MongoFolderEntity> findByFolderIdAndUserId(String id, String userId);

    boolean isExist(String folderId);

    boolean isOwnedById(String folderId, String userId);

}
