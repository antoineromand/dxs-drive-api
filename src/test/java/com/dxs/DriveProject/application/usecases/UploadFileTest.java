package com.dxs.DriveProject.application.usecases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
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
import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.domain.File;
import com.dxs.DriveProject.domain.exceptions.AccessFolderUnauthorizedException;
import com.dxs.DriveProject.domain.exceptions.FolderNotFoundException;
import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.file.ICustomMongoFileRepository;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;

public class UploadFileTest {

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
    void shouldThrowAnIllegalArgumentExceptionWhithInvalidParameters(List<MultipartFile> files, String userId,
            String folderId) throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            this.uploadFileUseCase.execute(files, userId, folderId);
        });
    }

    private static List<Arguments> provideInvalidInputs() {
        return List.of(
                Arguments.of(List.of(new MockMultipartFile("test", "test.txt", "text/plain", new byte[VALID_SIZE])),
                        null, "t"),
                Arguments.of(null, "xxx-xxxx", null),
                Arguments.of(List.of(), "xxx-xxxx", null));

    }

    @Test
    void shouldThrowAnExceptionIfFolderIsSpecifiedButDoesNotExist() throws IOException {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", new byte[10]);

        String userId = "xxx-xx1";
        String folderId = "xxx-xx2";

        when(folderRepository.isExist(eq(folderId))).thenReturn(false);

        assertThrows(FolderNotFoundException.class, () -> {
            this.uploadFileUseCase.execute(List.of(validFile), userId, folderId);
        });

    }

    @Test
    void shouldThrowAnExceptionIfFolderIsSpecifiedButDoesNotOwnIt() {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", new byte[10]);

        String userId = "xxx-xx1";
        String folderId = "xxx-xx2";
        when(folderRepository.isExist(folderId)).thenReturn(true);
        when(folderRepository.isOwnedById(folderId, userId)).thenReturn(false);

        assertThrows(AccessFolderUnauthorizedException.class, () -> {
            this.uploadFileUseCase.execute(List.of(validFile), userId, folderId);
        });

    }

    @ParameterizedTest
    @MethodSource("provideInvalidFile")
    void shouldThrowAnIllegalArgumentExceptionWhithInvalidFileSizeAndType(List<MultipartFile> files, String userId,
            String folderId) throws IOException {
        if (folderId != null) {
            when(folderRepository.isExist(folderId)).thenReturn(true);
            when(folderRepository.isOwnedById(folderId, userId)).thenReturn(true);
        }
        assertThrows(IllegalArgumentException.class, () -> {
            this.uploadFileUseCase.execute(files, userId, folderId);
        });

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
    void shouldWriteFileWihtoutFolder() throws IOException {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", new byte[10]);

        String expectedPath = "/uploads/user123/image.jpg";

        when(storageService.writeFile(any(MultipartFile.class), any(String.class), isNull()))
                .thenReturn(expectedPath);

        uploadFileUseCase.execute(List.of(validFile), "user123", null);

        verify(storageService, times(1)).writeFile(validFile, "user123", null);

        assertEquals(expectedPath, storageService.writeFile(validFile, "user123", null));

    }

    @Test
    void shouldWriteFileWithFolder() throws IOException {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", new byte[10]);

        String expectedPath = "/uploads/user123/123/image.jpg";

        when(folderRepository.isExist("123")).thenReturn(true);

        when(folderRepository.isOwnedById("123", "user123")).thenReturn(true);

        when(storageService.writeFile(any(MultipartFile.class), any(String.class), any(String.class)))
                .thenReturn(expectedPath);

        uploadFileUseCase.execute(List.of(validFile), "user123", "123");

        verify(storageService, times(1)).writeFile(validFile, "user123", "123");

        assertEquals(expectedPath, storageService.writeFile(validFile, "user123", "123"));

    }

    @Test
    void shouldCreateFileObjectWithCorrectValues() throws IOException {
        String userId = "user123";
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", new byte[10]);

        String expectedPath = "/uploads/user123/image.jpg";

        when(storageService.writeFile(any(MultipartFile.class), any(String.class), isNull()))
                .thenReturn(expectedPath);

        try (MockedStatic<MongoFileEntity> mockedStatic = mockStatic(MongoFileEntity.class)) {
            MongoFileEntity mockEntity = mock(MongoFileEntity.class);

            mockedStatic.when(() -> MongoFileEntity.fromDomain(any(File.class)))
                    .thenReturn(mockEntity);

            uploadFileUseCase.execute(List.of(validFile), userId, null);

            ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
            mockedStatic.verify(() -> MongoFileEntity.fromDomain(fileCaptor.capture()), times(1));

            File capturedFile = fileCaptor.getValue();

            assertEquals(userId, capturedFile.getOwnerId());
            assertEquals("image.jpg", capturedFile.getFilename());
            assertEquals(expectedPath, capturedFile.getPath());
            assertNull(capturedFile.getFolderId());

        }
    }

    @Test
    void shouldInsertFileInDB() throws IOException {

        String userId = "user123";
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", new byte[10]);

        String expectedPath = "/uploads/user123/image.jpg";

        when(storageService.writeFile(any(MultipartFile.class), any(String.class), isNull()))
                .thenReturn(expectedPath);

        try (MockedStatic<MongoFileEntity> mockedStatic = mockStatic(MongoFileEntity.class)) {
            MongoFileEntity mockEntity = mock(MongoFileEntity.class);

            mockedStatic.when(() -> MongoFileEntity.fromDomain(any(File.class)))
                    .thenReturn(mockEntity);

            uploadFileUseCase.execute(List.of(validFile), userId, null);

            ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
            mockedStatic.verify(() -> MongoFileEntity.fromDomain(fileCaptor.capture()), times(1));

            File capturedFile = fileCaptor.getValue();

            assertEquals(userId, capturedFile.getOwnerId());
            assertEquals("image.jpg", capturedFile.getFilename());
            assertEquals(expectedPath, capturedFile.getPath());
            assertNull(capturedFile.getFolderId());

        }

    }

    @Test
    void shouldInsertManyFilesWhenAllFilesAreValid() throws IOException {
        // Arrange: Prépare des fichiers valides
        String userId = "user123";
        MockMultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", new byte[10]);
        MockMultipartFile file2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", new byte[10]);

        List<MultipartFile> files = List.of(file1, file2);
        String expectedPath1 = "/uploads/user123/image1.jpg";
        String expectedPath2 = "/uploads/user123/image2.jpg";

        // Simule l'écriture des fichiers sur le storage
        when(storageService.writeFile(eq(file1), eq(userId), isNull())).thenReturn(expectedPath1);
        when(storageService.writeFile(eq(file2), eq(userId), isNull())).thenReturn(expectedPath2);

        // Mocke MongoFileEntity.fromDomain()
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
        MockMultipartFile invalidFile = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[10]);

        List<MultipartFile> files = List.of(invalidFile);

        assertThrows(IllegalArgumentException.class, () -> {
            uploadFileUseCase.execute(files, userId, null);
        });

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

            ArrayList<File> returnedFiles = uploadFileUseCase.execute(files, userId, null);

            verify(mongoFile1, times(1)).toDomain();
            verify(mongoFile2, times(1)).toDomain();

            assertEquals(2, returnedFiles.size());
            assertEquals("image1.jpg", returnedFiles.get(0).getFilename());
            assertEquals(expectedPath1, returnedFiles.get(0).getPath());
            assertEquals("image2.jpg", returnedFiles.get(1).getFilename());
            assertEquals(expectedPath2, returnedFiles.get(1).getPath());
        }
    }

}
