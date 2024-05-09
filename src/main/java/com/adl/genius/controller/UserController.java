package com.adl.genius.controller;

import com.adl.genius.config.Constants;
import com.adl.genius.entity.Response;
import com.adl.genius.entity.UserContext;
import com.adl.genius.entity.vo.LoginVO;
import com.adl.genius.entity.vo.RegisterVO;
import com.adl.genius.service.UserService;
import com.adl.genius.util.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public Response<Void> register(@RequestBody RegisterVO registerVO) {
        if (!StringUtils.hasText(registerVO.getUsername())) {
            throw new RuntimeException("The username cannot be empty.");
        }
        if (!StringUtils.hasText(registerVO.getPassword())) {
            throw new RuntimeException("The password cannot be empty.");
        }
        return Response.success(userService.register(registerVO));
    }

    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginVO loginVO) {
        if (!StringUtils.hasText(loginVO.getUsername())) {
            throw new RuntimeException("The username cannot be empty.");
        }
        if (!StringUtils.hasText(loginVO.getPassword())) {
            throw new RuntimeException("The password cannot be empty.");
        }
        return Response.success(userService.login(loginVO));
    }

    @PostMapping("/logout")
    public Response<Void> logout(@RequestHeader(Constants.HTTP_HEADER_TOKEN) String token) {
        UserContext userContext = JWTUtil.parseToken(token);
        String redisToken = (String) redisTemplate.opsForValue().get(userContext.getRedisKey());
        if (!token.equals(redisToken)) {
            throw new RuntimeException("Invalid token.");
        }
        return Response.success(userService.logout(userContext));
    }
}
