package com.adl.path.dao;


import com.adl.path.bean.CombineDto;
import com.adl.path.bean.Path;

import java.util.List;
import java.util.Map;

public interface PathDao {
    List findPaths4Combination(String sourceName, String targetNames, int combineCount, boolean useLog);

    void saveCombines(List<CombineDto> saveData);
}
