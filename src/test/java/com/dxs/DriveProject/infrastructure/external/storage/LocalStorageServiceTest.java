package com.dxs.DriveProject.infrastructure.external.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

import com.dxs.DriveProject.infrastructure.external.storage.files.FilesWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

public class LocalStorageServiceTest {
    private MultipartFile file;
    private LocalStorageService localStorageService;
    private FilesWrapper filesWrapper;


    @BeforeEach
    void setUp() {
        filesWrapper = mock(FilesWrapper.class);
        localStorageService = new LocalStorageService(filesWrapper);
        file = mock(MultipartFile.class);
    }

    @Test
    void testWriteFile_ShouldReturnCorrectPath(@TempDir Path tempDir) throws IOException {
        String userId = "user123";
        String folderId = "folder456";
        String fileName = "test.txt";
        Path parentPath = tempDir.resolve("uploads").resolve(userId).resolve(folderId);

        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(mock(InputStream.class));

        String result = localStorageService.writeFile(file, userId, parentPath.toString());

        assertTrue(result.startsWith(parentPath.toString()));
        assertTrue(result.endsWith(".txt"));

        String fileNameInResult = Path.of(result).getFileName().toString();
        String uuidPart = fileNameInResult.substring(0, fileNameInResult.lastIndexOf('.'));
        assertDoesNotThrow(() -> UUID.fromString(uuidPart));
    }

    @Test
    void testWriteFileWithoutFolderId_ShouldReturnCorrectPath(@TempDir Path tempDir) throws IOException {
        String userId = "user123";
        String fileName = "test.txt";
        Path parentPath = tempDir.resolve("uploads").resolve(userId);

        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(mock(InputStream.class));

        String result = localStorageService.writeFile(file, userId, tempDir.resolve("uploads").toString());

        assertTrue(result.endsWith(".txt"));

        String fileNameInResult = Path.of(result).getFileName().toString();
        String uuidPart = fileNameInResult.substring(0, fileNameInResult.lastIndexOf('.'));
        assertDoesNotThrow(() -> UUID.fromString(uuidPart));
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
        String parentPath = "uploads/xxx/xxx/xx2";

        String expectedRelativePath = parentPath + "/" + folderId;

        when(filesWrapper.exists(Path.of(parentPath))).thenReturn(true);
        when(filesWrapper.exists(Path.of(parentPath, folderId))).thenReturn(false);
        when(filesWrapper.createDirectories(Path.of(parentPath, folderId)))
                .thenReturn(Path.of(expectedRelativePath));

        String actualPath = localStorageService.writeFolder(userId, folderId, parentPath);

        assertEquals(expectedRelativePath, actualPath);
        assertFalse(actualPath.isEmpty());
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
