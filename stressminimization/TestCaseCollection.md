# General Test cases

## automated tests:

 - Algorithms throw exception in check when graph is empty

 - Test graphs:
    - Graph consisting of two subgraphs
    - Random graphs with 1, 5, 10 and 50 nodes

- Benchmark graphs:
    - Graphs from Michael and Karsten (will be uploaded to repository)

- Stress Minimization:
    - Algorithm returns on already perfect graph (need graph layout with minimal stress)
    - Algorithm does not change layout of graph with minimal stress
    - After layout all nodes still need to be present

- Multilevel:
    - Graph that statisfies coarsening critirium is not further reduced
    - Parameters need to be passed on to algorithms
    - CoarseningAlgorithms:
        All nodes need to be present as subnodes after execution
    - PlacementAlgorithms:
        All subnodes need to be present as nodes after execution

## use tests:
- algorithms update progress bar
- clicking stop in algorithm execution panel stops execution
- layout process terminates

- Stress Minimization:
    - after exection layout is acceptible
