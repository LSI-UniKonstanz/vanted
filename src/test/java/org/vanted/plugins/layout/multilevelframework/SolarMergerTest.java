package org.vanted.plugins.layout.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.vanted.plugins.layout.multilevelframework.SolarMerger.MAX_LEVEL_FACTOR_NAME;
import static org.vanted.plugins.layout.multilevelframework.SolarMerger.MIN_NODES_NAME;

/**
 * @see SolarMerger
 * @author Tobias
 */
public class SolarMergerTest {
	SolarMerger sM;
	Graph g1, g2;
	MultilevelGraph mg1, mg2;
	Node n01, n02, n03, n04, n05, n06, n07, n08, n09, n10, n11, n12, n13;
	
	@Before
	public void setUp() {
		this.sM = new SolarMerger();
		g1 = new AdjListGraph();
		g2 = new AdjListGraph();
		
		n01 = g1.addNode();
		n02 = g1.addNode();
		n03 = g1.addNode();
		n04 = g1.addNode();
		for (Node n : g1.getNodes()) {
			AttributeHelper.setPosition(n, 0, 0);
		}
		g1.addEdge(n01, n02, false);
		g1.addEdge(n03, n02, false);
		g1.addEdge(n04, n02, false);
		
		mg1 = new MultilevelGraph(g1);
		
		n01 = g2.addNode();
		n02 = g2.addNode();
		n03 = g2.addNode();
		n04 = g2.addNode();
		n05 = g2.addNode();
		n06 = g2.addNode();
		n07 = g2.addNode();
		n08 = g2.addNode();
		n09 = g2.addNode();
		n10 = g2.addNode();
		n11 = g2.addNode();
		n12 = g2.addNode();
		n13 = g2.addNode();
		
		for (Node n : g1.getNodes()) {
			AttributeHelper.setPosition(n, 0, 0);
		}
		g2.addEdge(n01, n02, false);
		g2.addEdge(n02, n03, false);
		g2.addEdge(n03, n04, false);
		g2.addEdge(n04, n05, false);
		g2.addEdge(n01, n06, false);
		g2.addEdge(n06, n07, false);
		g2.addEdge(n07, n08, false);
		g2.addEdge(n08, n09, false);
		g2.addEdge(n01, n10, false);
		g2.addEdge(n10, n11, false);
		g2.addEdge(n11, n12, false);
		g2.addEdge(n12, n13, false);
		
		for (Node n : g2.getNodes()) {
			AttributeHelper.setPosition(n, 0, 0);
		}
		
		mg2 = new MultilevelGraph(g2);
	}
	
	@Test
	public void buildCoarseningLevels1() {
		sM.minNodes = 2;
		sM.maxLevelFactor = 1;
		this.sM.buildCoarseningLevels(mg1);
	}
	
	@Test
	public void buildCoarseningLevels2() {
		MultilevelGraph untouchedMg1 = mg1;
		sM.minNodes = 0;
		this.sM.buildCoarseningLevels(mg1);
		assertTrue(untouchedMg1 == mg1);
	}
	
	@Test
	public void buildCoarseningLevels3() {
		MultilevelGraph untouchedMg1 = mg1;
		sM.maxLevelFactor = Integer.MAX_VALUE;
		this.sM.buildCoarseningLevels(mg1);
		assertTrue(untouchedMg1 == mg1);
	}
	
	@Test
	public void buildCoarseningLevels4() {
		MultilevelGraph mg0 = new MultilevelGraph(new AdjListGraph());
		MultilevelGraph untouchedMg1 = mg0;
		sM.minNodes = 0;
		this.sM.buildCoarseningLevels(mg0);
		assertTrue(untouchedMg1 == mg0);
	}
	
	@Test
	public void buildCoarseningLevels5() {
		sM.minNodes = 2;
		sM.maxLevelFactor = 1;
		this.sM.buildCoarseningLevels(mg2);
	}
	
	/**
	 * @author Tobias copyPasta credits to Gordian
	 */
	@Test(expected = IllegalStateException.class)
	public void setParametersFail() {
		Parameter[] parameters = { new BooleanParameter(false, "Invalid parameter name blah blah",
				"Super duper great description of an invalid parameter.") };
		sM.setParameters(parameters);
	}
	
	/**
	 * @author Tobias credits to Gordian
	 */
	@Test
	public void getNameAndDescription() {
		assertTrue(sM.getName().toLowerCase().contains("solar"));
		assertTrue(sM.getName().toLowerCase().contains("merger"));
		assertTrue(sM.getDescription().toLowerCase().contains("solar"));
	}
	
	/**
	 * @author Tobias credits to Gordian
	 */
	@Test
	public void setParameters() {
		Parameter[] parameters = sM.getParameters();
		assertNotNull(parameters);
		for (Parameter parameter : parameters) {
			switch (parameter.getName()) {
				case MAX_LEVEL_FACTOR_NAME: {
					((IntegerParameter) parameter).setValue(1337);
					break;
				}
				case MIN_NODES_NAME: {
					((IntegerParameter) parameter).setValue(1337);;
					break;
				}
				
			}
		}
		sM.setParameters(parameters);
		for (Parameter parameter : parameters) {
			switch (parameter.getName()) {
				case MAX_LEVEL_FACTOR_NAME: {
					assertEquals(1337, (int) ((IntegerParameter) parameter).getInteger());
					break;
				}
				case MIN_NODES_NAME: {
					assertEquals(1337, (int) ((IntegerParameter) parameter).getInteger());
					break;
					
				}
			}
		}
	}
}
