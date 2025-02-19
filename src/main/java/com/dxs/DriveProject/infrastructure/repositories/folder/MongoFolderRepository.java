package com.dxs.DriveProject.infrastructure.repositories.folder;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;

public interface MongoFolderRepository extends MongoRepository<MongoFolderEntity, String> {

}
