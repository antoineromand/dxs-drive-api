package com.dxs.DriveProject.infrastructure.external.storage.files;

import java.nio.file.Path;

public interface FilesWrapper {
    boolean exists(Path path);
}
