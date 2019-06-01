package org.vanted.addons.multilevelframework;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;

public class RandomPlacer implements Placer {

	@Override
	public Parameter[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParameters(Parameter[] parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reduceCoarseningLevel(MultilevelGraph multilevelGraph) {
		CoarsenedGraph cg = multilevelGraph.popCoarseningLevel();
		Collection<? extends MergedNode> allMergedNodes = cg.getMergedNodes();

		Random random = new Random();
		
		for(MergedNode mergedNode : allMergedNodes) { 
			Collection<? extends Node> innerNodes = mergedNode.getInnerNodes();
			
			for(Node node : innerNodes) {
				double randx = (-1.0 + 2.0 * random.nextDouble()) * 100;
				double randy = (-1.0 + 2.0 * random.nextDouble()) * 100;
				

				AttributeHelper.setPosition(node, AttributeHelper.getPositionX(mergedNode) + randx, AttributeHelper.getPositionX(mergedNode) + randy);
			}
		}
		
	}
	
}
