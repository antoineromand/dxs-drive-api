package com.dxs.DriveProject.infrastructure.external.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

import com.dxs.DriveProject.infrastructure.external.storage.files.FilesWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.springframework.web.multipart.MultipartFile;

public class LocalStorageServiceTest {
    private MultipartFile file;
    private LocalStorageService localStorageService;
    private FilesWrapper filesWrapper;


    @BeforeEach
    void setUp() {
        filesWrapper = Mockito.mock(FilesWrapper.class);
        localStorageService = new LocalStorageService(filesWrapper);
        file = Mockito.mock(MultipartFile.class);
    }

    @Test
    void testWriteFile_ShouldReturnCorrectPath() throws IOException {
        String userId = "user123";
        String folderId = "folder456";
        String fileName = "test.txt";
        Path parentPath = Path.of("uploads", userId, folderId);
        Path expectedPath = Path.of(parentPath.toString(), fileName);

        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(getClass().getClassLoader().getResourceAsStream("test-file.txt"));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any())).then(invocation -> null);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(0L);
            String result = localStorageService.writeFile(file, userId, parentPath.toString());

            assertEquals(expectedPath.toString(), result);
        }
    }

    @Test
    void testWriteFileWithoutFolderId_ShouldReturnCorrectPath() throws IOException {
        String userId = "user123";
        String fileName = "test.txt";
        Path expectedPath = Path.of("uploads", userId, fileName);

        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(getClass().getClassLoader().getResourceAsStream("test-file.txt"));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any())).then(invocation -> null);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(0L);
            String result = localStorageService.writeFile(file, userId, null);

            assertEquals(expectedPath.toString(), result);
        }
    }

    @Test
    void testWriteFile_ShouldThrowException_WhenFileIsNull() {
        String userId = "user123";
        String folderId = "folder456";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            localStorageService.writeFile(null, userId, folderId);
        });

        assertEquals("File not found !", exception.getMessage());
    }

    @Test
    void testWriteFile_ShouldThrowException_WhenUserIdIsNull() {
        String folderId = "folder456";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            localStorageService.writeFile(file, null, folderId);
        });

        assertEquals("User not provided !", exception.getMessage());
    }

    @Test
    void testWriteFile_ShouldThrowException_WhenUserIdIsEmpty() {
        String folderId = "folder456";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            localStorageService.writeFile(file, "", folderId);
        });

        assertEquals("User not provided !", exception.getMessage());
    }

    @Test
    void testWriteFolder_ShouldThrowExceptionIfUserIdIsNull() {
        String userId = null;
        String folderId = "xxx-xx1";

        assertThrows(IllegalArgumentException.class, () -> {
            localStorageService.writeFolder(userId, folderId, null);
        });
    }

    @Test
    void testWriteFolder_ShouldThrowExceptionIfFolderIdIsNull() {
        String userId = "xxx-xx1";
        String folderId = null;

        assertThrows(IllegalArgumentException.class, () -> {
            localStorageService.writeFolder(userId, folderId, null);
        });
    }

    @Test
    void testWriterFolder_ShouldThrowExceptionIfParentNotExist() {
        String userId = "xxx-xx1";
        String folderId = "xxx-xx1";
        String parentPath = "xxx/xxx/xxx";
        when(filesWrapper.exists(Path.of(parentPath))).thenReturn(false);

        assertThrows(NoSuchFileException.class, () -> {
            localStorageService.writeFolder(userId, folderId, parentPath);
        });
    }

    @Test
    void testWriterFolder_ShouldThrowExceptionIfFolderAlreadyExists() {
        String userId = "xxx-xx1";
        String folderId = "xx3";
        String parentPath = "xxx/xxx/xx2";
        when(filesWrapper.exists(Path.of(parentPath))).thenReturn( true);
        when(filesWrapper.exists(Path.of(parentPath, folderId))).thenReturn( true);
        assertThrows(FileAlreadyExistsException.class, () -> {
            localStorageService.writeFolder(userId, folderId, parentPath);
        });
    }


    @Test
    void testWriteFolder_ShouldReturnPathWithParent() throws IOException {
        String userId = "xxx-xx1";
        String folderId = "xx3";
        String parentPath = "xxx/xxx/xx2";
        Path expectedPath = Path.of(parentPath, folderId);
        when(filesWrapper.exists(Path.of(parentPath))).thenReturn( true);
        when(filesWrapper.exists(Path.of(parentPath, folderId))).thenReturn( false);
        when(filesWrapper.createDirectories(Path.of(parentPath, folderId))).thenReturn(expectedPath);
        String path = localStorageService.writeFolder(userId, folderId, parentPath);
        assertEquals(expectedPath.toString(), path);
        assertFalse(path.isEmpty());
    }

    @Test
    void testWriteFolder_ShouldReturnPathWithoutParent() throws IOException {
        String userId = "xxx-xx1";
        String folderId = "xx3";
        Path expectedPath = Path.of("uploads", userId, folderId);
        when(filesWrapper.exists(Path.of("uploads", userId, folderId))).thenReturn( false);
        when(filesWrapper.createDirectories(Path.of("uploads", userId, folderId))).thenReturn(expectedPath);
        String path = localStorageService.writeFolder(userId, folderId, null);
        assertEquals(expectedPath.toString(), path);
        assertFalse(path.isEmpty());
    }




}
