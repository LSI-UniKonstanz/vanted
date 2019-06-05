package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.apache.commons.math.linear.RealMatrix;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.List;

public class PivotMDS implements InitialPlacer {


    @Override
    public List<Vector2d> calculateInitialPositions(final List<Node> nodes, final NodeValueMatrix distances) {
        assert nodes.size() == distances.getDimension();

        return null;
    }

    private List<RealMatrix> powerIterate(final RealMatrix matrix) {
        // TODO
        return null;
    }


    private RealMatrix doubleCenter(final NodeValueMatrix distances, final int noPivots) {
        // TODO
        return null;
    }
}
