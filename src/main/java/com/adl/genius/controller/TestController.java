package com.adl.genius.controller;

import com.adl.genius.entity.Response;
import com.adl.genius.util.BCryptUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/exception")
    public Response<String> exception() {
        throw new RuntimeException("test exception");
    }

    @GetMapping("/datetime")
    public Response<LocalDateTime> datetime() {
        return Response.success(LocalDateTime.now());
    }

    @GetMapping("/bcrypt")
    public Response<Boolean> bcrypt() {
        String raw = "123456";
        String encoded = BCryptUtil.encode(raw);
        return Response.success(BCryptUtil.match(raw, encoded));
    }
}
