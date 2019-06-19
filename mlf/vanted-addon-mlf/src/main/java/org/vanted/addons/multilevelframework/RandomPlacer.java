package org.vanted.addons.multilevelframework;

import java.util.Collection;
import java.util.Random;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

/**
 * Implememtation of the {@link Placer} Interface that reduces Coarsening Levels
 * by randomly placing the inner nodes randomly around their {@link MergedNode}
 * 
 * @author Katze
 *
 */
public class RandomPlacer implements Placer {

	private Parameter[] parameters;
	private double maxPlaceDistance = 50;
	
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
			if (param.getName().equals("maxPlaceDistance")) {
				DoubleParameter p = (DoubleParameter) param;
				maxPlaceDistance = p.getDouble();
			}
		}
	}

	/**
	 * Places the internal Nodes of the topmost Coarsening Level of the given
	 * {@link MultilevelGraph} randomly around their average Position
	 * 
	 * @param multilevelGraph the coarsened Graph. Needs to contain at least one
	 *                        {@link InternalGraph}
	 * @author Katze
	 */
	@Override
	public void reduceCoarseningLevel(MultilevelGraph multilevelGraph) {
		CoarsenedGraph cg = multilevelGraph.popCoarseningLevel();
		Collection<? extends MergedNode> allMergedNodes = cg.getMergedNodes();

		Random random = new Random();

		for (MergedNode mergedNode : allMergedNodes) {
			Collection<? extends Node> innerNodes = mergedNode.getInnerNodes();

			for (Node node : innerNodes) {
				double randx = (-1.0 + 2.0 * random.nextDouble()) * maxPlaceDistance;
				double randy = (-1.0 + 2.0 * random.nextDouble()) * maxPlaceDistance;

				double x = AttributeHelper.getPositionX(mergedNode) + randx;
				double y = AttributeHelper.getPositionY(mergedNode) + randy;

				AttributeHelper.setPosition(node, x, y);
			}
		}

	}

}
