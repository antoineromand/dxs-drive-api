package com.dxs.DriveProject.application.usecases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dxs.DriveProject.application.usecases.dto.UploadError;
import com.dxs.DriveProject.application.usecases.dto.UploadErrorType;
import com.dxs.DriveProject.application.usecases.dto.UploadResponse;
import com.dxs.DriveProject.web.controllers.dto.FileDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.domain.File;
import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.file.ICustomMongoFileRepository;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;

@Transactional
public class UploadFileUseCaseTest {

    private IStorageService storageService;
    private ICustomMongoFileRepository fileRepository;
    private ICustomMongoFolderRepository folderRepository;
    private UploadFileUseCase uploadFileUseCase;

    private final static int INVALID_SIZE = 51 * 1024 * 1024;
    private final static int VALID_SIZE = 3 * 1240 * 1024;

    @BeforeEach
    public void init() {
        this.storageService = Mockito.mock(IStorageService.class);
        this.fileRepository = Mockito.mock(ICustomMongoFileRepository.class);
        this.folderRepository = Mockito.mock(ICustomMongoFolderRepository.class);
        this.uploadFileUseCase = new UploadFileUseCase(this.storageService, this.fileRepository, this.folderRepository);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidInputs")
    void shouldContainsErrorIfParametersAreNotValid(List<MultipartFile> files, String userId,
            String folderId) throws IOException {
        UploadResponse<List< FileDTO>> response = this.uploadFileUseCase.execute(files, userId, folderId);
        List<UploadError> errors = response.getErrors();
        assertFalse(errors.isEmpty());
        assertEquals(UploadErrorType.INVALID_PARAMETER, errors.get(0).getErrorType());
    }

    private static List<Arguments> provideInvalidInputs() {
        return List.of(
                Arguments.of(List.of(new MockMultipartFile("test", "test.txt", "text/plain", new byte[VALID_SIZE])),
                        null, "t"),
                Arguments.of(null, "xxx-xxxx", null),
                Arguments.of(List.of(), "xxx-xxxx", null));

    }

    @Test
    void shouldContainsErrorIfFolderIdIsSpecifiedAndNotFound() throws IOException {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", new byte[10]);

        String userId = "xxx-xx1";
        String folderId = "xxx-xx2";

        when(folderRepository.isExist(eq(folderId))).thenReturn(false);

        UploadResponse<List< FileDTO>> response = this.uploadFileUseCase.execute(List.of(validFile), userId, folderId);
        List<UploadError> errors = response.getErrors();

        assertFalse(errors.isEmpty());
        assertEquals(UploadErrorType.FOLDER_NOT_FOUND, errors.get(0).getErrorType());

    }

    @Test
    void shouldContainsErrorIfFolderIsSpecifiedButDoesNotOwnIt() throws IOException {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", new byte[10]);

        String userId = "xxx-xx1";
        String folderId = "xxx-xx2";
        when(folderRepository.isExist(folderId)).thenReturn(true);
        when(folderRepository.isOwnedById(folderId, userId)).thenReturn(false);

        UploadResponse<List< FileDTO>> response = this.uploadFileUseCase.execute(List.of(validFile), userId, folderId);
        List<UploadError> errors = response.getErrors();

        assertFalse(errors.isEmpty());
        assertEquals(UploadErrorType.FORBIDDEN_ACCESS, errors.get(0).getErrorType());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidFile")
    void shouldContainsErrorIfInvalidFileSizeAndType(List<MultipartFile> files, String userId, String folderId) throws IOException {
        if (folderId != null) {
            when(folderRepository.isExist(folderId)).thenReturn(true);
            when(folderRepository.isOwnedById(folderId, userId)).thenReturn(true);
        }

        UploadResponse<List<FileDTO>> response = uploadFileUseCase.execute(files, userId, folderId);
        List<UploadError> errors = response.getErrors();

        assertEquals(UploadErrorType.FILE_VALIDATION_ERROR, errors.get(0).getErrorType());

        if (folderId != null) {
            verify(folderRepository, times(1)).isExist(folderId);
            verify(folderRepository, times(1)).isOwnedById(folderId, userId);
        } else {
            verify(folderRepository, never()).isExist(any());
            verify(folderRepository, never()).isOwnedById(any(), any());
        }
    }



    private static List<Arguments> provideInvalidFile() {
        return List.of(
                Arguments.of(List.of(new MockMultipartFile("test", "test.txt", "invalid", new byte[VALID_SIZE])),
                        "xxx-xxx", null),
                Arguments.of(List.of(new MockMultipartFile("test", "test.txt", "text/plain", new byte[INVALID_SIZE])),
                        "xxxx-xxx", null),
                Arguments.of(List.of(new MockMultipartFile("test", "test.txt", "text/plain", new byte[INVALID_SIZE])),
                        "xxxx-xxx", "xxx"));

    }

    @Test
    void shouldInsertManyFilesWhenAllFilesAreValid() throws IOException {
        String userId = "user123";
        MockMultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", new byte[10]);
        MockMultipartFile file2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", new byte[10]);

        List<MultipartFile> files = List.of(file1, file2);
        String expectedPath1 = "/uploads/user123/image1.jpg";
        String expectedPath2 = "/uploads/user123/image2.jpg";

        when(storageService.writeFile(eq(file1), eq(userId), isNull())).thenReturn(expectedPath1);
        when(storageService.writeFile(eq(file2), eq(userId), isNull())).thenReturn(expectedPath2);

        try (MockedStatic<MongoFileEntity> mockedStatic = mockStatic(MongoFileEntity.class)) {
            MongoFileEntity mongoFile1 = mock(MongoFileEntity.class);
            MongoFileEntity mongoFile2 = mock(MongoFileEntity.class);

            mockedStatic.when(() -> MongoFileEntity.fromDomain(any(File.class)))
                    .thenReturn(mongoFile1, mongoFile2);

            uploadFileUseCase.execute(files, userId, null);

            ArgumentCaptor<ArrayList<MongoFileEntity>> listCaptor = ArgumentCaptor.forClass(ArrayList.class);
            verify(fileRepository, times(1)).insertMany(listCaptor.capture());

            ArrayList<MongoFileEntity> capturedList = listCaptor.getValue();

            assertEquals(2, capturedList.size());
            assertTrue(capturedList.contains(mongoFile1));
            assertTrue(capturedList.contains(mongoFile2));
        }
    }

    @Test
    void shouldNotInsertFilesWhenNoValidFiles() throws IOException {
        String userId = "user123";
        MockMultipartFile invalidFile = new MockMultipartFile("file", "document.pdf", "te/pdf", new byte[10]);

        List<MultipartFile> files = List.of(invalidFile);

        UploadResponse<List<FileDTO>> response = uploadFileUseCase.execute(files, userId, null);
        List<UploadError> errors = response.getErrors();

        assertFalse(errors.isEmpty());
        assertEquals(UploadErrorType.FILE_VALIDATION_ERROR, errors.get(0).getErrorType());

        verify(fileRepository, never()).insertMany(any());
    }

    @Test
    void shouldReturnFilesWhenInsertManyIsSuccessful() throws IOException {
        String userId = "user123";
        MockMultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", new byte[10]);
        MockMultipartFile file2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", new byte[10]);

        List<MultipartFile> files = List.of(file1, file2);
        String expectedPath1 = "/uploads/user123/image1.jpg";
        String expectedPath2 = "/uploads/user123/image2.jpg";

        when(storageService.writeFile(eq(file1), eq(userId), isNull())).thenReturn(expectedPath1);
        when(storageService.writeFile(eq(file2), eq(userId), isNull())).thenReturn(expectedPath2);

        MongoFileEntity mongoFile1 = mock(MongoFileEntity.class);
        MongoFileEntity mongoFile2 = mock(MongoFileEntity.class);
        when(mongoFile1.toDomain()).thenReturn(new File(null, userId, null, "image1.jpg", expectedPath1, false,
                file1.getSize(), file1.getContentType(), false, new Date()));
        when(mongoFile2.toDomain()).thenReturn(new File(null, userId, null, "image2.jpg", expectedPath2, false,
                file2.getSize(), file2.getContentType(), false, new Date()));

        try (MockedStatic<MongoFileEntity> mockedStatic = mockStatic(MongoFileEntity.class)) {
            mockedStatic.when(() -> MongoFileEntity.fromDomain(any(File.class)))
                    .thenReturn(mongoFile1, mongoFile2);

            ArrayList<MongoFileEntity> insertedEntities = new ArrayList<>();
            insertedEntities.add(mongoFile1);
            insertedEntities.add(mongoFile2);
            when(fileRepository.insertMany(any())).thenReturn(insertedEntities);

            UploadResponse<List<FileDTO>> response = uploadFileUseCase.execute(files, userId, null);
            List<FileDTO> returnedFiles = response.getData();

            verify(mongoFile1, times(1)).toDomain();
            verify(mongoFile2, times(1)).toDomain();

            assertEquals(2, returnedFiles.size());
        }
    }

}
