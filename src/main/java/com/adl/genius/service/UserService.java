package com.adl.genius.service;

import com.adl.genius.entity.UserContext;
import com.adl.genius.entity.vo.LoginVO;
import com.adl.genius.entity.vo.RegisterVO;

public interface UserService {

    Void register(RegisterVO registerVO);

    String login(LoginVO loginVO);

    Void logout(UserContext userContext);
}
