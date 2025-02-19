package com.dxs.DriveProject.infrastructure.repositories.file;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;

@Repository
public class MongoFileRepositoryImpl implements ICustomMongoFileRepository {

    private final MongoTemplate mongoTemplate;

    public MongoFileRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public MongoFileEntity insert(MongoFileEntity file) {
        return mongoTemplate.insert(file);
    }

}
