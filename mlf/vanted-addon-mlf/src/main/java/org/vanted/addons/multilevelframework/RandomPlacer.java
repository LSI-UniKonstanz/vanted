package org.vanted.addons.multilevelframework;

import java.util.Collection;
import java.util.Random;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

public class RandomPlacer implements Placer {

	Parameter[] parameters;
	
	/**
	 * See {@link Placer}
	 * @author Katze
	 */
	@Override
	public Parameter[] getParameters() {
		return null;
	}

	/**
	 * See {@link Placer}
	 * @author Katze
	 */
	@Override
	public void setParameters(Parameter[] parameters) {
		this.parameters = parameters;
	}

	/**
	 * Places the internal Nodes of the topmost Coarsening Level of the given {@link MultilevelGraph} randomly around their average Position
	 * @param multilevelGraph the coarsened Graph. Needs to contain at least one {@link InternalGraph}
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
				double randx = (-1.0 + 2.0 * random.nextDouble()) * 100;
				double randy = (-1.0 + 2.0 * random.nextDouble()) * 100;
				
				double x =  AttributeHelper.getPositionX(mergedNode) + randx;
				if(x < 0) x = -x;
				double y =  AttributeHelper.getPositionY(mergedNode) + randy;
				if(y < 0) y = -y;
				
				

				AttributeHelper.setPosition(node, x, y);
			}
		}

	}

}
