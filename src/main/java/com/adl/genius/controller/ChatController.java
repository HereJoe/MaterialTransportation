package com.adl.genius.controller;

import com.adl.genius.config.Constants;
import com.adl.genius.entity.Response;
import com.adl.genius.entity.UserContext;
import com.adl.genius.entity.vo.*;
import com.adl.genius.service.ChatService;
import com.adl.genius.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/dialogs")
    public Response<List<DialogVO>> dialogs(@RequestHeader(Constants.HTTP_HEADER_TOKEN) String token) {
        UserContext userContext = JWTUtil.parseToken(token);
        String redisToken = (String) redisTemplate.opsForValue().get(userContext.getRedisKey());
        if (!token.equals(redisToken)) {
            throw new RuntimeException("Invalid token.");
        }
        return Response.success(chatService.dialogs(userContext));
    }

    @GetMapping("/dialog/{id}")
    public Response<List<QAVO>> dialog(
            @RequestHeader(Constants.HTTP_HEADER_TOKEN) String token, @PathVariable("id") int dialogId) {
        UserContext userContext = JWTUtil.parseToken(token);
        String redisToken = (String) redisTemplate.opsForValue().get(userContext.getRedisKey());
        if (!token.equals(redisToken)) {
            throw new RuntimeException("Invalid token.");
        }
        return Response.success(chatService.dialog(userContext, dialogId));
    }

    @DeleteMapping("/dialog/{id}")
    public Response<Void> deleteDialog(
            @RequestHeader(Constants.HTTP_HEADER_TOKEN) String token, @PathVariable("id") int dialogId) {
        UserContext userContext = JWTUtil.parseToken(token);
        String redisToken = (String) redisTemplate.opsForValue().get(userContext.getRedisKey());
        if (!token.equals(redisToken)) {
            throw new RuntimeException("Invalid token.");
        }
        return Response.success(chatService.deleteDialog(userContext, dialogId));
    }

    @PostMapping("/dialog/title")
    public Response<Void> title(
            @RequestHeader(Constants.HTTP_HEADER_TOKEN) String token, @RequestBody DialogTitleVO dialogTitleVO) {
        if (!StringUtils.hasText(dialogTitleVO.getTitle())) {
            throw new RuntimeException("The title cannot be empty.");
        }
        UserContext userContext = JWTUtil.parseToken(token);
        String redisToken = (String) redisTemplate.opsForValue().get(userContext.getRedisKey());
        if (!token.equals(redisToken)) {
            throw new RuntimeException("Invalid token.");
        }
        return Response.success(chatService.title(userContext, dialogTitleVO));
    }

    @PostMapping()
    public Response<DialogOutputVO> chat(
            @RequestHeader(Constants.HTTP_HEADER_TOKEN) String token, @RequestBody DialogInputVO dialogInputVO) {
        if (!StringUtils.hasText(dialogInputVO.getUserInput())) {
            throw new RuntimeException("The input cannot be empty.");
        }
        UserContext userContext = JWTUtil.parseToken(token);
        String redisToken = (String) redisTemplate.opsForValue().get(userContext.getRedisKey());
        if (!token.equals(redisToken)) {
            throw new RuntimeException("Invalid token.");
        }
        return Response.success(chatService.chat(userContext, dialogInputVO));
    }
}
