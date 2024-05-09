package com.adl.genius.mapstruct;

import com.adl.genius.entity.UserContext;
import com.adl.genius.entity.po.User;
import com.adl.genius.entity.vo.RegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapping {
    UserMapping M = Mappers.getMapper(UserMapping.class);

    @Mapping(target = "id", ignore = true)
    User registerVO2User(RegisterVO registerVO);

    UserContext user2UserContext(User user);
}
