package com.dxs.DriveProject.domain.exceptions;

public class ParentFolderNotFoundException extends  RuntimeException{
    public ParentFolderNotFoundException(String parentId) {
        super("Parent folder with id :" + parentId + " not founded");
    }
}
