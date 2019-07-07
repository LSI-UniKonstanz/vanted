package org.vanted.addons.MultilevelFramework.Placement;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.AttributeHelper;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.MultilevelFramework.Coarsening.SolarDistanceAttribute;
import org.vanted.addons.MultilevelFramework.Coarsening.SolarMergerSunAttribute;

/**
 * Places the nodes according to a Solar-Placement-Algorithm. only works
 * together with SolarMergerCoarsening, will default to ZeroPlacement in case
 * another Coarsening algorithm is selected.
 */
public class SolarPlacement extends AbstractPlacementAlgorithm {

	@Override
	public String getName() {

		return "Solar Placer";
	}

	@Override
	public String getDescription() {
		return "Designed for use with Solar Merger. Places Nodes according to their intersystem path between suns.";
	}

	@Override
	public void execute() {

		ArrayList<Node> nodeList = new ArrayList<Node>(selection.getNodes());
		HashSet<Node> suns = new HashSet<Node>();
		for (Node n : nodeList) {
			suns.add(getSun(n));// finding suns

		}

		if (!suns.contains(null)) { // if there is a sun than execute solar placement algorithm, else execute null
									// placement algorithm
			ArrayList<ArrayList<Node>> neighborlist = new ArrayList<ArrayList<Node>>();
			HashSet<Node> borderObjects = new HashSet<Node>();
			for (Edge e : selection.getEdges()) {
				if (selection.contains(e.getSource()) && selection.contains(e.getTarget())) {
					if (getSun(e.getTarget()) != getSun(e.getSource())) {
						if (getSun(e.getSource()) != e.getSource() && getSun(e.getTarget()) != e.getTarget()) {// finding
																												// planets
																												// at
																												// the
																												// border
																												// of
																												// their
																												// solar
																												// systems
							ArrayList<Node> list = new ArrayList<Node>();
							list.add(getSun(e.getSource()));
							list.add(e.getSource());
							list.add(e.getTarget());
							list.add(getSun(e.getTarget()));
							neighborlist.add(list);
							borderObjects.add(e.getSource());
							borderObjects.add(e.getTarget());
						}

					}
				}
			}
			HashSet<Node> influencer[] = new HashSet[graph.getNodes().size()];
			for (int i = 0; i < influencer.length; i++) { // creating sets of influencers for calculation of paths
				influencer[i] = new HashSet<Node>();
			}
			for (Node n : borderObjects) {
				for (Node v : n.getNeighbors()) {
					if (getSun(n) == getSun(v)) {
						influencer[(int) v.getID()].add(n);
					} else {

					}
				}
			}

			int iterator = 0;
			int iteratorAlt = -1;
			while (iterator != iteratorAlt) {
				iteratorAlt = iterator;
				for (Edge e : selection.getEdges()) {
					if (selection.contains(e.getSource()) && selection.contains(e.getTarget())) {
						Node source = e.getSource();
						Node target = e.getTarget();
						int sourceID = (int) source.getID();
						int targetID = (int) target.getID();
						if (!(influencer[sourceID].isEmpty()) && target != getSun(source)
								&& getSun(target) == getSun(source)
								&& !influencer[targetID].containsAll(influencer[sourceID])) {
							influencer[targetID].addAll(influencer[sourceID]);
							iterator++;
						}
						if (!(influencer[targetID].isEmpty()) && source != getSun(target)
								&& getSun(target) == getSun(source)
								&& !influencer[sourceID].containsAll(influencer[targetID])) {
							influencer[sourceID].addAll(influencer[targetID]);
							iterator++;
						}
					}
				}
			}

			for (Node n : suns) {// assigning position for suns
				AttributeHelper.setPosition(n, getParentPosition(n));

			}

			for (Node n : borderObjects) { // assigning positions for the borderobjects
				int iterations = 0;
				double posX = 0;
				double posY = 0;
				Point2D sPosition = AttributeHelper.getPosition(getSun(n));
				for (ArrayList<Node> l : neighborlist) {
					if (l.get(1) == n) {
						Node u = l.get(2);
						Point2D tPosition = AttributeHelper.getPosition(getSun(u));
						double path = 1 + getSolarDistance(n) + getSolarDistance(u);
						posX += sPosition.getX() + (getSolarDistance(n) / path) * (tPosition.getX() - sPosition.getX());
						posY += sPosition.getY() + (getSolarDistance(n) / path) * (tPosition.getY() - sPosition.getY());
						iterations++;
					}
					if (l.get(2) == n) {
						Node u = l.get(1);
						Point2D tPosition = AttributeHelper.getPosition(getSun(u));
						double path = 1 + getSolarDistance(n) + getSolarDistance(u);
						posX += sPosition.getX() + (getSolarDistance(n) / path) * (tPosition.getX() - sPosition.getX());
						posY += sPosition.getY() + (getSolarDistance(n) / path) * (tPosition.getY() - sPosition.getY());
						iterations++;
					}
				}
				if (iterations != 0) {
					posX /= iterations;
					posY /= iterations;
					AttributeHelper.setPosition(n, posX, posY);

				}
			}
			for (Node n : nodeList) {// assigning positions for every other node
				if (!suns.contains(n) && !borderObjects.contains(n)) {
					double posX = 0;
					double posY = 0;
					int iterations = 0;
					int desiredEdgeLength = getSolarDistance(n);
					Point2D sPosition = AttributeHelper.getPosition(getSun(n));
					if (influencer[(int) n.getID()].isEmpty()) {// position for nodes not connected to any border-object
						Point2D sunposition = AttributeHelper.getPosition(getSun(n));
						posY = sunposition.getY();
						posX = sunposition.getX();
						iterations = 1;

					}
					for (Node v : influencer[(int) n.getID()]) {// position for nodes connected to a borderobject
						for (ArrayList<Node> l : neighborlist) {
							if (l.get(1) == v) {
								Node u = l.get(2);
								Point2D tPosition = AttributeHelper.getPosition(getSun(u));
								double path = 1 + getSolarDistance(v) + getSolarDistance(u);
								posX += sPosition.getX()
										+ (desiredEdgeLength / path) * (tPosition.getX() - sPosition.getX());
								posY += sPosition.getY()
										+ (desiredEdgeLength / path) * (tPosition.getY() - sPosition.getY());
								iterations++;

							}
							if (l.get(2) == v) {
								Node u = l.get(1);
								Point2D tPosition = AttributeHelper.getPosition(getSun(u));
								double path = 1 + getSolarDistance(v) + getSolarDistance(u);
								posX += sPosition.getX()
										+ (desiredEdgeLength / path) * (tPosition.getX() - sPosition.getX());
								posY += sPosition.getY()
										+ (desiredEdgeLength / path) * (tPosition.getY() - sPosition.getY());
								iterations++;

							}
						}
					}
					if (iterations != 0) {
						posX /= iterations;
						posY /= iterations;
						Random offset = new Random();
						posX = posX + (offset.nextDouble() * 10);
						posY = posY + (offset.nextDouble() * 10);
						AttributeHelper.setPosition(n, posX, posY);

					}
				}
			}
		} else {// zero placement in case SolarMergerCoarsenig was not used
			ZeroPlacementAlgorithm alg = new ZeroPlacementAlgorithm();
			alg.attach(graph, selection);
			DoubleParameter offset = new DoubleParameter(0.01, 0.0, "Max Offset", "Max Offset in each direction");
			alg.setParameters(new Parameter[] { offset });
			alg.execute();
		}
	}

	/**
	 * finds the sun for a node
	 * 
	 * @param n the node whos sun is needed
	 * @return the sun of node n
	 */
	public Node getSun(Node n) {
		try {
			return (Node) n.getAttribute(SolarMergerSunAttribute.fullpath).getValue();
		} catch (AttributeNotFoundException e) {
			return null;
		}
	}

	/**
	 * hop-distance of a node to its sun
	 * 
	 * @param n the node whos distance is required
	 * @return the hop-distance of node n to its sun
	 */
	public int getSolarDistance(Node n) {
		return (int) n.getAttribute(SolarDistanceAttribute.fullpath).getValue();
	}

}
