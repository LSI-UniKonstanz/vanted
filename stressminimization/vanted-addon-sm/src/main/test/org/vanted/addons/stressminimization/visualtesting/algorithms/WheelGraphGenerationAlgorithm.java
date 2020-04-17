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
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.addons.stressminimization.benchmark.GraphGeneration;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * Generates wheel graphs with a certain number of nodes
 */
public class WheelGraphGenerationAlgorithm extends AbstractAlgorithm {

	private int numberOfNodes = 10;

	@Override
	public String getCategory() {
		return "File.New.Generate";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.COMPUTATION));
	}

	@Override
	public Parameter[] getParameters() {
		return new Parameter[]{new IntegerParameter(numberOfNodes, "Number of nodes", "Number of nodes")};
	}

	@Override
	public void setParameters(Parameter[] params) {
		numberOfNodes = ((IntegerParameter) params[0]).getInteger();
	}

	@Override
	public void check() throws PreconditionException {
		if (numberOfNodes < 1) {
			throw new PreconditionException("Number of nodes needs to be at least 1");
		}
	}

	@Override
	public String getName() {
		return "Generate wheel graphs";
	}

	@Override
	public String getDescription() {
		return "Generate a wheel graph with a certain number of nodes";
	}

	@Override
	public boolean isAlwaysExecutable() {
		return true;
	}

	@Override
	public void execute() {

		BackgroundTaskHelper.issueSimpleTask("Generating wheel graph", "Generating wheel graph", new Runnable() {
			@Override
			public void run() {
				try {
					GraphGeneration gen = new GraphGeneration();
					Graph graph = gen.generateWheel(numberOfNodes);

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
