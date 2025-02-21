package com.dxs.DriveProject.web.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping()
public class TestController {

    @GetMapping()
    private String getHello() {
        return "Hello";
    }

}
