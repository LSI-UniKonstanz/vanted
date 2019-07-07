# Stress minimization - getting started

This add-on calculates the total stress in the graph and changes
the layout to minimize this stress. This usually results in
nice looking layouts if the graph does not have too much edges.

## Process Description

The stress of a layout is defined by a mathmatical formula that 
measures the displacement of all nodes relative to their graph
theoretical distances. 

In layouter tries to minimize the stress of the layouts.
It therefore tries to assign all nodes positions such that
the distances in the layout plane from some node to any other one
matches their *graph theoretical distance*, the minimal number of edges
one has to walk to get from the first node to the second. 

By default the layouter is configured to calculate only an approximated
layout with focus on the global structure.
Please read the Parameters section for more information on this.

## Some typical layouts



## Parameters

You can change the following parameters:

### Auto redraw
If checked, after every iteration of the layouter, the current layout
will be shown in VANTED. This may greatly increase the computation time, 
but lets you treck the layouting process. 
You always change this option during the layouting process

### Landmarks count
The amount of nodes in the graph that gets layouted. 
By default, this value is 100, which gives a broad approximation 
of the global structure for large to very large graphs 
(5000-10.000+ nodes) with a very short runtime. 
You may arbitrarily increase this value up to 1000 nodes 
to enhance the level of detail.
You may also turn of this option by setting the value 
to "off" / all nodes of the graph. 
With this option the layouter will compute a globally 
and locally optimal layout, which will in general look 
much nicer but may require a lot of time and memory. 
Turning of this option is only usefull for graphs 
with less than 5000 nodes. Graphs with more than 1000 nodes 
may however still need 10-30 minutes for layouting.

### Disable Landmark Preprocessing
By default this layouter runs itself with a low number 
of landmarks before executing the main process to gain 
a good initial layout. 
If you already have a good initial layout or if you are 
using this layouter in a multilevel framework it is recommended
to disable this preprocessing by turning on this option
(select the disable landmark preprocessing parameter),
since the preprocessing will destroy such an initial layout.

### Stress change threshold
The minimum amount of total stress change that has to happen in
each iteration. If the total stress change falls below this amount, 
the computation terminates. 
Lower numbers may result in longer computation time.
Higher numbers may disturb the resulting layout.

### Node movemend threshold
Minimum total node movement that has to happen in each iteration.
If the total node movement falls below this amount, the computation
terminates.
Lower nubers may result in longer computation time.
Higher numbers may disturb the resulting layout.

### Iteration Maximum
Maximum number of iterations until the computation terminates
by default, this value is set to 75, which are usually enough 
iterations to compute a very good layout. 
Iterations above this treshold tend to optimize vary small 
imperfections in the layout which are usually not of any interest.

### Weight factor
Determines how importand distant nodes are for computing the total 
stress. By default this is 2 and in most cases this parameter does 
not need to be changed.
If you need a layout that approximates distances
between all nodes equally well, you should reduce this 
value to 1 or 0. 

### Randomize initial layout
If checked, the layout of the graph will be randomized before 
applying the stress minimization layout. 
This option is only usefull if you think you have a bad initial layout
and you disabled landmark preprocessing. 
However the stress minimization layouter is able to 
produce a nice layout starting on any layout, 
but computation may take more time.
