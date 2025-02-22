package com.dxs.DriveProject.infrastructure.external.storage;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    String writeFile(MultipartFile file, String userId, String folderId) throws IOException;
    String writeFolder(String userId, String folderId, String parentPath) throws IOException;
}
