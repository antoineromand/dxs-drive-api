package com.dxs.DriveProject.infrastructure.repositories.file;

import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;

public interface ICustomMongoFileRepository {
    MongoFileEntity insert(MongoFileEntity file);
}
