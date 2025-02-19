package com.dxs.DriveProject.infrastructure.entities;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import org.junit.jupiter.api.Test;

import com.dxs.DriveProject.domain.File;

public class MongoFileEntityTest {

    @Test
    void shouldConvertBetweenMongoEntityAndDomainCorrectly() {
        String filename = "test.txt";
        String ownerId = "user-123";
        String folderId = "folder-456";
        Date createdAt = new Date();

        MongoFileEntity mongoFileEntity = MongoFileEntity.builder()
                .id("xxx-1")
                .ownerId(ownerId)
                .folderId(folderId)
                .filename(filename)
                .path("/xxxxx/xxxxx")
                .bookmark(false)
                .size(1530L)
                .type("application/pdf")
                .softDelete(false)
                .createdAt(createdAt)
                .build();

        File domainFile = mongoFileEntity.toDomain();

        assertEquals(mongoFileEntity.getId(), domainFile.getId());
        assertEquals(mongoFileEntity.getOwnerId(), domainFile.getOwnerId());
        assertEquals(mongoFileEntity.getFolderId(), domainFile.getFolderId());
        assertEquals(mongoFileEntity.getFilename(), domainFile.getFilename());
        assertEquals(mongoFileEntity.getPath(), domainFile.getPath());
        assertEquals(mongoFileEntity.getBookmark(), domainFile.isBookmarked());
        assertEquals(mongoFileEntity.getSize(), domainFile.getSize());
        assertEquals(mongoFileEntity.getType(), domainFile.getType());
        assertEquals(mongoFileEntity.getSoftDelete(), domainFile.isSoftDeleted());
        assertEquals(mongoFileEntity.getCreatedAt(), domainFile.getCreatedAt());

        assertNotSame(mongoFileEntity, domainFile);

        domainFile.renameFile("new.txt");

        MongoFileEntity mongoNewFile = MongoFileEntity.fromDomain(domainFile);

        assertEquals("new.txt", mongoNewFile.getFilename());
        assertEquals(mongoFileEntity.getId(), mongoNewFile.getId());
        assertEquals(mongoFileEntity.getOwnerId(), mongoNewFile.getOwnerId());
        assertEquals(mongoFileEntity.getPath(), mongoNewFile.getPath());
        assertEquals(mongoFileEntity.getSize(), mongoNewFile.getSize());
        assertEquals(mongoFileEntity.getType(), mongoNewFile.getType());
        assertEquals(mongoFileEntity.getSoftDelete(), mongoNewFile.getSoftDelete());
        assertEquals(mongoFileEntity.getCreatedAt(), mongoNewFile.getCreatedAt());

        assertNotSame(mongoFileEntity, mongoNewFile);
    }
}
