# Protokoll des 3. internen Treffens

Datum | Uhrzeit
------|---------
16.05 | 17:00

- Vanted aus Repo läuft jetzt bei allen und Änderungen lassen sich auch bei anderen compilieren.
- Sowohl für Stress Minimization, als auch für das Multilevel Framework haben wir schon Diagramme gemacht
- Wir reviewen gemeinsam diese Diagramme
- Design Decision: Wir müssen selber Algorithmen ausführen, können GravistoService nicht benutzen, da Parameter nicht setzbar.
- Multilevel Framework GUI ist ein Problem
- Test Cases
```
automated tests:
Algorithms throw exception in check when graph is empty

Test graphs:
    Graph consisting of two subgraphs
    Random graphs with 1, 5, 10 and 50 nodes

Benchmark graphs:
    Graphs from Michael and Karsten (will be uploaded to repository)

Stress Minimization:
    Algorithm returns on already perfect graph (need graph layout with minimal stress)
    Algorithm does not change layout of graph with minimal stress
    After layout all nodes still need to be present

Multilevel:
    Graph that statisfies coarsening critirium is not further reduced
    Parameters need to be passed on to algorithms
    CoarseningAlgorithms:
        All nodes need to be present as subnodes after execution
    PlacementAlgorithms:
        All subnodes need to be present as nodes after execution

user tests:
algorithms update progress bar
clicking stop in algorithm execution panel stops execution
layout process terminates

Stress Minimization:
    after exection layout is acceptible
```
- je ein Branch per Addon
