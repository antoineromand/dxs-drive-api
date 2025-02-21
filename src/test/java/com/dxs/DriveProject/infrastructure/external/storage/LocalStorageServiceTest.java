package com.dxs.DriveProject.infrastructure.external.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.springframework.web.multipart.MultipartFile;

public class LocalStorageServiceTest {
    private MultipartFile file;
    private LocalStorageService localStorageService;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        file = Mockito.mock(MultipartFile.class);
    }

    @Test
    void testWriteFile_ShouldReturnCorrectPath() throws IOException {
        String userId = "user123";
        String folderId = "folder456";
        String fileName = "test.txt";
        Path expectedPath = Path.of("uploads", folderId, userId, fileName);

        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(getClass().getClassLoader().getResourceAsStream("test-file.txt"));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any())).then(invocation -> null);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(0L);
            String result = localStorageService.writeFile(file, userId, folderId);

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
}
