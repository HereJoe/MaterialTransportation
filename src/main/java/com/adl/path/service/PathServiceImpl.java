package com.adl.path.service;

import com.adl.path.bean.*;
import com.adl.path.dao.PathDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.adl.path.enums.DeviceType.Destination;
import static com.adl.path.enums.DeviceType.Source;

@Service
public class PathServiceImpl implements PathService {
    private final Random random = new Random();
    @Resource
    private DeviceService deviceService;
    @Resource
    private ConnService connService;
    @Resource
    private PathDao pathDao;

    @Override
    public List<List<CombineVo>> findShortestCombine(String sourceName, String targetNames, int quantity){
        // get whole connections and build connect map
        Map<Integer, Device> deviceIdMap = new HashMap<>();
        Map<String, Device> deviceNameMap = new HashMap<>();
        Map<Integer, List<ConnectionExt>> connMap = new HashMap<>();
        Set<Integer> targetIdSet = new HashSet<>(16);
        Device source = buildDeviceAndConnMaps(sourceName,targetNames,deviceIdMap,deviceNameMap,connMap,targetIdSet);
        // build whole tree from source (cut off cycle and the paths not leading to targets)
        Map<Integer,List<Node>> targetPathsMap = new HashMap<>();
        buildTree(targetPathsMap,source,connMap,deviceIdMap,targetIdSet);
        if (targetPathsMap.isEmpty()){
            return null;
        }
        // use sub-path to reduce the tree (Node, SubPath)
        //buildSubPathNode(root);
        // dfs find all paths for each target, get target-paths map
        Map<Integer,List<Path>> targetPaths = transferTargetNodes2Paths(targetPathsMap);
        // build combines and get the best n combines
        List<Combine> combines = buildCombinesByPaths(targetPaths, quantity);
        // format path, and subpath for each combine
        calcSharedCost(combines);
        formatPaths(combines);
        // build combineVO for front-end
        return buildCombineVosAndSaveDB(combines);
    }

    private static void formatPaths(List<Combine> combines) {
        for (Combine combine : combines) {
            for (Path path : combine.getPaths()) {
                if (!path.isFormatted()){
                    // reconstruct shared path, and format path
                    StringBuilder sharedSB = new StringBuilder();
                    StringBuilder tmpSharedSB = new StringBuilder();
                    StringBuilder pathSB = new StringBuilder();
                    StringBuilder formatPathSB = new StringBuilder();
                    boolean onProcess=false;
                    BitSet sharedNodeBit = path.getSharedNodeBit();
                    String[] nodeNames = path.getNodeNames();
                    for (int i = 0; i < nodeNames.length; i++) {
                        pathSB.append("->").append(nodeNames[i]);
                        boolean match = sharedNodeBit.get(i);
                        if (match&&!onProcess) {
                            onProcess=true;
                            formatPathSB.append("-><B><I>").append(nodeNames[i]);
                            if (tmpSharedSB.length()>0){
                                sharedSB.append(',').append(tmpSharedSB.substring(2));
                                tmpSharedSB = new StringBuilder();
                            }
                        }else if (!match&&onProcess){
                            onProcess=false;
                            formatPathSB.append("</I></B>->").append(nodeNames[i]);
                        }else {
                            formatPathSB.append("->").append(nodeNames[i]);
                        }
                        // shared path
                        if (match){
                            tmpSharedSB.append("->").append(nodeNames[i]);
                        }
                    }

                    // check if on process at the end point
                    if (onProcess){
                        formatPathSB.append("</B>");
                        if (tmpSharedSB.length()>0){
                            sharedSB.append(',').append(tmpSharedSB.substring(2));
                        }
                    }
                    // set str
                    path.setPathStr(pathSB.substring(2));
                    path.setFormatPathStr(formatPathSB.substring(2));
                    if (sharedSB.length()>0){
                        path.setSharedStr(sharedSB.substring(1));
                    }else {
                        path.setSharedStr("");
                    }

                }
            }
        }
    }

    private List<List<PathVo>> buildPathVosAndSaveDB(List<List<Path>> pathGroups) {
        int batchId = 100000 + random.nextInt(900000);
        List<PathDto> saveData = new ArrayList<>();
        List<List<PathVo>> pathList = new ArrayList<>();
        for (List<Path> pathGroup : pathGroups) {
            List<PathVo> vos = new ArrayList<>();
            for (Path path : pathGroup) {
                PathVo pathVo = new PathVo();
                pathVo.setBatchId(batchId);
                String[] nodeNames = path.getNodeNames();
                pathVo.setSource(path.getNodeNames()[0]);
                pathVo.setTarget(path.getNodeNames()[nodeNames.length-1]);
                StringBuilder sb = new StringBuilder();
                for (String nodeName : nodeNames) {
                    sb.append("->").append(nodeName);
                }
                pathVo.setPath(sb.substring(2));
                pathVo.setTotalNode(nodeNames.length);
                pathVo.setTotalCost(path.getPathCost());
                pathVo.setCreatedBy("findShortestPaths");
                saveData.add(pathVo);
                vos.add(pathVo);
            }
            pathList.add(vos);
        }
        pathDao.savePaths(saveData);
        return pathList;
    }
    private List<List<CombineVo>> buildCombineVosAndSaveDB(List<Combine> combines) {
        // calc shared cost
        calcSharedCost(combines);
        // build data
        List<CombineDto> saveData = new ArrayList<>();
        List<List<CombineVo>> combineList = new ArrayList<>();
        int batchId = 100000 + random.nextInt(900000);
        int combineNumber = 1;
        for (Combine combine : combines) {
            List<CombineVo> vos = new ArrayList<>();
            for (Path path : combine.getPaths()) {
                CombineVo combineVo = new CombineVo();
                combineVo.setBatchId(batchId);
                combineVo.setCombineNumber(combineNumber);
                combineVo.setCombineCost(combine.getTotalCost());
                combineVo.setPathCost(path.getPathCost());
                combineVo.setSharedPathCost(path.getSharedCost());
                combineVo.setPath(path.getPathStr());
                combineVo.setFormattedPath(path.getFormatPathStr());
                combineVo.setSharedPath(path.getSharedStr());
                combineVo.setCreatedBy("findShortestCombines");
                saveData.add(combineVo);
                vos.add(combineVo);
            }
            combineList.add(vos);
            combineNumber++;
        }
        pathDao.saveCombines(saveData);
        return combineList;
    }

    private void calcSharedCost(List<Combine> combines) {
        for (Combine combine : combines) {
            LinkedList<Path> paths = combine.getPaths();
            // initialize
            paths.replaceAll(Path::clone);
            // compare each pair paths
            for (int i = 0; i < paths.size(); i++) {
                // each path
                Path path = paths.get(i);
                for (int j = i + 1; j < paths.size(); j++) {
                    comparePathsPair(path, paths.get(j));
                }
            }
            // calc shared cost
            for (Path path : paths) {
                int sharedCost=0;
                int[] nodeCosts = path.getNodeCosts();
                int[] edgeCosts = path.getEdgeCosts();
                BitSet sharedNodeBit = path.getSharedNodeBit();
                for (int j = sharedNodeBit.nextSetBit(0); j >= 0; j = sharedNodeBit.nextSetBit(j + 1)) {
                    sharedCost += nodeCosts[j];
                }
                BitSet sharedEdgeBit = path.getSharedEdgeBit();
                for (int j = sharedEdgeBit.nextSetBit(0); j >= 0; j = sharedEdgeBit.nextSetBit(j + 1)) {
                    sharedCost += edgeCosts[j];
                }
                path.setSharedCost(sharedCost);
            }
        }
    }

    private Map<Integer, List<Path>> transferTargetNodes2Paths(Map<Integer, List<Node>> targetNodesMap) {
        int id=0;
        Map<Integer, List<Path>> pathMap = new HashMap<>(16);
        for (Map.Entry<Integer, List<Node>> entry : targetNodesMap.entrySet()) {
            List<Path> paths = new ArrayList<>();
            for (Node node : entry.getValue()) {
                int depth = node.getDepth();
                Path path = new Path();
                path.setId(++id);
                path.setPathCost(node.getTotalCost());
                String[] nodeNames = new String[depth + 1];
                int[] nodeIds = new int[depth + 1];
                int[] nodeCosts = new int[depth + 1];
                int[] edgeIds = new int[depth + 1];
                int[] edgeCosts = new int[depth + 1];
                BitSet sharedNodeBit = new BitSet(depth + 1);
                BitSet sharedEdgeBit = new BitSet(depth + 1);
                do{
                    depth = node.getDepth();
                    nodeNames[depth]=node.getDevice().getName();
                    nodeIds[depth]=node.getDevice().getId();
                    edgeIds[depth]=node.getConnId();
                    nodeCosts[depth]=node.getDevice().getCost();
                    edgeCosts[depth]=node.getConnCost();
                    node=node.getParent();
                }
                while (node!=null);
                path.setNodeNames(nodeNames);
                path.setNodeIds(nodeIds);
                path.setEdgeIds(edgeIds);
                path.setNodeCosts(nodeCosts);
                path.setEdgeCosts(edgeCosts);
                path.setSharedNodeBit(sharedNodeBit);
                path.setSharedEdgeBit(sharedEdgeBit);
                paths.add(path);
            }
            pathMap.put(entry.getKey(),paths);
        }
        return pathMap;
    }

    public List<Combine> buildCombinesByPaths(Map<Integer, List<Path>> targetPaths, int maxCombine) {
        // get combines
        List<Integer> keys = new ArrayList<>(targetPaths.keySet());
        List<Combine> combines = getCombinesByPathsRecursive(targetPaths, keys, 0,new Combine());
        // calculate cost for combine
        combines.forEach(this::calcCombineCost);
        // sort and filter combines
        PriorityQueue<Combine> queue = new PriorityQueue<>((c1, c2)->{
            int t1 = c1.getTotalCost();
            int t2 = c2.getTotalCost();
            return Integer.compare(t1, t2);
        });
        combines.forEach(queue::offer);
        List<Combine> list = new ArrayList<>(maxCombine);
        for (int i = 0; i < maxCombine; i++) {
            Combine poll = queue.poll();
            if (poll!=null){
                list.add(poll);
            }
        }
        return list;
    }

    private void calcCombineCost(Combine combine) {
        // calculate cost
        Set<Integer> nodeIdSet = new HashSet<>();
        Set<Integer> edgeIdSet = new HashSet<>();
        int totalCost=0;
        for (Path path : combine.getPaths()) {
            int[] nodeIds = path.getNodeIds();
            int[] edgeIds = path.getEdgeIds();
            int[] nodeCosts = path.getNodeCosts();
            int[] edgeCosts = path.getEdgeCosts();
            for (int i = 0; i < nodeIds.length; i++) {
                if (!edgeIdSet.contains(edgeIds[i])){
                    totalCost+=edgeCosts[i];
                    edgeIdSet.add(edgeIds[i]);
                }
                if (!nodeIdSet.contains(nodeIds[i])){
                    totalCost+=nodeCosts[i];
                    nodeIdSet.add(nodeIds[i]);
                }
            }
        }
        combine.setTotalCost(totalCost);
    }
    /**
     *  find shared sub-paths for each pair
     * @param path1 combination's part
     * @param path2 combination's part
     */
    private static void comparePathsPair(Path path1, Path path2) {
        int[] nodeIds1=path1.getNodeIds();
        int[] nodeIds2=path2.getNodeIds();
        BitSet sharedBitN1 = path1.getSharedNodeBit();
        BitSet sharedBitN2 = path2.getSharedNodeBit();
        BitSet sharedBitE1 = path1.getSharedEdgeBit();
        BitSet sharedBitE2 = path2.getSharedEdgeBit();
        int compInd=0;
        boolean onProcess=false;
        f:for (int i = 0; i < nodeIds1.length; i++) {
            for (int j = compInd; j < nodeIds2.length; j++) {
                if (nodeIds1[i] == nodeIds2[j]) {
                    // shared nodes
                    sharedBitN1.set(i);
                    sharedBitN2.set(j);
                    // shared edges
                    if (onProcess){
                        sharedBitE1.set(i);
                        sharedBitE2.set(j);
                    }else {
                        onProcess=true;
                    }
                    compInd = j+1;
                    continue f;

                }else {
                    onProcess=false;
                }

            }
        }
    }

    private static List<Combine> getCombinesByPathsRecursive(Map<Integer, List<Path>> targetPaths, List<Integer> keys, int keyIndex, Combine curCombine) {
        Integer targetId = keys.get(keyIndex);
        List<Combine> all = new ArrayList<>();
        for (Path path : targetPaths.get(targetId)) {
            Combine combine = curCombine.clone();
            LinkedList<Path> paths = combine.getPaths();
            paths.add(path);
            if (keyIndex==keys.size()-1){
                all.add(combine);
            }else {
                List<Combine> rst = getCombinesByPathsRecursive(targetPaths, keys, keyIndex + 1, combine);
                all.addAll(rst);
            }
        }
        return all;
    }

    private void buildTree(Map<Integer,List<Node>> targetPathsMap, Device device, Map<Integer, List<ConnectionExt>> connMap, Map<Integer, Device> deviceIdMap, Set<Integer> targetSet) {
        Node root = new Node();
        root.setDeviceCost(device.getCost());
        root.setTotalCost(device.getCost());
        root.setDevice(device);
        root.setDepth(0);
        buildTree(targetPathsMap,root,connMap,deviceIdMap,targetSet,new HashSet<>(),new HashSet<>());
    }

    private int buildTree(Map<Integer,List<Node>> targetPathsMap, Node current, Map<Integer, List<ConnectionExt>> connMap, Map<Integer, Device> deviceIdMap, Set<Integer> targetSet, Set<Integer> visited, Set<Integer> skipSet) {
        // achieve target
        if (Destination.name().equalsIgnoreCase(current.getDevice().getType())){
            // save target nodes
            int id = current.getDevice().getId();
            if (targetSet.contains(id)){
                List<Node> targetPath = targetPathsMap.get(id);
                if (targetPath==null){
                    targetPath=new ArrayList<>();
                }
                targetPath.add(current);
                targetPathsMap.put(id,targetPath);
                return 0;
            }else {
                // skip other targets
                return -1;
            }
        }
        int count=0;
        List<ConnectionExt> conns = connMap.get(current.getDevice().getId());
        if (conns!=null) {
            for (ConnectionExt conn : conns) {
                int dest = conn.getDestinationDevice();
                // skip
                if (skipSet.contains(dest)){
                    continue;
                }
                if (!visited.contains(dest)){
                    visited.add(dest);
                    Node child = new Node();
                    child.setParent(current);
                    Device des = deviceIdMap.get(dest);
                    child.setDevice(des);
                    child.setDeviceCost(des.getCost());
                    child.setConnId(conn.getId());
                    child.setConnCost(conn.getWeight());
                    child.setTotalCost(current.getTotalCost()+conn.getWeight()+des.getCost());
                    child.setDepth(current.getDepth()+1);
                    int rst = buildTree(targetPathsMap,child, connMap, deviceIdMap, targetSet, visited,skipSet);
                    if (rst>=0){
                        List<Node> children = current.getChildren();
                        if (children==null){
                            children=new ArrayList<>();
                        }
                        children.add(child);
                        count++;
                        // related devices can be reused, devices without relationship can be skipped next time
                        visited.remove(dest);
                    }else {
                        skipSet.add(dest);
                    }
                }
            }
        }
        // a non-destination-node who has no child can be cut off
        if (count==0) {
            count=-1;
        }else {
            current.setChildCount(count);
        }

        return count;
    }

    private Device buildDeviceAndConnMaps(String sourceName, String targetNames, Map<Integer, Device> deviceIdMap, Map<String, Device> deviceNameMap, Map<Integer, List<ConnectionExt>> connMap, Set<Integer> targetIdSet) {
        // build device map
        List<Device> devices = deviceService.listAvailableDevices();
        for (Device d : devices) {
            deviceIdMap.put(d.getId(),d);
            deviceNameMap.put(d.getName(),d);
        }
        // check source
        Device source = deviceNameMap.get(sourceName.trim());
        if (source==null){
            throw new RuntimeException(String.format("source %s doesn't exist or is unavailable",sourceName));
        }
        if (!Source.name().equalsIgnoreCase(source.getType())){
            throw new RuntimeException(String.format("source %s is not a source Device",sourceName));
        }
        // check targets
        for (String s : targetNames.split(",")) {
            Device d = deviceNameMap.get(s.trim());
            if (d==null){
                throw new RuntimeException(String.format("destination %s doesn't exist or is unavailable",s));
            }
            if (!Destination.name().equalsIgnoreCase(d.getType())){
                throw new RuntimeException(String.format("destination %s is not a destination Device",s));
            }
            targetIdSet.add(d.getId());
        }
        // build conn map
        List<ConnectionExt> conns = connService.listAvailableConn();
        for (ConnectionExt conn : conns) {
            List<ConnectionExt> list = connMap.computeIfAbsent(conn.getSourceDevice(),(k)->new ArrayList<>());
            list.add(conn);
        }
        return source;
    }

    @Override
    public List<List<PathVo>> findShortestPaths(String sourceName, String targetNames, int quantity) {
        // get whole connections and build connect map
        Map<Integer, Device> deviceIdMap = new HashMap<>();
        Map<String, Device> deviceNameMap = new HashMap<>();
        Map<Integer, List<ConnectionExt>> connMap = new HashMap<>();
        Set<Integer> targetIdSet = new HashSet<>();
        Device source = buildDeviceAndConnMaps(sourceName,targetNames,deviceIdMap,deviceNameMap,connMap,targetIdSet);
        // build whole tree from source (cut off cycle and the paths not leading to targets)
        Map<Integer,List<Node>> targetPathsMap = new HashMap<>();
        buildTree(targetPathsMap,source,connMap,deviceIdMap,targetIdSet);
        if (targetPathsMap.isEmpty()){
            return null;
        }
        // dfs find all paths for each target, get target-paths map
        Map<Integer,List<Path>> targetPaths = transferTargetNodes2Paths(targetPathsMap);
        // choose the cheapest n path for each target
        List<List<Path>> pathGroups = chooseCheapestNPath(targetPaths, quantity);
        // build pathvo for front-end, save path
        return buildPathVosAndSaveDB(pathGroups);
    }

    private List<List<Path>> chooseCheapestNPath(Map<Integer, List<Path>> targetPathsMap, int maxPath) {

        List<List<Path>> pathGroups = new ArrayList<>();
        targetPathsMap.forEach((k,v)->{
            // sort and filter combines
            PriorityQueue<Path> queue = new PriorityQueue<>((c1, c2)->{
                int t1 = c1.getPathCost();
                int t2 = c2.getPathCost();
                return Integer.compare(t1, t2);
            });
            for (Path path2 : v) {
                queue.offer(path2);
            }
            List<Path> pathGroup = new ArrayList<>();
            for (int i = 0; i < maxPath; i++) {
                Path poll = queue.poll();
                if (poll!=null){
                    pathGroup.add(poll);
                }
            }
            pathGroups.add(pathGroup);
        });
        return pathGroups;
    }

    @Override
    public List<PathVo> shortestPathAlgorithmBasedOnDB(String sourceName, String targetNames, int quantity, boolean useLog){
        return pathDao.findShortestPaths(sourceName, targetNames, quantity, useLog);
    }

}
