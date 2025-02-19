package com.dxs.DriveProject.application.usecases;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.file.ICustomMongoFileRepository;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;

public class UploadFileTest {

    private IStorageService storageService;
    private ICustomMongoFileRepository fileRepository;
    private ICustomMongoFolderRepository folderRepository;
    private UploadFileUseCase uploadFileUseCase;

    private static int invalidSize = 51 * 1024 * 1024;
    private static int validSize = 3 * 1240 * 1024;

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
                Arguments.of(List.of(new MockMultipartFile("test", "test.txt", "text/plain", new byte[validSize])),
                        null, "t"),
                Arguments.of(null, "xxx-xxxx", null),
                Arguments.of(List.of(), "xxx-xxxx", null));

    }

    @ParameterizedTest
    @MethodSource("provideInvalidFile")
    void shouldThrowAnIllegalArgumentExceptionWhithInvalidFileSizeAndType(List<MultipartFile> files, String userId,
            String folderId) throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            this.uploadFileUseCase.execute(files, userId, folderId);
        });
    }

    private static List<Arguments> provideInvalidFile() {
        return List.of(
                Arguments.of(List.of(new MockMultipartFile("test", "test.txt", "invalid", new byte[validSize])),
                        "xxx-xxx", null),
                Arguments.of(List.of(new MockMultipartFile("test", "test.txt", "text/plain", new byte[invalidSize])),
                        "xxxx-xxx", null));
    }

}
