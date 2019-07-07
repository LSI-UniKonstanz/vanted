package org.vanted.addons.MultilevelFramework.Placement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.AttributeHelper;
import org.graffiti.graph.Node;

/**
 * Places each node randomly.
 */
public class RandomPlacementAlgorithm extends AbstractPlacementAlgorithm {

	/**
	 * calculates an appropriate field-size then distributes Nodes randomly across
	 * the field
	 */
	@Override
	public void execute() {
		List<Node> NodeList = new ArrayList<Node>();
		for (Node n : selection.getNodes()) {
			NodeList.add(n);
		}
		HashSet<Node> parentSet = new HashSet<Node>();
		for (Node n : NodeList) {
			parentSet.add(getParent(n));// creating list of distinct parents
		}

		double maxX = 0;
		double maxY = 0;
		for (Node n : parentSet) {// find x and y boundaries of parent-graph
			double posX = AttributeHelper.getPosition(n).getX();
			double posY = AttributeHelper.getPosition(n).getY();
			if (posX > maxX) {
				maxX = posX;
			}
			if (posY > maxY) {
				maxY = posY;
			}
		}

		double fieldSizeX = maxX * (NodeList.size() / parentSet.size());// calculate field size
		double fieldSizeY = maxY * (NodeList.size() / parentSet.size());

		Random positionGenerator = new Random();

		for (Node n : NodeList) {
			double posX = positionGenerator.nextDouble() * fieldSizeX;// calculate random positions for each node
			double posY = positionGenerator.nextDouble() * fieldSizeY;
			AttributeHelper.setPosition(n, posX, posY);
		}

	}

	@Override
	public String getName() {
		return "Random Placer";
	}

	@Override
	public String getDescription() {
		return "Randomly places nodes within the boundary of the parent graph.";
	}
}
