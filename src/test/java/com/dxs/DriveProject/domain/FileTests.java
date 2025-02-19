package com.dxs.DriveProject.domain;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class FileTests {

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
}
