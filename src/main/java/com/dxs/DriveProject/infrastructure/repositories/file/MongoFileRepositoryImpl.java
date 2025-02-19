package com.dxs.DriveProject.infrastructure.repositories.file;

import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;

public class MongoFileRepositoryImpl implements ICustomMongoFileRepository {

    private final MongoFileRepository mongoFileRepository;

    public MongoFileRepositoryImpl(MongoFileRepository repository) {
        this.mongoFileRepository = repository;
    }

    @Override
    public MongoFileEntity insert(MongoFileEntity file) {
        return mongoFileRepository.insert(file);
    }

}
