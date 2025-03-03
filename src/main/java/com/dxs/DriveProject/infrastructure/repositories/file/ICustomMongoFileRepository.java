package com.dxs.DriveProject.infrastructure.repositories.file;

import java.util.ArrayList;
import java.util.List;

import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;

public interface ICustomMongoFileRepository {
    List<MongoFileEntity> insertMany(List<MongoFileEntity> files);

}
