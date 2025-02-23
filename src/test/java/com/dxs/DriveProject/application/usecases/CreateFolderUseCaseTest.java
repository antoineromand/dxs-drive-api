package com.dxs.DriveProject.application.usecases;

import com.dxs.DriveProject.domain.exceptions.ParentFolderNotFoundException;
import com.dxs.DriveProject.infrastructure.entities.MongoFolderEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

public class CreateFolderUseCaseTest {
    private IStorageService storageService;
    private ICustomMongoFolderRepository folderRepository;
    private CreateFolderUseCase usecase;

    @BeforeEach
    void init() {
        this.storageService = Mockito.mock(IStorageService.class);
        this.folderRepository = Mockito.mock(ICustomMongoFolderRepository.class);
        this.usecase = new CreateFolderUseCase(this.storageService, this.folderRepository);
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
        String userId = null;
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

        Mockito.when(this.folderRepository.findByFolderIdAndUserId(parentId, userId)).thenReturn(Optional.empty());

        assertThrows(ParentFolderNotFoundException.class, () -> {
            this.usecase.execute(userId, foldername, parentId);
        });
    }

//    @Test
//    void shouldReturn() {
//        String userId = "xxx-xx1u";
//        String parentId = "xxx-xx1f";
//        String foldername = "test";
//        String expectedPath = "uploads/xxx-xx1u/xxx/xxx-xx1f";
//
//        Mockito.when(this.folderRepository.findByFolderIdAndUserId(parentId, userId)).thenReturn(Optional.of(any(MongoFolderEntity.class)));
//
//
//
//    }

}
