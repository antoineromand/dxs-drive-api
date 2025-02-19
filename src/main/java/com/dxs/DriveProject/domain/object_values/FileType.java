package com.dxs.DriveProject.domain.object_values;

import java.util.Set;

public record FileType(String type) {

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
            "application/pdf", "image/png", "image/jpeg", "text/plain", "application/zip");

    public FileType {
        if (!ALLOWED_FILE_TYPES.contains(type)) {
            throw new IllegalArgumentException("Invalid file type: " + type);
        }
    }

    public String getType() {
        return type;
    }

    public static Boolean isTypeValide(String type) {
        return ALLOWED_FILE_TYPES.contains(type);
    }
}
