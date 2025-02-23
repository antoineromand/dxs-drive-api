package com.dxs.DriveProject.infrastructure.repositories.folder;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
    public Optional<MongoFolderEntity> findByFolderIdAndUserId(String id, String ownerId) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(id).and("owner_id").is(ownerId);
        query.addCriteria(criteria);
        return Optional.ofNullable(mongoTemplate.findOne(query, MongoFolderEntity.class));
    }

    @Override
    public boolean isExist(String folderId) {
        return this.folderExists(folderId, null);
    }

    @Override
    public boolean isOwnedById(String folderId, String userId) {
        return this.folderExists(folderId, userId);
    }

    private boolean folderExists(String folderId, String userId) {
        if (!ObjectId.isValid(folderId)) {
            return false;
        }

        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(new ObjectId(folderId));

        if (userId != null) {
            criteria = criteria.and("owner_id").is(userId);
        }

        query.addCriteria(criteria);
        return mongoTemplate.exists(query, MongoFolderEntity.class);
    }

}
