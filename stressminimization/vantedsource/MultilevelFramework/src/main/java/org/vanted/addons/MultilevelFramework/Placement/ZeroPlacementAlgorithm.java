package org.vanted.addons.MultilevelFramework.Placement;

import java.util.HashMap;
import java.util.Random;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * Places each node at the position of its parent with a small random offset.
 */
public class ZeroPlacementAlgorithm extends AbstractPlacementAlgorithm {
	private double maxOffset;

	@Override
	public void setParameters(Parameter[] params) {
		maxOffset = (double) params[0].getValue();
	}

	@Override
	public Parameter[] getParameters() {
		DoubleParameter maxOffsetParameter = new DoubleParameter(0.01, 0.0, "Maximum Offset",
				"Nodes will be placed within this distance from their parent node in every direction.");
		return new Parameter[] { maxOffsetParameter };

	}

	@Override
	public String getName() {
		return "Zero Placement";
	}

	@Override
	public String getDescription() {
		return "Places nodes at the position of their parent with a small random offset.";
	}

	@Override
	public void execute() {
		HashMap<Node, Vector2d> positions = new HashMap<Node, Vector2d>();
		for (Node n : selection.getNodes()) {
			Random rand = new Random();
			Vector2d parentPosition = getParentPosition(n);
			parentPosition.x += (rand.nextDouble() * 2.0 - 1.0) * maxOffset;
			parentPosition.y += (rand.nextDouble() * 2.0 - 1.0) * maxOffset;
			positions.put(n, parentPosition);
		}

		GraphHelper.applyUndoableNodePositionUpdate(positions, "");
	}

}
