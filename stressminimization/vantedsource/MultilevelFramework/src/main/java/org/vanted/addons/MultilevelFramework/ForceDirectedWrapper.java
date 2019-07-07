package org.vanted.addons.MultilevelFramework;

import java.util.Set;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.ThreadSafeAlgorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyNonInteractiveSpringEmb;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.myOp;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * Wrapper class to enable usage of the PatternspringEmbedder as part of the
 * Multilevel Framework. This class solves two problems. First of all it makes
 * all relevant Parameters of the available, some of which are not included in
 * getParameters()/setParameters() in the original PatternspringEmbedder. Second
 * of all it waits for the layouting to finish before returning in the execute
 * method. Otherwise the multilevel framework would proceed to placing the next
 * level with an unfinished layout as input.
 */
public class ForceDirectedWrapper extends AbstractEditorAlgorithm {

	ThreadSafeAlgorithm forceDirected;

	private double zeroLength;
	private double stiffness;
	private double horForce;
	private double vertForce;
	private boolean considerNodeDegree;
	private int progress;
	private boolean borderForce;
	private boolean gridForce;
	private boolean nodeOverlap;

	public ForceDirectedWrapper() {
		forceDirected = new PatternSpringembedder();
	}

	@Override
	public boolean activeForView(View v) {
		return false;
	}

	@Override
	public String getName() {
		return forceDirected.getName();
	}

	@Override
	public void execute() {
		ThreadSafeOptions options = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
		options.temp_alpha = 0.98;// not sure why this is helpful but patternspringembedder does it too
		// The class ThreadSafeOptions is a horrible mess!
		options.setDval(myOp.DvalIndexSliderZeroLength, zeroLength);
		options.setDval(myOp.DvalIndexSliderStiffness, stiffness);
		options.setDval(myOp.DvalIndexSliderHorForce, horForce);
		options.setDval(myOp.DvalIndexSliderVertForce, vertForce);
		options.doMultiplyByNodeDegree = considerNodeDegree;
		options.temperature_max_move = progress;
		options.borderForce = borderForce;
		options.setBval(6, gridForce);
		options.doFinishRemoveOverlapp = nodeOverlap;

		MyNonInteractiveSpringEmb mse = new MyNonInteractiveSpringEmb(graph, selection, options);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(mse, mse, forceDirected.getName(), forceDirected.getName(),
				true, false);
		bth.startWork(forceDirected);
		while (BackgroundTaskHelper.isTaskWithGivenReferenceRunning(forceDirected)) {
			// no alternatives to busy waiting possible without changing some of VANTEDs
			// preexisting classes
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {

			}
		}

	}

	public void setParameters(Parameter[] params) {
		forceDirected.setParameters(new Parameter[] { params[0], params[2], params[3] });
		zeroLength = ((DoubleParameter) params[0]).getDouble().doubleValue();
		stiffness = ((DoubleParameter) params[1]).getDouble().doubleValue();
		horForce = ((DoubleParameter) params[2]).getDouble().doubleValue();
		vertForce = ((DoubleParameter) params[3]).getDouble().doubleValue();
		considerNodeDegree = ((BooleanParameter) params[4]).getBoolean().booleanValue();
		progress = ((IntegerParameter) params[5]).getInteger();
		borderForce = ((BooleanParameter) params[6]).getBoolean().booleanValue();
		gridForce = ((BooleanParameter) params[7]).getBoolean().booleanValue();
		nodeOverlap = ((BooleanParameter) params[8]).getBoolean().booleanValue();
	}

	public Parameter[] getParameters() {
		Parameter[] basicParameters = forceDirected.getParameters(); // does not include all parameters!
		((DoubleParameter) basicParameters[0]).setDouble(100);
		// so we need to include some more:

		DoubleParameter stiffnessParameter = new DoubleParameter(10.0, 0.0, 100.0, "Stiffness",
				"Modifies the forces determined by connections to other nodes (edge target length).");
		BooleanParameter nodeDegreeParameter = new BooleanParameter(true, "Consider node degree",
				"If enabled, the repulsive forces of a node to the remaining nodes are multiplied by its number of connections to other nodes. "
						+ "Highly connected nodes will get more room.");
		IntegerParameter progressParameter = new IntegerParameter(300, 0, 1000, "Progress",
				"Adjust this value to decrease or increase the run-time of the algorithm."
						+ "This value determines the maximum node movement during one layout-loop run.");
		BooleanParameter borderForceParameter = new BooleanParameter(false, "Border Force",
				"If selected, a force will be added, which lets the nodes move slowly to the top left. "
						+ "The nodes will avoid movement towards negative coordinates.");
		BooleanParameter gridForceParameter = new BooleanParameter(false, "Finish: Grid Force", "");
		BooleanParameter nodeOverlapParameter = new BooleanParameter(false, "Finish: Remove Node Overlaps",
				"If selected, the final layout will be modified to remove any node overlaps");

		return new Parameter[] { basicParameters[0], stiffnessParameter, basicParameters[1], basicParameters[2],
				nodeDegreeParameter, progressParameter, borderForceParameter, gridForceParameter,
				nodeOverlapParameter };
	}

	public void attach(Graph g, Selection s) {
		graph = g;
		selection = s;
		forceDirected.attach(g, s);
	}

	public void reset() {
		forceDirected.reset();
	}

	public String getCategory() {
		return forceDirected.getCategory();
	}

	@Override
	public Set<Category> getSetCategory() {
		return forceDirected.getSetCategory();
	}

	@Override
	public String getMenuCategory() {
		return forceDirected.getMenuCategory();
	}

	public boolean isLayoutAlgorithm() {
		return forceDirected.isLayoutAlgorithm();
	}

	public String getDescription() {
		return forceDirected.getDescription();
	}

}
