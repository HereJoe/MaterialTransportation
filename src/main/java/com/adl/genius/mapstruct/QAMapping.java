package com.adl.genius.mapstruct;

import com.adl.genius.entity.po.QA;
import com.adl.genius.entity.vo.QAVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface QAMapping {
    QAMapping M = Mappers.getMapper(QAMapping.class);

    QAVO QA2QAVO(QA qa);
}
