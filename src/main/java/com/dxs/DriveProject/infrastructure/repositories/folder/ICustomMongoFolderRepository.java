package com.dxs.DriveProject.infrastructure.repositories.folder;

import java.util.Optional;

import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;

public interface ICustomMongoFolderRepository {
    MongoFolderEntity insert(MongoFolderEntity folder);

    Optional<MongoFolderEntity> findById(String id);
}
