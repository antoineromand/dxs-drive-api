package com.dxs.DriveProject.infrastructure.repositories.file;

import java.util.ArrayList;

import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;

public interface ICustomMongoFileRepository {
    ArrayList<MongoFileEntity> insertMany(ArrayList<MongoFileEntity> files);
}
