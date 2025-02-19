package com.dxs.DriveProject.infrastructure.repositories.folder;

import java.util.Optional;

import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;

public class MongoFolderRepositoryImpl implements ICustomMongoFolderRepository {

    private final MongoFolderRepository mongoFolderRepository;

    public MongoFolderRepositoryImpl(MongoFolderRepository repository) {
        this.mongoFolderRepository = repository;
    }

    @Override
    public MongoFolderEntity insert(MongoFolderEntity file) {
        return mongoFolderRepository.insert(file);
    }

    @Override
    public Optional<MongoFolderEntity> findById(String id) {
        return mongoFolderRepository.findById(id);
    }

}
