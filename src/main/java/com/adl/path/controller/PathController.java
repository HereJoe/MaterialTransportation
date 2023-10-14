package com.adl.path.controller;

import com.adl.path.bean.Resp;
import com.adl.path.service.PathService;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import java.util.List;

@RequestMapping("/path")
@RestController
@Validated
public class PathController {

    @Resource
    private PathService pathService;

    @CrossOrigin(origins = "*")
    @GetMapping("/findShortestCombines/{sourceName}/{targetNames}/{quantity}")
    public Resp findShortestCombine(@PathVariable @NotEmpty String sourceName, @PathVariable @NotEmpty String targetNames, @PathVariable @Min(value = 1) int quantity){
        try {
            List combines = pathService.findShortestCombine2(sourceName,targetNames, quantity);
            if (combines==null){
                return Resp.fail("no combine among source and targets");
            }
        return Resp.success(combines);
        } catch (Exception e) {
            return Resp.fail(e.getMessage());
        }
    }
    @CrossOrigin(origins = "*")
    @GetMapping("/findShortestPaths/{sourceName}/{targetNames}/{quantity}")
    public Resp findShortestPaths(@PathVariable String sourceName,@PathVariable String targetNames,@PathVariable int quantity){
        return findShortestPaths(sourceName,targetNames,quantity,false);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/findShortestPaths/{sourceName}/{targetNames}/{quantity}/{dbAlgorithm}")
    public Resp findShortestPaths(@PathVariable String sourceName,@PathVariable String targetNames,@PathVariable int quantity,@PathVariable boolean dbAlgorithm){
        return findShortestPaths(sourceName,targetNames,quantity,dbAlgorithm,false);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/findShortestPaths/{sourceName}/{targetNames}/{quantity}/{dbAlgorithm}/{useLog}")
    public Resp findShortestPaths(@PathVariable String sourceName,@PathVariable String targetNames,@PathVariable int quantity,@PathVariable boolean dbAlgorithm,@PathVariable boolean useLog){
        List paths;
        try {
            if (dbAlgorithm){
                paths = pathService.shortestPathAlgorithmBasedOnDB(sourceName,targetNames, quantity, useLog);
            }else {
                paths = pathService.findShortestPaths(sourceName,targetNames, quantity);
            }
            if (paths==null){
                return Resp.fail("no combine among source and targets");
            }
        } catch (Exception e) {
            return Resp.fail(e.getMessage());
        }
        return Resp.success(paths);
    }

}
