General information und documentation [can be found here](https://www.notion.so/General-Comments-9f9f8ec88b93412f92b9304a852185e1).

This module/library provides graph data structures (and operations on them) that are more efficient for some purposes.

This module is dependent on VANTED core.

The main idea is to avoid working with `Node` objects directly but instead configure a collection of `Node`s as a *base set* once and then keep bit vectors representing a subset of that base set. Union/intersect on these bit vectors is much faster. These operations are often required in BFS, which is done when computing connected componentents.

A good starting point for reading the code is `IndexedNodeSet`.