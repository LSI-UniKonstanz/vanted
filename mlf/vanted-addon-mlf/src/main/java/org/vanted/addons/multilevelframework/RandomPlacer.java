package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.multilevelframework.sm_util.gui.Describable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;

import static org.vanted.addons.multilevelframework.MlfHelper.validateNumber;

/**
 * Implementation of the {@link Placer} Interface that reduces Coarsening Levels
 * by randomly placing the inner nodes randomly around their {@link MergedNode}
 *
 * @author Katze
 */
public class RandomPlacer implements Placer {

    private double maxPlaceDistance = 50;
    final static String MAX_PLACE_DIST_NAME = "Maximum place distance";

    /**
     * Array of parameters that will be displayed in the GUI.
     */
    private Parameter[] parameters = {
            new DoubleParameter(this.maxPlaceDistance, MAX_PLACE_DIST_NAME,
                    "The maximum distance from the position of the merged node " +
                            "where the represented nodes will be placed")
    };

    /**
     * See {@link Placer}
     *
     * @author Katze
     */
    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    /**
     * Sets Parameters, this implementation automatically tries to assign the
     * maximum place distance if given as a {@link Parameter}.
     *
     * @author Katze
     */
    @Override
    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;

        for (Parameter param : this.parameters) {
            if (MAX_PLACE_DIST_NAME.equals(param.getName())) {
                final double value = ((DoubleParameter) param).getDouble();
                validateNumber(value, 0, Double.MAX_VALUE, MAX_PLACE_DIST_NAME);
                maxPlaceDistance = value;
            }
        }
    }

    /**
     * Places the internal Nodes of the topmost Coarsening Level of the given
     * {@link MultilevelGraph} randomly around their average Position
     *
     * @param multilevelGraph the coarsened Graph. Needs to contain at least one
     *                        {@link InternalGraph}
     * @author Katze, Gordian
     */
    @Override
    public void reduceCoarseningLevel(MultilevelGraph multilevelGraph) {
        CoarsenedGraph cg = multilevelGraph.popCoarseningLevel();
        Collection<? extends MergedNode> allMergedNodes = cg.getMergedNodes();

        Random random = new Random();


        for (MergedNode mergedNode : allMergedNodes) {
            Collection<? extends Node> innerNodes = mergedNode.getInnerNodes();

            // Compute minimum distance from the center of the MergedNode.
            // If multiple nodes are placed within this distance they are going to overlap.
            // Note that this doesn't guarantee that nodes won't overlap (it's a random placer after all),
            // it just makes it less likely.
            // Divide by "almost 2", so the nodes don't lie right next to each other.
            double minPlaceDistanceX = innerNodes.stream()
                    .map(AttributeHelper::getWidth)
                    .max(Comparator.naturalOrder()).orElse(0.0) / 1.9;
            double minPlaceDistanceY = innerNodes.stream()
                    .map(AttributeHelper::getHeight)
                    .max(Comparator.naturalOrder()).orElse(0.0) / 1.9;

            for (Node node : innerNodes) {
                double randx = (-1.0 + 2.0 * random.nextDouble()) * maxPlaceDistance;
                double randy = (-1.0 + 2.0 * random.nextDouble()) * maxPlaceDistance;

                // scale the minimum distance using a random combination of 1 and -1
                double xFactor = random.nextBoolean() ? 1 : -1;
                double yFactor = random.nextBoolean() ? 1 : -1;

                double x = minPlaceDistanceX * xFactor + AttributeHelper.getPositionX(mergedNode) + randx;
                double y = minPlaceDistanceY * yFactor + AttributeHelper.getPositionY(mergedNode) + randy;

                AttributeHelper.setPosition(node, x, y);
            }
        }

    }

    /**
     * @author Gordian
     * @see Describable#getName()
     */
    @Override
    public String getName() {
        return "Random Placer";
    }

    /**
     * @author Gordian
     * @see Describable#getDescription()
     */
    @Override
    public String getDescription() {
        return "Randomly places the nodes represented by a merged node in a customizable radius around the position of"
                + " the merged node";
    }
}
