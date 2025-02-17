package com.dxs.DriveProject.infrastructure.entities;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    private UUID ownerId;

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

        
}
