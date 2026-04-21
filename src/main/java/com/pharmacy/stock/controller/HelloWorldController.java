package com.pharmacy.stock.controller;

import com.pharmacy.stock.service.HelloWorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    @Autowired
    public HelloWorldController (HelloWorldService helloWorldService)
    {
        this.helloWorldService= helloWorldService;
    }

    @GetMapping("/hello")
    public String sayhello(){
        return helloWorldService.sayHello();
    }
}
