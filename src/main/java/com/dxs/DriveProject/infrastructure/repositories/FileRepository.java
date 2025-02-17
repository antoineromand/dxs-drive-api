package com.dxs.DriveProject.infrastructure.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;

public interface FileRepository extends MongoRepository<MongoFileEntity, String> {

}
