package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.List;

public interface InitialPlacer {

    public List<Vector2d> calculateInitialPositions(final List<Node> nodes,
                                                    final NodeValueMatrix distances);
}
