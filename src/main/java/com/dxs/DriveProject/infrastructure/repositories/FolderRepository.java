package com.dxs.DriveProject.infrastructure.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;

public interface FolderRepository extends MongoRepository<MongoFolderEntity, String> {

}
