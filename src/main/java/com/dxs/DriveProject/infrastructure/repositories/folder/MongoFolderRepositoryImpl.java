package com.dxs.DriveProject.infrastructure.repositories.folder;

import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;

@Repository

public class MongoFolderRepositoryImpl implements
        ICustomMongoFolderRepository {

    private final MongoTemplate mongoTemplate;

    public MongoFolderRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public MongoFolderEntity insert(MongoFolderEntity file) {
        return mongoTemplate.insert(file);
    }

    @Override
    public Optional<MongoFolderEntity> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, MongoFolderEntity.class));
    }

}
