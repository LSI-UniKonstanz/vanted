package org.vanted.addons.MultilevelFramework.Coarsening;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

/**
 * The Solar merger chooses Suns randomly out of the nodes, assigns the other
 * nodes as planets to their nearest sun then collapses the created Solarsystems
 * into their respective suns.
 */
public class SolarMergerCoarsening extends AbstractCoarseningAlgorithm {

	protected double sunProbability;

	@Override
	public String getName() {
		return "Solar Merger";
	}

	@Override
	public String getDescription() {
		return "Nodes are partitioned into solar systems and every node is merged into the respective sun node.";
	}

	@Override
	public void execute() {

		List<Node> allNodesList = new ArrayList<Node>(childSelection.getNodes());
		ArrayList<Node> suns = new ArrayList<Node>();
		Random isSun = new Random();
		int idCounter = 0;
		long assigned[] = new long[allNodesList.size()];
		int distanceToSun[] = new int[assigned.length];
		for (Node n : allNodesList) {// adding a node to sun-list with probability p
			assigned[idCounter] = -1;
			distanceToSun[idCounter] = 0;
			int i = isSun.nextInt(1000);
			if (i <= 1000 * sunProbability) {
				assigned[idCounter] = idCounter;
				suns.add(n);
			}
			n.setID(idCounter++);
		}

		ArrayList<Node> toBeRemoved = new ArrayList<Node>();

		for (Node n : suns) { // removing suns in direct neighborhood of other suns
			for (Node k : n.getNeighbors()) {
				if (suns.contains(k) && assigned[(int) k.getID()] == k.getID()) {
					if (k.getID() > n.getID()) {
						toBeRemoved.add(n);
						assigned[(int) n.getID()] = -1;
						break;
					} else {
						toBeRemoved.add(k);
						assigned[(int) k.getID()] = -1;
					}

				}
			}
		}

		for (Node n : toBeRemoved) {
			suns.remove(n);
		}
		toBeRemoved.clear();
		for (Node n : suns) { // removing suns with distance 2 to another sun
			for (Node k : n.getNeighbors()) {
				if (childSelection.contains(k)) {
					if (suns.contains(n) && assigned[(int) n.getID()] == n.getID()) {
						for (Node i : k.getNeighbors()) {
							if (childSelection.contains(i)) {
								if (i != n && suns.contains(i) && assigned[(int) i.getID()] == i.getID()) {
									if (i.getID() > n.getID()) {
										toBeRemoved.add(n);
										assigned[(int) n.getID()] = -1;
										break;
									} else {
										toBeRemoved.add(i);
										assigned[(int) i.getID()] = -1;
									}
								}
							}
						}

					}
				}
			}
		}
		for (Node n : toBeRemoved) {
			suns.remove(n);
		}

		for (Node n : suns) {
			for (Node k : n.getNeighbors()) {
				if (childSelection.contains(k)) {
					assigned[(int) k.getID()] = n.getID();
					distanceToSun[(int) k.getID()] = 1;
				}
			}
		}

		int distanceIterator = 1;
		int index = 0;
		while (index < allNodesList.size()) { // assigning planets via broadcast from sun
			int assignedNodes = 0;
			for (Edge e : childSelection.getEdges()) {
				if (childSelection.contains(e.getSource()) && childSelection.contains(e.getTarget())) {
					int sourceID = (int) e.getSource().getID();
					int targetID = (int) e.getTarget().getID();
					if (assigned[sourceID] != -1 && distanceToSun[sourceID] == distanceIterator) {
						if (assigned[targetID] == -1) {
							assigned[targetID] = assigned[sourceID];
							assignedNodes++;
							distanceToSun[targetID] = distanceToSun[sourceID] + 1;
						}
					} else if (assigned[targetID] != -1 && distanceToSun[targetID] == distanceIterator) {
						if (assigned[sourceID] == -1) {
							assigned[sourceID] = assigned[targetID];
							assignedNodes++;
							distanceToSun[sourceID] = distanceToSun[targetID] + 1;
						}
					}
				}
			}
			if (assignedNodes != 0) {
				index += assignedNodes;
			} else {
				index++;
			}
			distanceIterator++;
		}

		ArrayList<Node>[] mergeList = new ArrayList[assigned.length];
		for (int i = 0; i < mergeList.length; i++) {
			mergeList[i] = new ArrayList();
		}
		for (Node n : allNodesList) { // packing nodes to lists
			int nodeID = (int) n.getID();
			if (assigned[nodeID] != -1) {
				if (suns.contains(n)) {
					mergeList[nodeID].add(0, n);
				} else {
					mergeList[(int) assigned[nodeID]].add(n);

				}
			} else {

				mergeList[nodeID].add(0, n);
			}
		}

		for (int i = 0; i < mergeList.length; i++) {// creating parents in parent-graph
			if (!mergeList[i].isEmpty()) {
				createParent(mergeList[i], AttributeHelper.getPosition(mergeList[i].get(0)));

				for (Node m : mergeList[i]) {
					Attribute sun = new SolarMergerSunAttribute(SolarMergerSunAttribute.name, mergeList[i].get(0));
					AttributeHelper.setAttribute(m, SolarMergerSunAttribute.path, SolarMergerSunAttribute.name, sun);
					Attribute s = new SolarDistanceAttribute(SolarDistanceAttribute.name,
							distanceToSun[(int) m.getID()]);
					AttributeHelper.setAttribute(m, SolarDistanceAttribute.path, SolarDistanceAttribute.name, s);
				}
			}
		}
		if (childSelection.getNumberOfNodes() != childGraph.getNumberOfNodes()) {
			createParentNotSelectedNodes();
		}
		createEdges();// adding edges

	}

	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		double probability = ((DoubleParameter) params[0]).getDouble().doubleValue();
		this.sunProbability = probability;
	}

	@Override
	public Parameter[] getParameters() {
		DoubleParameter SunProbability = new DoubleParameter("Sun Probability",
				"Sets the probabilty that a particular node is a sun.");
		SunProbability.setDouble(0.5);
		SunProbability.setMax(1.0);
		SunProbability.setMin(0.01);
		return new Parameter[] { SunProbability };

	}

}
