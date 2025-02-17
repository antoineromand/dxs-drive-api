package com.dxs.DriveProject.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@RequestMapping()
public class TestController {
    @GetMapping()
    private String getHello() {
        return "Hello";
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public List<String> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        List<String> fileDetails = new ArrayList<>();

        for (MultipartFile file : files) {
            fileDetails.add(file.getContentType());
        }
        return fileDetails;
    }

}
