package com.dxs.DriveProject.infrastructure.external.storage.files;

import java.io.IOException;
import java.nio.file.Path;

public interface FilesWrapper {
    boolean exists(Path path);
    Path createDirectories(Path path) throws IOException;
}
