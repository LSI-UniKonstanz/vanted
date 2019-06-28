package org.vanted.addons.stressminimization.visualtesting.algorithms;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.stressminimization.benchmark.GraphGeneration;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * Generates barabasi albert networks
 */
public class BarabasiAlbertNetworkGenerationAlgorithm extends AbstractAlgorithm {

	@Override
	public String getCategory() {
		return "File.New.Random Network";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.COMPUTATION));
	}

	private int numberOfNodes = 10;
	private int initialComponentSize = 3;
	private int desiredDegree = 3;
	private double degreeDistribution = 1.0;

	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
				new IntegerParameter(numberOfNodes, "Number of nodes", "Number of nodes"),
				new IntegerParameter(initialComponentSize, "Size of the initial component", "Size of the initial component"),
				new IntegerParameter(desiredDegree, "Desired mean degree", "Desired degree of each node"),
				new DoubleParameter(degreeDistribution, "Controll the degree distribution", "Controll the degree distribution")
		};
	}

	@Override
	public void setParameters(Parameter[] params) {
		numberOfNodes = ((IntegerParameter) params[0]).getInteger();
		initialComponentSize = ((IntegerParameter) params[1]).getInteger();
		desiredDegree = ((IntegerParameter) params[2]).getInteger();
		degreeDistribution = ((DoubleParameter) params[3]).getDouble();
	}

	@Override
	public void check() throws PreconditionException {
		if (numberOfNodes < 1) {
			throw new PreconditionException("Number of nodes needs to be at least 1");
		}
		if (initialComponentSize < 1 || initialComponentSize > numberOfNodes) {
			throw new PreconditionException("Choose Initial Components Size between 1 and number of nodes.");
		}
		if (desiredDegree > initialComponentSize) {
			throw new PreconditionException("Desired degree may not be larger than the initial component size");
		}
		if (degreeDistribution < 0.0) {
			throw new PreconditionException("Mean Degree distribution paramater may not be < 0");
		}
	}

	@Override
	public String getName() {
		return "Generate barabasi albert networks";
	}

	@Override
	public String getDescription() {
		return "Generate barabasi albert networks with certain parameters";
	}

	@Override
	public boolean isAlwaysExecutable() {
		return true;
	}

	@Override
	public void execute() {

		BackgroundTaskHelper.issueSimpleTask("Generating random barabasi albert network", "Generating random barabasi albert networkh", new Runnable() {
			@Override
			public void run() {
				try {
					GraphGeneration gen = new GraphGeneration();
					Graph graph = gen.generateBarabasiAlbertNetwork(numberOfNodes, initialComponentSize, desiredDegree, degreeDistribution);

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							MainFrame.getInstance().showGraph(graph, new ActionEvent(this, 1, getName()));
							GraphHelper.issueCompleteRedrawForActiveView();
						}
					});
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}, null);
	}

}
