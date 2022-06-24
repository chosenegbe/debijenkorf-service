package com.dailycodebuffer.cloudgateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class FallBackMethodControllers {

    @GetMapping("/userServiceFallBack")
    public String userServiceFallBackMethod () {
         return "User service is taking longer that expected. Please try again later";
    }

    @GetMapping("/departmentServiceFallBack")
    public String departmentServiceFallBackMethod () {
        return "Department service is taking longer that expected. Please try again later";
    }
}
