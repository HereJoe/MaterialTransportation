package com.adl.genius.mapstruct;

import com.adl.genius.entity.po.Dialog;
import com.adl.genius.entity.vo.DialogVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DialogMapping {
    DialogMapping M = Mappers.getMapper(DialogMapping.class);

    @Mapping(target = "lastActiveTime", source = "updateTime")
    DialogVO dialog2DialogVO(Dialog dialog);
}
