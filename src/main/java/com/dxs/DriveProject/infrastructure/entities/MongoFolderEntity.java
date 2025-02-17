package com.dxs.DriveProject.infrastructure.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
}
