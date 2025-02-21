package com.dxs.DriveProject.infrastructure.repositories.file;

import java.util.ArrayList;

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
    public ArrayList<MongoFileEntity> insertMany(ArrayList<MongoFileEntity> files) {
        return new ArrayList<>(mongoTemplate.insert(files, MongoFileEntity.class));
    }

}
