package org.vanted.plugins.layout.multilevelframework;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class RandomPlacerTest {
	private Graph alg;
	private Node n1, n2, n3, n4;
	private RandomPlacer rp;
	private MultilevelGraph mlg;
	
	/**
	 * @author Katze
	 */
	@Before
	public void setUp() throws Exception {
		rp = new RandomPlacer();
		
		this.alg = new AdjListGraph();
		this.n1 = alg.addNode();
		this.n2 = alg.addNode();
		this.n3 = alg.addNode();
		this.n4 = alg.addNode();
		//Labels
		AttributeHelper.setLabel(n1, "1");
		AttributeHelper.setLabel(n2, "2");
		AttributeHelper.setLabel(n3, "3");
		AttributeHelper.setLabel(n4, "4");
		//Positions
		AttributeHelper.setPosition(n1, 100, 100);
		AttributeHelper.setPosition(n2, 100, 100);
		AttributeHelper.setPosition(n3, 300, 300);
		AttributeHelper.setPosition(n4, 300, 300);
		//Build MLG
		mlg = new MultilevelGraph(alg);
		mlg.newCoarseningLevel();
		MergedNode mn1 = mlg.addNode(new HashSet<>(Arrays.asList(n1, n2)));
		MergedNode mn2 = mlg.addNode(new HashSet<>(Arrays.asList(n3, n4)));
		mlg.addEdge(mn1, mn2);
	}
	
	/**
	 * @author Katze
	 */
	@Test
	public void testPosChange() {
		Vector2d[] oldpos = { AttributeHelper.getPositionVec2d(n1),
				AttributeHelper.getPositionVec2d(n2),
				AttributeHelper.getPositionVec2d(n3),
				AttributeHelper.getPositionVec2d(n4) };
		rp.reduceCoarseningLevel(mlg);
		assertFalse(oldpos[0].distance(AttributeHelper.getPositionVec2d(n1)) == 0.0);
		assertFalse(oldpos[1].distance(AttributeHelper.getPositionVec2d(n2)) == 0.0);
		assertFalse(oldpos[2].distance(AttributeHelper.getPositionVec2d(n3)) == 0.0);
		assertFalse(oldpos[3].distance(AttributeHelper.getPositionVec2d(n4)) == 0.0);
	}
	
	/**
	 * @author Katze, Gordian
	 */
	@Test
	public void testParameters() {
		Parameter[] parameters = rp.getParameters();
		assertNotNull(parameters);
		int i = 0;
		for (Parameter parameter : parameters) {
			if (parameter.getName().equals(RandomPlacer.MAX_PLACE_DIST_NAME)) {
				parameter.setValue(1234.5);
				break;
			}
			i++;
		}
		rp.setParameters(parameters);
		assertEquals(1234.5, ((DoubleParameter) parameters[i]).getDouble(), 0.000001);
	}
	
	/**
	 * @author Gordian
	 */
	@Test
	public void getNameAndDescription() {
		assertTrue(rp.getName().toLowerCase().contains("random"));
		assertTrue(rp.getName().toLowerCase().contains("placer"));
		assertTrue(rp.getDescription().toLowerCase().contains("random"));
	}
}
