package com.dxs.DriveProject.infrastructure.external.storage.files;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FilesWrapperImpl implements FilesWrapper {

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }
}
