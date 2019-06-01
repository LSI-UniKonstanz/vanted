package org.vanted.addons.stressminaddon.util;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import org.graffiti.graph.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MockShortestDistances {

    public static NodeValueMatrix getShortestPaths(List<Node> nodes) {
        NodeValueMatrix result = new NodeValueMatrix(nodes.size());

        for (int row = 0; row < nodes.size(); row++) {
            HashMap<Node, Integer> distancesIntermediate = new HashMap<>(nodes.size());
            GraphHelper.getShortestDistances(distancesIntermediate, new HashSet<>(Collections.singleton(nodes.get(row))),
                    false, 0);
            for (int col = 0; col < row; col++) {
                result.set(row, col, distancesIntermediate.getOrDefault(nodes.get(col), -1));
            }
        }

        return result;
    }
}
