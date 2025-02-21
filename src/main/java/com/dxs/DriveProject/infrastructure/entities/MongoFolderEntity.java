package com.dxs.DriveProject.infrastructure.entities;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.dxs.DriveProject.domain.Folder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "folders")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MongoFolderEntity {

    @Id
    private String id;
    @Indexed
    @Field(name = "owner_id")
    private String ownerId;
    @Indexed
    @Field(name = "parent_id")
    private String parentId;
    @Indexed
    @Field(name = "foldername")
    private String foldername;
    @Field(name = "path")
    private String path;
    @Indexed
    @Field(name = "bookmark")
    private Boolean bookmark;
    @Field(name = "soft_delete")
    private Boolean softDelete;
    @Field(name = "created_at")
    private Date createdAt;

    public Folder toDomain() {
        return new Folder(this.id, this.ownerId, this.foldername, this.path, this.parentId, this.bookmark,
                this.softDelete, this.createdAt);
    }

    public static MongoFolderEntity fromDomain(Folder folder) {
        return MongoFolderEntity.builder()
                .id(folder.getId())
                .ownerId(folder.getOwnerId())
                .parentId(folder.getParentId())
                .path(folder.getPath())
                .bookmark(folder.isBookmarked())
                .createdAt(folder.getCreatedAt())
                .softDelete(folder.isSoftDeleted())
                .foldername(folder.getFoldername())
                .build();
    }
}
