package com.dxs.DriveProject.application.usecases;

import com.dxs.DriveProject.application.usecases.dto.UploadError;
import com.dxs.DriveProject.application.usecases.dto.UploadErrorType;
import com.dxs.DriveProject.application.usecases.dto.UploadResponse;
import com.dxs.DriveProject.domain.Folder;
import com.dxs.DriveProject.domain.exceptions.ParentFolderNotFoundException;
import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.dxs.DriveProject.web.controllers.dto.FolderDTO;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CreateFolderUseCaseTest {
    private static final String USER_ID = "user123";
    private static final String FOLDER_NAME = "TestFolder";
    private static final String PARENT_ID = "parentFolderId";
    private static final String GENERATED_FOLDER_ID = "generatedFolderId";
    private static final String EXPECTED_PATH = "/uploads/user123/folderId123";
    private static final String NON_EXISTING_PATH = "/non/existing/path";

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
        UploadResponse<FolderDTO> result = this.usecase.execute(null, null, null);
        List<UploadError> errors = result.getErrors();

        assertFalse(errors.isEmpty());
        assertEquals(UploadErrorType.INVALID_PARAMETER, errors.get(0).getErrorType());
    }

    @Test
    void shouldReturnErrorIfFoldernameIsNull() {
        UploadResponse<FolderDTO> result = this.usecase.execute(USER_ID, null, null);
        List<UploadError> errors = result.getErrors();

        assertFalse(errors.isEmpty());
        assertEquals(UploadErrorType.INVALID_PARAMETER, errors.get(0).getErrorType());
    }

    @Test
    void shouldReturnErrorsIfParentIdIsSpecifiedButNotFound() {

        when(this.folderRepository.findByFolderIdAndUserId(PARENT_ID, USER_ID)).thenReturn(Optional.empty());

        UploadResponse<FolderDTO> result = this.usecase.execute(USER_ID, FOLDER_NAME, PARENT_ID);
        List<UploadError> errors = result.getErrors();

        assertFalse(errors.isEmpty());
        assertEquals(UploadErrorType.FOLDER_NOT_FOUND, errors.get(0).getErrorType());
    }

    @Test
    public void shouldWriteSaveAndReturnFolder() throws IOException {

        when(mongoClient.startSession()).thenReturn(clientSession);

        Folder folderSansPath = new Folder(null, USER_ID, FOLDER_NAME, null, null, false, false, new Date());
        MongoFolderEntity entitySansPath = MongoFolderEntity.fromDomain(folderSansPath);
        entitySansPath.setId(GENERATED_FOLDER_ID);

        Folder folderAvecPath = entitySansPath.toDomain();
        folderAvecPath.setPath(EXPECTED_PATH);
        MongoFolderEntity entityAvecPath = MongoFolderEntity.fromDomain(folderAvecPath);


        when(folderRepository.save(any(MongoFolderEntity.class)))
                .thenReturn(entitySansPath)
                .thenReturn(entityAvecPath);

        when(storageService.writeFolder(eq(USER_ID), eq(GENERATED_FOLDER_ID), any()))
                .thenReturn(EXPECTED_PATH);

        UploadResponse<FolderDTO> result = usecase.execute(USER_ID, FOLDER_NAME, null);

        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(GENERATED_FOLDER_ID, result.getData().getId());
        assertTrue(result.getErrors().isEmpty());

        verify(clientSession).startTransaction();
        verify(clientSession).commitTransaction();
        verify(clientSession).close();

        ArgumentCaptor<MongoFolderEntity> captor = ArgumentCaptor.forClass(MongoFolderEntity.class);
        verify(folderRepository, times(2)).save(captor.capture());
        List<MongoFolderEntity> savedEntities = captor.getAllValues();

        MongoFolderEntity updatedEntity = savedEntities.get(1);
        assertEquals(EXPECTED_PATH, updatedEntity.getPath());
        assertEquals(GENERATED_FOLDER_ID, updatedEntity.getId());

    }

    @Test
    public void shouldThrowAnExceptionAndRollback() throws IOException {
       MongoFolderEntity parentFolder = MongoFolderEntity.builder()
                .id(PARENT_ID)
                .ownerId(USER_ID)
                .foldername("parentFolder")
                .path(EXPECTED_PATH)
                .bookmark(false)
                .softDelete(false)
                .createdAt(new Date())
                .build();
        when(folderRepository.findByFolderIdAndUserId(eq(PARENT_ID), eq(USER_ID)))
                .thenReturn(Optional.of(parentFolder));

        MongoFolderEntity insertedEntity = MongoFolderEntity.builder()
                .id(GENERATED_FOLDER_ID)
                .ownerId(USER_ID)
                .foldername(FOLDER_NAME)
                .path(null)
                .parentId(PARENT_ID)
                .bookmark(false)
                .softDelete(false)
                .createdAt(new Date())
                .build();
        when(folderRepository.save(any(MongoFolderEntity.class))).thenReturn(insertedEntity);

        when(storageService.writeFolder(eq(USER_ID), eq(GENERATED_FOLDER_ID), eq(EXPECTED_PATH)))
                .thenThrow(IOException.class);

        when(mongoClient.startSession()).thenReturn(clientSession);



        UploadResponse<FolderDTO> result = this.usecase.execute(USER_ID, FOLDER_NAME, PARENT_ID);
        List<UploadError> errors = result.getErrors();

        assertFalse(errors.isEmpty());
        assertEquals(UploadErrorType.FOLDER_WRITE_ERROR, errors.get(0).getErrorType());

        verify(clientSession).abortTransaction();
        verify(clientSession, never()).commitTransaction();
    }

    @Test
    public void shouldReturnErrorWhenFolderWithoutPathCannotBeSavedInDB() throws IOException {


        when(mongoClient.startSession()).thenReturn(clientSession);

        MongoFolderEntity parentFolder = MongoFolderEntity.builder()
                .id(PARENT_ID)
                .ownerId(USER_ID)
                .foldername("parentFolder")
                .path("/non/existing/path")
                .bookmark(false)
                .softDelete(false)
                .createdAt(new Date())
                .build();

        when(folderRepository.findByFolderIdAndUserId(eq(PARENT_ID), eq(USER_ID)))
                .thenReturn(Optional.of(parentFolder));

        when(folderRepository.save(any(MongoFolderEntity.class)))
                .thenThrow(new RuntimeException());


        UploadResponse<FolderDTO> result = usecase.execute(USER_ID, FOLDER_NAME, PARENT_ID);

        assertNull(result.getData());
        assertFalse(result.getErrors().isEmpty());
        assertEquals(UploadErrorType.DATABASE_ERROR, result.getErrors().get(0).getErrorType());

        verify(clientSession).startTransaction();
        verify(clientSession).abortTransaction();
        verify(clientSession).close();
    }

    @Test
    public void shouldReturnErrorWhenFolderWithPathCannotBeSavedInDB() throws IOException {
        when(mongoClient.startSession()).thenReturn(clientSession);

        MongoFolderEntity parentFolder = MongoFolderEntity.builder()
                .id("xxxfzefe")
                .ownerId(USER_ID)
                .foldername("parentFolder")
                .parentId(null)
                .path(null)
                .bookmark(false)
                .softDelete(false)
                .createdAt(new Date())
                .build();

        MongoFolderEntity folderWithoutPath = MongoFolderEntity.builder()
                .id("eeee")
                .ownerId(USER_ID)
                .foldername(FOLDER_NAME)
                .parentId("xxxfzefe")
                .path(null)
                .bookmark(false)
                .softDelete(false)
                .createdAt(new Date())
                .build();



        when(folderRepository.findByFolderIdAndUserId(eq(PARENT_ID), eq(USER_ID)))
                .thenReturn(Optional.of(parentFolder));

        when(folderRepository.save(any(MongoFolderEntity.class))).thenReturn(folderWithoutPath).thenThrow(new RuntimeException());

        when(storageService.writeFolder(eq(USER_ID), eq("eeee"), any())).thenReturn("/non/existing/path/eeee");

        UploadResponse<FolderDTO> result = usecase.execute(USER_ID, FOLDER_NAME, PARENT_ID);

        assertNull(result.getData());
        assertFalse(result.getErrors().isEmpty());
        assertEquals(UploadErrorType.DATABASE_ERROR, result.getErrors().get(0).getErrorType());

        verify(clientSession).startTransaction();
        verify(clientSession).abortTransaction();
        verify(clientSession).close();
    }

    @Test
    public void shouldReturnErrorWhenParentFolderNotFoundOnDisk() throws IOException {
        MongoFolderEntity parentFolder = MongoFolderEntity.builder()
                .id(PARENT_ID)
                .ownerId(USER_ID)
                .foldername("ParentFolder")
                .parentId(null)
                .path("/non/existing/path")
                .bookmark(false)
                .softDelete(false)
                .createdAt(new Date())
                .build();

        MongoFolderEntity newFolder = MongoFolderEntity.builder()
                .id("generatedFolderId")
                .ownerId(USER_ID)
                .foldername(FOLDER_NAME)
                .parentId(PARENT_ID)
                .path(null)
                .bookmark(false)
                .softDelete(false)
                .createdAt(new Date())
                .build();

        when(mongoClient.startSession()).thenReturn(clientSession);

        when(folderRepository.findByFolderIdAndUserId(eq(PARENT_ID), eq(USER_ID)))
                .thenReturn(Optional.of(parentFolder));

        when(folderRepository.save(any(MongoFolderEntity.class)))
                .thenReturn(newFolder);

        when(storageService.writeFolder(eq(USER_ID), eq("generatedFolderId"), eq("/non/existing/path")))
                .thenThrow(new NoSuchFileException("Parent folder could not be found!"));

        UploadResponse<FolderDTO> result = usecase.execute(USER_ID, FOLDER_NAME, PARENT_ID);
        List<UploadError> errors = result.getErrors();

        assertNull(result.getData());
        assertFalse(errors.isEmpty());
        assertEquals(UploadErrorType.FOLDER_NOT_FOUND, errors.get(0).getErrorType());
        assertEquals("folder:parent", errors.get(0).getTarget());

        verify(clientSession).startTransaction();
        verify(clientSession).abortTransaction();
        verify(clientSession).close();
    }



}
