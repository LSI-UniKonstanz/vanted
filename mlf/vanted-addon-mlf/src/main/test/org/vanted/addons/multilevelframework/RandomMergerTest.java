package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation.ErdosRenyiGraphGenerator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation.WattsStrogatzGraphGenerator;
import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.*;
import org.junit.Before;
import org.junit.Test;
import org.vanted.addons.multilevelframework.sm_util.gui.Parameterizable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

import static org.vanted.addons.multilevelframework.RandomMerger.*;

/**
 * @see RandomMerger
 * @author Tobias
 */
public class RandomMergerTest {
    RandomMerger rM;
    Graph g1,g2;
    MultilevelGraph mg1,mg2;
    Node n1, n2, n3, n4;

    @Before
    public void setUp(){
        this.rM = new RandomMerger();

        g1 = new AdjListGraph();
        g2 = new AdjListGraph();

        n1 = g1.addNode();
        n2 = g1.addNode();
        n3 = g1.addNode();
        n4 = g1.addNode();
        for (Node n : g1.getNodes()) {
            AttributeHelper.setPosition(n, 0, 0);
        }
        g1.addEdge(n1,n2,false);
        g1.addEdge(n3,n2,false);
        g1.addEdge(n4,n2,false);

        mg1 = new MultilevelGraph(g1);

        n1 = g2.addNode();
        n2 = g2.addNode();
        for (Node n : g2.getNodes()) {
            AttributeHelper.setPosition(n, 0, 0);
        }

        mg2 = new MultilevelGraph(g2);

    }

    /**
     * @author Gordian
     */
    @Test
    public void testEdgeWeightSorting() {
        RandomMerger rm = new RandomMerger();
        String path = rm.weightAttributePath;
        rm.considerEdgeWeights = true;
        rm.minNumberOfNodesPerLevel = 3;
        AdjListGraph dummy = new AdjListGraph();
        Node n1 = dummy.addNode();
        Node n2 = dummy.addNode();
        Node n3 = dummy.addNode();
        Node n4 = dummy.addNode();
        for (Node n : dummy.getNodes()) {
            AttributeHelper.setPosition(n, 0, 0);
        }
        Edge e1 = dummy.addEdge(n1, n2, false);
        Edge e2 = dummy.addEdge(n3, n4, false);
        e1.setDouble(path, 100);
        e2.setDouble(path, -1);
        MultilevelGraph mlg = new MultilevelGraph(dummy);
        rm.buildCoarseningLevels(mlg);
        for (MergedNode mn : mlg.popCoarseningLevel().getMergedNodes()) {
            assertTrue(mn.getInnerNodes().size() == 1 ||
                    mn.getInnerNodes().containsAll(Arrays.asList(n3, n4)));
        }
    }


    @Test
    public void buildCoarseningLevels1(){
        this.rM.buildCoarseningLevels(mg1);
        assertTrue(mg1.isComplete());
    }

    @Test
    public void buildCoarseningLevels2(){
        rM.maxNumberOfIterations = 0;
        Graph oldmg1 = mg1.getTopLevel();
        this.rM.buildCoarseningLevels(mg1);
        assertTrue(mg1.getTopLevel() == oldmg1);
    }

    @Test
    public void buildCoarseningLevels3() {
        this.rM.buildCoarseningLevels(mg2);
        assertTrue(mg2.isComplete());
    }

    @Test
    public void buildCoarseningLevels4(){
        rM.minNumberOfNodesPerLevel = 10000;
        Graph oldmg1 = mg1.getTopLevel();
        this.rM.buildCoarseningLevels(mg1);
        assertTrue(mg1.getTopLevel() == oldmg1);
    }

    @Test
    public void buildCoarseningLevels5(){
        rM.coarseningFactor = 0;
        Graph oldmg1 = mg1.getTopLevel();
        this.rM.buildCoarseningLevels(mg1);
        assertTrue(mg1.getTopLevel() == oldmg1);
    }

    @Test
    public void buildCoarseningLevels6(){
        rM.coarseningFactor = 1;
        Graph oldmg1 = mg1.getTopLevel();
        this.rM.buildCoarseningLevels(mg1);
        assertTrue(mg1.getTopLevel() == oldmg1);
    }

    @Test
    public void setParameters() {
        Parameter[] parameters = rM.getParameters();
        assertNotNull(parameters);
        for (Parameter parameter : parameters) {
            switch (parameter.getName()) {
                case COARSENING_FACTOR_NAME: {
                    ((DoubleParameter) parameter).setDouble(0.1337);
                    break;
                }
                case MIN_LEVEL_NODE_NUM_NAME: {
                    ((IntegerParameter) parameter).setValue(1337);;
                    break;
                }
                case MAX_NAM_ITERATIONS_NAME: {
                    ((IntegerParameter) parameter).setValue(13337);
                    break;
                }
                case USE_WEIGHTS_NAME: {
                    ((BooleanParameter) parameter).setValue(false);
                    break;
                }
                case CONSIDER_EDGE_WEIGHTS_NAME: {
                    ((BooleanParameter) parameter).setValue(true);
                    break;
                }
                case WEIGHT_ATTR_PATH_NAME: {
                    ((StringParameter) parameter).setValue("le string");
                    break;
                }
            }
        }
        rM.setParameters(parameters);
        for (Parameter parameter : parameters) {
            switch (parameter.getName()) {
                case COARSENING_FACTOR_NAME: {
                    assertEquals(0.1337, ((DoubleParameter) parameter).getDouble(), 0.000001);
                    break;
                }
                case MIN_LEVEL_NODE_NUM_NAME: {
                    assertEquals(1337, (int) ((IntegerParameter) parameter).getInteger());
                    break;
                }
                case MAX_NAM_ITERATIONS_NAME: {
                    assertEquals(13337, (int) ((IntegerParameter) parameter).getInteger());
                    break;
                }
                case USE_WEIGHTS_NAME: {
                    assertEquals(false, ((BooleanParameter) parameter).getBoolean());
                    break;
                }
                case CONSIDER_EDGE_WEIGHTS_NAME: {
                    assertEquals(true, ((BooleanParameter) parameter).getBoolean());
                    break;
                }
                case WEIGHT_ATTR_PATH_NAME: {
                    assertEquals("le string", ((StringParameter) parameter).getString());
                    break;
                }
            }
        }
    }

    /**
     * @author Gordian
     */
    @Test(expected = IllegalStateException.class)
    public void setParametersFail() {
        Parameter[] parameters = {new BooleanParameter(false, "Invalid parameter name blah blah",
                "Super duper great description of an invalid parameter.")};
        rM.setParameters(parameters);
    }

    /**
     * @author Gordian
     */
    @Test
    public void getNameAndDescription() {
        assertTrue(rM.getName().toLowerCase().contains("random"));
        assertTrue(rM.getName().toLowerCase().contains("merger"));
        assertTrue(rM.getDescription().toLowerCase().contains("random"));
    }

}
