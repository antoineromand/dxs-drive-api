package com.dxs.DriveProject.infrastructure.entities;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import org.junit.jupiter.api.Test;

import com.dxs.DriveProject.domain.Folder;

public class MongoFolderEntityTest {
    @Test
    void shouldConvertBetweenMongoEntityAndDomainCorrectly() {
        String foldername = "test";
        String ownerId = "user-123";
        Date createdAt = new Date();

        MongoFolderEntity mongoFolderEntity = MongoFolderEntity.builder()
                .id("xxx-xxz")
                .ownerId(ownerId)
                .parentId(null)
                .path("/")
                .bookmark(true)
                .createdAt(createdAt)
                .softDelete(false)
                .foldername(foldername)
                .build();

        Folder domainFolder = mongoFolderEntity.toDomain();

        assertEquals(mongoFolderEntity.getId(), domainFolder.getId());
        assertEquals(mongoFolderEntity.getOwnerId(), domainFolder.getOwnerId());
        assertEquals(mongoFolderEntity.getParentId(), domainFolder.getParentId());
        assertEquals(mongoFolderEntity.getFoldername(), domainFolder.getFoldername());
        assertEquals(mongoFolderEntity.getPath(), domainFolder.getPath());
        assertEquals(mongoFolderEntity.getBookmark(), domainFolder.isBookmarked());
        assertEquals(mongoFolderEntity.getSoftDelete(), domainFolder.isSoftDeleted());
        assertEquals(mongoFolderEntity.getCreatedAt(), domainFolder.getCreatedAt());

        assertNotSame(mongoFolderEntity, domainFolder);

        domainFolder.renameFolder("test2");

        MongoFolderEntity mongoNewFolder = MongoFolderEntity.fromDomain(domainFolder);

        assertEquals("test2", mongoNewFolder.getFoldername());
        assertEquals(mongoFolderEntity.getId(), mongoNewFolder.getId());
        assertEquals(mongoFolderEntity.getOwnerId(), mongoNewFolder.getOwnerId());
        assertEquals(mongoFolderEntity.getPath(), mongoNewFolder.getPath());
        assertEquals(mongoFolderEntity.getSoftDelete(), mongoNewFolder.getSoftDelete());
        assertEquals(mongoFolderEntity.getCreatedAt(), mongoNewFolder.getCreatedAt());
        assertEquals(mongoFolderEntity.getParentId(), mongoNewFolder.getParentId());

        assertNotSame(mongoFolderEntity, mongoNewFolder);
    }
}
