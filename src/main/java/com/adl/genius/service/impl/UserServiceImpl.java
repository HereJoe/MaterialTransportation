package com.adl.genius.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.adl.genius.entity.UserContext;
import com.adl.genius.entity.po.User;
import com.adl.genius.entity.vo.LoginVO;
import com.adl.genius.entity.vo.RegisterVO;
import com.adl.genius.mapper.UserMapper;
import com.adl.genius.mapstruct.UserMapping;
import com.adl.genius.service.UserService;
import com.adl.genius.util.BCryptUtil;
import com.adl.genius.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Void register(RegisterVO registerVO) {
        User user = UserMapping.M.registerVO2User(registerVO);
        user.setPassword(BCryptUtil.encode(user.getPassword()));
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("The username already exists.", e);
        }
        return null;
    }

    @Override
    public String login(LoginVO loginVO) {
        Wrapper<User> wrapper = Wrappers.<User>lambdaQuery().eq(User::getUsername, loginVO.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("Incorrect user name or password.");
        }
        boolean match = BCryptUtil.match(loginVO.getPassword(), user.getPassword());
        if (!match) {
            throw new RuntimeException("Incorrect user name or password.");
        }

        UserContext userContext = UserMapping.M.user2UserContext(user);
        String token = JWTUtil.getToken(userContext);
        redisTemplate.opsForValue().set(userContext.getRedisKey(), token, 1, TimeUnit.DAYS);

        return token;
    }

    @Override
    public Void logout(UserContext userContext) {
        redisTemplate.delete(userContext.getRedisKey());
        return null;
    }
}
