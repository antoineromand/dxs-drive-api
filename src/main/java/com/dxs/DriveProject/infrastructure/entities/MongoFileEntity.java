package com.dxs.DriveProject.infrastructure.entities;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.dxs.DriveProject.domain.File;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MongoFileEntity {
    @Id
    private String id;

    @Indexed
    @Field(name = "owner_id")
    private String ownerId;

    @Indexed
    @Field(name = "folder_id")
    private String folderId;

    @Field(name = "filename")
    private String filename;

    @Field(name = "path")
    private String path;

    @Indexed
    @Field(name = "bookmark")
    private Boolean bookmark;

    @Field(name = "size")
    private Long size;

    @Indexed
    @Field(name = "type")
    private String type;

    @Field(name = "soft_delete")
    private Boolean softDelete;

    @Field(name = "created_at")
    private Date createdAt;

    public File toDomain() {
        return new File(this.id, this.ownerId, this.folderId, this.filename, this.path, this.bookmark, this.size,
                this.type, this.softDelete, this.createdAt);
    }

    public static MongoFileEntity fromDomain(File file) {
        return MongoFileEntity.builder()
                .id(file.getId())
                .ownerId(file.getOwnerId())
                .folderId(file.getFolderId())
                .filename(file.getFilename())
                .path(file.getPath())
                .bookmark(file.isBookmarked())
                .size(file.getSize())
                .type(file.getType())
                .softDelete(file.isSoftDeleted())
                .createdAt(file.getCreatedAt())
                .build();
    }

}
