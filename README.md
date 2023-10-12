### Software Engineering & Projects 2023

#### 1. Project Information

| Project Name | Shortest Path Algorithm for Material Transportation                                                                                                                                                                                               |
| ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Team         | PATH14PG                                                                                                                                                                                                                                          |
| Team Members | Jiani Wu (a1864754)<br/>Nihal Manjunath (a1813127)<br/>Prabhjot Kaur (a1877033)<br/>Pranjal Batra (a1877315)<br/>Qinyu Wang (a1844745) <br/>Sarthak Rawat (a1875619) <br/>Yaxin Li (a1847376)<br/>Yidan Wang (a1873148) <br/>Zhuo Yang (a1878962) |

#### 2. Remote Demo : [Remote Demo](http://47.106.108.49/shortestPath/swagger-ui.html)

#### 3. Project folder/files description

**bin** : *folder for tools*

> `start.bat` : one click to build and run the local projec, then visite the link:
> [Swagger UI](http://localhost/shortestPath/swagger-ui.html) (Requirement: JDK 8+, Maven)
> `start.sh` : one click script for linux, mac.

**src/resource/script** : *position for procedure, (TODO: which will be synchronized to database autometically.)*

> **`findMultiplePath(source_name,target_name,path_max,enable_log)`**
> `source_name`: name of source spot
> `target_name`: name of target spot
> `path_max`: how many paths do you want
> `enable_log`: TRUE/FALSE, if set TRUE, the algorithm execution will be recorded in details.
> e.g.: CALL findMultiplePath ('Device A','Device G',6, FALSE);

#### 4. Algorithm decision

##### Current Algorithm Selection:

> - The problem addressed in this project is the single-source shortest path problem in a graph. The available algorithms to solve such problems include UCS, A*, Dijkstra, and Bellman.
> 
> - Since the project's business scenario does not involve negative cycles, we can exclude the computationally expensive Bellman's algorithm (O(VE)).
> 
> - Due to the absence of suitable heuristic functions in the known conditions, A* cannot be used alone when searching for a single shortest path.
> 
> - Because the goal of finding the shortest path is well-defined, and we don't need
>    to search the entire graph, we can directly use UCS, which can exit the
>    search earlier than Dijkstra while maintaining the same time complexity.

##### Future Optimization Strategies:

If the business model changes in the future, and the network becomes very large, we can optimize in the following ways:

> - Exploit the symmetry of undirected graphs by concurrently searching in both the forward and reverse graphs to select the more favorable path.
>    Alternatively, you can simply choose nodes with fewer out-degrees from the start and end points for experimentation.
> 
> - If you need to find a large number of shortest paths (e.g., 100 paths), you can also accelerate using a hybrid approach of Dijkstra and A* in an N+1 pattern. In the first round, perform a reverse Dijkstra search to find a heuristic function, then apply A*.
