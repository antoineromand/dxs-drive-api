package com.dxs.DriveProject.application.usecases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.dxs.DriveProject.domain.File;
import com.dxs.DriveProject.domain.exceptions.AccessFolderUnauthorizedException;
import com.dxs.DriveProject.domain.exceptions.FolderNotFoundException;
import com.dxs.DriveProject.domain.object_values.FileType;
import com.dxs.DriveProject.infrastructure.entities.MongoFileEntity;
import com.dxs.DriveProject.infrastructure.external.storage.IStorageService;
import com.dxs.DriveProject.infrastructure.repositories.file.ICustomMongoFileRepository;
import com.dxs.DriveProject.infrastructure.repositories.folder.ICustomMongoFolderRepository;

public class UploadFileUseCase {
    private final IStorageService storageService;
    private final ICustomMongoFileRepository fileRepository;
    private final ICustomMongoFolderRepository folderRepository;

    public UploadFileUseCase(IStorageService storageService, ICustomMongoFileRepository fileRepository,
            ICustomMongoFolderRepository folderRepository) {
        this.storageService = storageService;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    public void execute(List<MultipartFile> files, String userId, String folderId) throws IOException {
        if (userId == null || files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        long maxSizeFile = 50L * 1024 * 1024;
        // Si folderId est précisé, vérifier si le fichier existe.
        if (folderId != null && !folderRepository.isExist(folderId)) {
            throw new FolderNotFoundException(folderId);
        }
        if (folderId != null && !folderRepository.isOwnedById(folderId, userId)) {
            throw new AccessFolderUnauthorizedException(folderId, userId);
        }

        ArrayList<MongoFileEntity> filesToInsert = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!FileType.isTypeValide(file.getContentType()) || file.getSize() < 0 || file.getSize() > maxSizeFile) {
                throw new IllegalArgumentException("Invalid file");
            }
            String path = storageService.writeFile(file, userId, folderId);

            File fileToDomain = new File(null, userId, folderId, file.getOriginalFilename(), path, false,
                    file.getSize(), file.getContentType(), false, new Date());

            MongoFileEntity fileToDB = MongoFileEntity.fromDomain(fileToDomain);

            filesToInsert.add(fileToDB);

        }
    }

}
