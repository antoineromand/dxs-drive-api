package com.dxs.DriveProject.infrastructure.repositories.file;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;

public interface MongoFileRepository extends MongoRepository<MongoFileEntity, String> {

}
