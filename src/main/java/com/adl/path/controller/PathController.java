package com.adl.path.controller;

import com.adl.path.bean.Combine;
import com.adl.path.bean.Resp;
import com.adl.path.service.PathService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/path")
@RestController
public class PathController {

    @Resource
    private PathService pathService;


    @CrossOrigin(origins = "*")
    @GetMapping("/findShortestCombines/{sourceName}/{targetNames}/{maxCombine}")
    public Resp findShortestCombine(@PathVariable String sourceName,@PathVariable String targetNames,@PathVariable int maxCombine){
        return findShortestCombine(sourceName,targetNames,maxCombine,false);
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/findShortestCombines/{sourceName}/{targetNames}/{maxCombine}/{saveDB}")
    public Resp findShortestCombine(@PathVariable String sourceName,@PathVariable String targetNames,@PathVariable int maxCombine,@PathVariable boolean saveDB){
        return findShortestCombine(sourceName,targetNames,maxCombine,saveDB,false);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/findShortestCombines/{sourceName}/{targetNames}/{maxCombine}/{saveDB}/{saveLog}")
    public Resp findShortestCombine(@PathVariable String sourceName,@PathVariable String targetNames,@PathVariable int maxCombine,@PathVariable boolean saveDB, boolean useLog){
        //List combines = pathService.findShortestCombine(sourceName,targetNames, maxCombine, saveDB, useLog);
        List combines = null;
        try {
            combines = pathService.findShortestCombine2(sourceName,targetNames, maxCombine, saveDB);
            if (combines==null){
                return Resp.fail("no combine among source and targets");
            }
        } catch (Exception e) {
            return Resp.fail(e.getMessage());
        }
        return Resp.success(combines);
    }

}
