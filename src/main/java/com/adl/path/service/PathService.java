package com.adl.path.service;


import java.util.List;

public interface PathService {
    List findShortestCombine2(String sourceName, String targetNames, int maxCombine, boolean saveDB);

    List findShortestCombine(String sourceName, String targetNames, int maxCombine, boolean saveDB, boolean useLog);

}
