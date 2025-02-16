package com.dxs.DriveProject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class TestController {
    @GetMapping()
    private String getHello() {
        return "Hello";
    }
}
