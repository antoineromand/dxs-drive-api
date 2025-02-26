package com.dxs.DriveProject.domain;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class FolderTest {

    @Test
    void shouldRenameFolder() {
        Folder folder = new Folder("e", "e", "folder", "/uploads/xcvzrr", null, false, false, new Date());

        folder.renameFolder("yes");

        assertEquals("yes", folder.getFoldername());

    }

    @Test
    void shouldMarkFolderAsDeleted() {
        Folder folder = new Folder("e", "e", "folder", "/uploads/xcvzrr", null, false, false, new Date());

        folder.markAsDeleted();

        assertTrue(folder.isSoftDeleted());
    }

    @Test
    void shouldMoveFolderIntoNewFolder() {
        Folder folder = new Folder("e", "e", "folder", "/uploads/xcvzrr", null, false, false, new Date());

        folder.moveToFolder("xxxxx-xxxx");

        assertEquals("xxxxx-xxxx", folder.getParentId());
    }

    @Test
    void shouldToggleBookmark() {
        Folder folder = new Folder("e", "e", "folder", "/uploads/xcvzrr", null, false, false, new Date());

        folder.toggleBookmark();

        assertTrue(folder.isBookmarked());

        folder.toggleBookmark();

        assertFalse(folder.isBookmarked());
    }

    @Test
    void shouldThrowExceptionIfOwnerIdIsEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Folder("e", "", "folder", "/uploads/xcvzrr", null, false, false, new Date());
        });

        assertEquals("Owner ID is required and cannot be empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfFoldernameIsEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Folder("e", "e", "", "/uploads/xcvzrr", null, false, false, new Date());
        });

        assertEquals("Foldername is required and cannot be empty", exception.getMessage());
    }

}
