package com.dxs.DriveProject.application.usecases;

import com.dxs.DriveProject.domain.Folder;
import com.dxs.DriveProject.domain.exceptions.ParentFolderNotFoundException;
import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CreateFolderUseCaseTest {
    private IStorageService storageService;
    private ICustomMongoFolderRepository folderRepository;
    private CreateFolderUseCase usecase;
    private MongoClient mongoClient;
    private ClientSession clientSession;

    @BeforeEach
    void init() {
        this.storageService = mock(IStorageService.class);
        this.folderRepository = mock(ICustomMongoFolderRepository.class);
        this.mongoClient = mock(MongoClient.class);
        this.clientSession = mock(ClientSession.class);
        this.usecase = new CreateFolderUseCase(this.storageService, this.folderRepository, this.mongoClient);
    }

    @Test
    void shouldThrowAnExceptionIfUserIdIsNull() {
        String userId = null;
        String parentId = null;
        String foldername = null;
        assertThrows(IllegalArgumentException.class, () -> {
            this.usecase.execute(userId, foldername, parentId);
        });
    }

    @Test
    void shouldThrowAnExceptionIfFilenameIsNull() {
        String userId = "test";
        String parentId = null;
        String foldername = null;
        assertThrows(IllegalArgumentException.class, () -> {
            this.usecase.execute(userId, foldername, parentId);
        });
    }

    @Test
    void shouldThrowAnExceptionIfParentIdIsSpecifiedButNotFound() {
        String userId = "xxx-xx1u";
        String parentId = "xxx-xx1f";
        String foldername = "test";

        when(this.folderRepository.findByFolderIdAndUserId(parentId, userId)).thenReturn(Optional.empty());

        assertThrows(ParentFolderNotFoundException.class, () -> {
            this.usecase.execute(userId, foldername, parentId);
        });
    }

    @Test
    public void shouldWriteSaveAndReturnFolder() throws IOException {
        String userId = "user123";
        String foldername = "TestFolder";
        String generatedFolderId = "folderId123";
        String expectedPath = "/uploads/user123/folderId123";

        when(mongoClient.startSession()).thenReturn(clientSession);

        Folder folderSansPath = new Folder(null, userId, foldername, null, null, false, false, new Date());
        MongoFolderEntity entitySansPath = MongoFolderEntity.fromDomain(folderSansPath);
        entitySansPath.setId(generatedFolderId);

        Folder folderAvecPath = entitySansPath.toDomain();
        folderAvecPath.setPath(expectedPath);
        MongoFolderEntity entityAvecPath = MongoFolderEntity.fromDomain(folderAvecPath);


        when(folderRepository.save(any(MongoFolderEntity.class)))
                .thenReturn(entitySansPath)
                .thenReturn(entityAvecPath);

        when(storageService.writeFolder(eq(userId), eq(generatedFolderId), any()))
                .thenReturn(expectedPath);

        Folder result = usecase.execute(userId, foldername, null);

        verify(clientSession).startTransaction();
        verify(clientSession).commitTransaction();
        verify(clientSession).close();

        ArgumentCaptor<MongoFolderEntity> captor = ArgumentCaptor.forClass(MongoFolderEntity.class);
        verify(folderRepository, times(2)).save(captor.capture());
        List<MongoFolderEntity> savedEntities = captor.getAllValues();

        MongoFolderEntity updatedEntity = savedEntities.get(1);
        assertEquals(expectedPath, updatedEntity.getPath());
        assertEquals(generatedFolderId, updatedEntity.getId());

        assertEquals(expectedPath, result.getPath());
    }

    @Test
    public void shouldThrowAnExceptionAndRollback() throws IOException {
        String userId = "user123";
        String foldername = "MonDossier";
        String parentId = "parent456";
        String generatedFolderId = "folderGenerated123";
        String expectedParentPath = "uploads/" + userId + "/" + parentId;

        MongoFolderEntity parentFolder = MongoFolderEntity.builder()
                .id(parentId)
                .ownerId(userId)
                .foldername("parentFolder")
                .path(expectedParentPath)
                .bookmark(false)
                .softDelete(false)
                .createdAt(new Date())
                .build();
        when(folderRepository.findByFolderIdAndUserId(eq(parentId), eq(userId)))
                .thenReturn(Optional.of(parentFolder));

        MongoFolderEntity insertedEntity = MongoFolderEntity.builder()
                .id(generatedFolderId)
                .ownerId(userId)
                .foldername(foldername)
                .path(null)
                .parentId(parentId)
                .bookmark(false)
                .softDelete(false)
                .createdAt(new Date())
                .build();
        when(folderRepository.save(any(MongoFolderEntity.class))).thenReturn(insertedEntity);

        when(storageService.writeFolder(eq(userId), eq(generatedFolderId), eq(expectedParentPath)))
                .thenThrow(IOException.class);

        when(mongoClient.startSession()).thenReturn(clientSession);



        assertThrows(RuntimeException.class, () -> {
            usecase.execute(userId, foldername, parentId);
        });

        verify(clientSession).abortTransaction();
        verify(clientSession, never()).commitTransaction();
    }

}
