package com.dxs.DriveProject.domain;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dxs.DriveProject.config.AbstractMongoDBTest;
import org.junit.jupiter.api.Test;

class FileTest {

    @Test
    void shouldRenameFile() {
        File file = new File("1", "xx", "123", "document.txt", "/files/document.txt", false, 500L,
                "text/plain", false, new Date());

        file.renameFile("new-name.txt");

        assertEquals("new-name.txt", file.getFilename());
    }

    @Test
    void shouldMoveFileToNewFolder() {
        File file = new File("1",
                "xx", "123", "document.txt", "/files/document.txt", false, 500L,
                "text/plain", false, new Date());

        file.moveToFolder("xxxxx-xxxx");

        assertEquals("xxxxx-xxxx", file.getFolderId());
    }

    @Test
    void shouldMarkFileAsDeleted() {
        File file = new File("1",
                "xx", "123", "document.txt", "/files/document.txt", false, 500L,
                "text/plain", false, new Date());

        file.markAsDeleted();

        assertTrue(file.isSoftDeleted());
    }

    @Test
    void shouldToggleBookmark() {
        File file = new File("1",
                "xx", "123", "document.txt", "/files/document.txt", false, 500L,
                "text/plain", false, new Date());

        file.toggleBookmark();

        assertTrue(file.isBookmarked());

        file.toggleBookmark();

        assertFalse(file.isBookmarked());
    }

    @Test
    void shouldThrowExceptionIfOwnerIdIsEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new File(null, "", "folder-123", "test.pdf", "/files/test.pdf", false, 500L, "application/pdf", false,
                    new Date());
        });

        assertEquals("Owner ID is required and cannot be empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfFilenameIsEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new File(null, "owner-123", "folder-123", "", "/files/test.pdf", false, 500L, "application/pdf", false,
                    new Date());
        });

        assertEquals("Filename is required and cannot be empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfSizeIsNegative() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new File(null, "owner-123", "folder-123", "test.pdf", "/files/test.pdf", false, -100L, "application/pdf",
                    false, new Date());
        });

        assertEquals("Size must be greater than 0", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfFileTypeIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new File(null, "owner-123", "folder-123", "test.pdf", "/files/test.pdf", false, 500L, "application/exe",
                    false, new Date());
        });

        assertEquals("Invalid file type: application/exe", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfPathIsEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new File(null, "owner-123", "folder-123", "test.pdf", "", false, -100L, "application/pdf",
                    false, new Date());
        });

        assertEquals("Path is required and cannot be empty", exception.getMessage());
    }
}
