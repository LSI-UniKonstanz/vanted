package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.CircleLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * Algorithm to generate an Erdos Renyi random network of n nodes, using p edge
 * probability.
 * 
 * @version 2.6.5
 * @vanted.revision 2.6.5
 */
public class ErdosRenyiGraphGenerator extends AbstractAlgorithm {
	
	@Override
	public String getCategory() {
		return "File.New.Random Network";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.COMPUTATION));
	}
	
	private int numberOfNodes = 5;
	private double p = 0.5;
	private boolean label = true;
	private boolean directed = false;
	private boolean selfLoops = false;
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new IntegerParameter(numberOfNodes, "Number of nodes", "Number of nodes"),
				new DoubleParameter(p, 0d, 1d, .05, "Edge probability", "Edge-creation probability"),
				new BooleanParameter(label, "Add node label", "If enabled, each node will be labeld (1,2,3,...)"),
				new BooleanParameter(directed, "Create directed graph", "If enabled, a directed network is created"),
				new BooleanParameter(selfLoops, "Create self loops", "If enabled, self loops are created") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		numberOfNodes = ((IntegerParameter) params[i++]).getInteger();
		p = ((DoubleParameter) params[i++]).getDouble();
		label = ((BooleanParameter) params[i++]).getBoolean();
		directed = ((BooleanParameter) params[i++]).getBoolean();
		selfLoops = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	@Override
	public void check() throws PreconditionException {
		if (numberOfNodes < 1)
			throw new PreconditionException("Number of nodes needs to be at least 1");
	}
	
	@Override
	public String getName() {
		return "Generate Erdos-Renyi random network";
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "Using the G(n, p) model, a network of size n is constructed by connecting <br>"
				+ "its nodes randomly. Each edge is included in the network with probability p.";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK);
	}
	
	@Override
	public void execute() {
		BackgroundTaskHelper.issueSimpleTask("Generating random network", "Generating random network", new Runnable() {
			@Override
			public void run() {
				try {
					final Graph rdg = createGraph(numberOfNodes, label, p, directed, selfLoops);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							MainFrame.getInstance().showGraph(rdg, new ActionEvent(this, 1, getName()));
							GraphHelper.issueCompleteRedrawForActiveView();
						}
					});
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				} catch (OutOfMemoryError e) {
					ErrorMsg.addErrorMessage(
							"Out of memory! Please choose to create a smaller network or increase memory of Java VM!");
				}
			}
		}, null);
		
	}
	
	public static Graph createGraph(int numberOfNodes, boolean label, double p, boolean directed, boolean selfLoops) {
		Graph rdg = new AdjListGraph();
		
		rdg.getListenerManager().transactionStarted(rdg);
		try {
			ArrayList<Node> nodes = new ArrayList<Node>();
			PositionGridGenerator pgg = new PositionGridGenerator(50, 50, 800);
			for (int i = 0; i < numberOfNodes; i++) {
				Node n = rdg.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pgg.getNextPositionVec2d()));
				AttributeHelper.setShapeEllipse(n);
				nodes.add(n);
				
				if (label)
					AttributeHelper.setLabel(n, "" + (i + 1));
			}
			for (int i = 0; i < numberOfNodes; i++) {
				Node a = nodes.get(i);
				for (int j = 0; j < numberOfNodes; j++) {
					if (!selfLoops && (i == j))
						continue;
					if (!directed && i > j)
						continue;
					Node b = nodes.get(j);
					double r = Math.random();
					if (r <= p) {
						rdg.addEdge(a, b, directed,
								AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, directed));
					}
				}
			}
			CircleLayouterAlgorithm ca = new CircleLayouterAlgorithm(400);
			ca.attach(rdg, new Selection("empty"));
			ca.execute();
			CenterLayouterAlgorithm ctr = new CenterLayouterAlgorithm();
			ctr.attach(rdg, new Selection("empty"));
			ctr.execute();
		} finally {
			rdg.getListenerManager().transactionFinished(rdg);
		}
		return rdg;
	}
	
	@Override
	public boolean isAlwaysExecutable() {
		return true;
	}
	
}
