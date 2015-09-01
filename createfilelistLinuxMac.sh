#!/bin/bash
echo "Create XML Plugin file lists..."

find ./src/main/java/ -name "*.xml" > ./src/main/resources/plugins.txt

rm src/main/resources/plugins_cluster.txt
rm src/main/resources/plugins_exclude.txt

echo create Cluster Plugin List
echo "./org/graffiti/plugins/views/defaults/plugin.xml" > src/main/resources/plugins_cluster.txt
echo "./org/graffiti/plugins/modes/defaults/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/editcomponents/label_alignment/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/editcomponents/cluster_colors/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/expand_no_overlapp/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./org/graffiti/plugins/modes/defaultEditMode/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./org/graffiti/plugins/ios/importers/gml/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./org/graffiti/plugins/ios/exporters/gml/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./org/graffiti/plugins/inspectors/defaults/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./org/graffiti/plugins/editcomponents/defaults/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./org/graffiti/plugins/attributes/defaults/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./org/graffiti/plugins/attributecomponents/simplelabel/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/svg_exporter/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/invert_selection/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/graph_cleanup/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/tree_simple/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/rt_tree/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/random/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/radial_tree/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/pattern_springembedder/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/grid/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/graph_to_origin_mover/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/expand_reduce_space/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/circle/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/pajek/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/matrix/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/exporters/matrix/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/zoomfit/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/set_background_color/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/info_dialog_cluster_analysis/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/enhanced_attribute_editors/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/editing_tools/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/node_mover/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/ipk_graffitiview/plugin.xml" >> src/main/resources/plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/layout_control/pluginClusterTabs.xml" >> src/main/resources/plugins_cluster.txt


echo create Exclude-List for DBE-Gravisto
echo "./org/graffiti/plugins/ios/gml/gmlWriter/plugin.xml" > src/main/resources/plugins_exclude.txt
#echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/naive_pattern_finder/plugin.xml" >> src/main/resources/plugins_exclude.txt
#echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/copy_pattern_layout/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/random/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/ios/exporters/gmlxml/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/views/defaults/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/ios/gml/gmlReader/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/ios/gml/gmlReader/parser/build.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/info_dialog/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/guis/switchselections/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/random_node_resizer/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/node_mover/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/node_highlighter/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/edge_directer/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/trivialgridrestricted/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/trivialgrid/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/editor/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/springembedderrestricted/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/springembedder/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/pluginsForOnlineUse.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/webstart/jarprefs.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/tools/enhancedzoomtool/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/views/matrix/plugin.xml" >> src/main/resources/plugins_exclude.txt
####echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/fast_view/plugin.xml" >> src/main/resources/plugins_exclude.txt
#echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/print/printer/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/print/preview/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/jfree/chart/demo/piedata.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/jfree/chart/demo/categorydata.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/apsp/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/bfs/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/bfstopsort/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/bn_preparator/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/bonacich_eigenvector/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/pattern_from_canonical_label/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/closeness/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/connectspecial/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/connect/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/degree/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/edge_labeling/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/excentricity/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/fordfulkerson/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/frequent_pattern_finder/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/genophen/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/iterative_partitioning/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/maximum_independent_set/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/random_walk_betweenness/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/rectangle/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/springembedder_1/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/systematic_motif_generator/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/wclique3/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/tools/zoomtool/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/tools/enhancedzoomtool/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/HighDimEmbed/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/info_dialog_cluster_analysis/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/pattern_springembedder_no_cache/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/layout_control/pluginClusterTabs.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/scripting/plugin.xml"  >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/fish_eye/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/graph_colorer/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/generators/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/randomizedlabeling/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/numbernodes/plugin.xml" >> src/main/resources/plugins_exclude.txt
#echo "./org/graffiti/plugins/ios/importers/graphml/plugin.xml" >> src/main/resources/plugins_exclude.txt
#echo "./org/graffiti/plugins/ios/exporters/graphviz/plugin.xml" >> src/main/resources/plugins_exclude.txt
#echo "./org/graffiti/plugins/ios/exporters/graphml/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./org/graffiti/plugins/ios/exporters/gmlxml/plugin.xml" >> src/main/resources/plugins_exclude.txt
#echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/xgmml/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/matrix/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/genophen/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/flatfile/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/exporters/matrix/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/collapsed_graph_producer/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "./log4j.xml" >> src/main/resources/plugins_exclude.txt
#excluding the SOM plugin until it's fixed
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/som/plugin.xml" >> src/main/resources/plugins_exclude.txt
echo "READY"
