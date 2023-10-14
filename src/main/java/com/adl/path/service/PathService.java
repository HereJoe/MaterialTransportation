package com.adl.path.service;


import com.adl.path.bean.PathVo;

import java.util.List;

public interface PathService {
    List findShortestCombine2(String sourceName, String targetNames, int maxCombine);

    List findShortestPaths(String sourceName, String targetNames, int maxCombine);

    List<PathVo> shortestPathAlgorithmBasedOnDB(String sourceName, String targetNames, int maxCombine, boolean useLog);
}
