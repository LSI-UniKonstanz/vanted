package org.vanted.addons.multilevelframework;

import java.awt.event.ActionEvent;
import java.util.Set;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.*;
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

import javax.swing.*;

/**
 * Wrapper class to enable usage of the PatternspringEmbedder (a.k.a. force-directed layout)
 * provided in the VANTED core as part of the Multilevel Framework.
 * <p>
 * This class provides two things:
 * <ol>
 *     <li>It waits for the layouting to finish before returning in the execute
 *          method (busy waiting). Otherwise, the multilevel framework would proceed to placing the
 *          next level with an unfinished layout as input.</li>
 *     <li>it makes all relevant Parameters of the available, some of which are not included in
 *          getParameters()/setParameters() in the original PatternspringEmbedder.</li>
 * </ol>
 *
 * @see LayoutAlgorithmWrapper#layoutAlgWhitelist
 */
public class ForceDirectedLayoutWrapper extends ThreadSafeAlgorithm {

    ThreadSafeAlgorithm forceDirectedAlg;

    // populated by setParameters in case we do not use the threadSafeOptions variant
    private double zeroLength;
    private double stiffness;
    private double horForce;
    private double vertForce;
    private boolean considerNodeDegree;
    private int progress;
    private boolean borderForce;
    private boolean gridForce;
    private boolean nodeOverlap;
    private ActionEvent actionEvent;

    public ForceDirectedLayoutWrapper() {
        forceDirectedAlg = new PatternSpringembedder();
    }

    @Override
    public String getName() {
        return forceDirectedAlg.getName();
    }

    private void executeWithOptions(ThreadSafeOptions options) {
        options.temp_alpha = 0.98;// not sure why this is helpful but patternspringembedder does it too

        MyNonInteractiveSpringEmb mse = new MyNonInteractiveSpringEmb(
                options.getGraphInstance(),
                options.getSelection(),
                options);
        BackgroundTaskHelper bth = new BackgroundTaskHelper(mse, mse, forceDirectedAlg.getName(), forceDirectedAlg.getName(),
                true, false);
        bth.startWork(forceDirectedAlg);
        while (BackgroundTaskHelper.isTaskWithGivenReferenceRunning(forceDirectedAlg)) {
            // no alternatives to busy waiting possible without changing some of VANTEDs
            // preexisting classes
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }

    @Override
    public void execute() {
        ThreadSafeOptions options = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
        // set additional parameters not covered by forceDirectedAlg.getParameters, .setParameters
        // (which would populate the ThreadSafeOptions with the values of the parameteres)
        options.setDval(myOp.DvalIndexSliderZeroLength, zeroLength);
        options.setDval(myOp.DvalIndexSliderStiffness, stiffness);
        options.setDval(myOp.DvalIndexSliderHorForce, horForce);
        options.setDval(myOp.DvalIndexSliderVertForce, vertForce);
        options.doMultiplyByNodeDegree = considerNodeDegree;
        options.temperature_max_move = progress;
        options.borderForce = borderForce;
        options.setBval(6, gridForce);
        options.doFinishRemoveOverlapp = nodeOverlap;
        this.executeWithOptions(options);
    }

    public Parameter[] getParameters() {
        Parameter[] basicParameters = forceDirectedAlg.getParameters(); // does not include all parameters!
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

        return new Parameter[]{basicParameters[0], stiffnessParameter, basicParameters[1], basicParameters[2],
                nodeDegreeParameter, progressParameter, borderForceParameter, gridForceParameter,
                nodeOverlapParameter};
    }

    public void setParameters(Parameter[] params) {
        forceDirectedAlg.setParameters(new Parameter[]{params[0], params[2], params[3]});
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

    public void attach(Graph g, Selection s) {
        forceDirectedAlg.attach(g, s);
    }

    @Override
    public void check() throws PreconditionException {

    }

    public void reset() {
        forceDirectedAlg.reset();
    }

    public String getCategory() {
        return forceDirectedAlg.getCategory();
    }

    @Override
    public Set<Category> getSetCategory() {
        return forceDirectedAlg.getSetCategory();
    }

    @Override
    public String getMenuCategory() {
        return forceDirectedAlg.getMenuCategory();
    }

    public boolean isLayoutAlgorithm() {
        return forceDirectedAlg.isLayoutAlgorithm();
    }

    public String getDescription() {
        return forceDirectedAlg.getDescription();
    }

    @Override
    public ActionEvent getActionEvent() {
        return this.actionEvent;
    }

    @Override
    public void setActionEvent(ActionEvent a) {
        this.actionEvent = a;
    }

    @Override
    public boolean setControlInterface(ThreadSafeOptions options, JComponent jc) {
        return this.forceDirectedAlg.setControlInterface(options, jc);
    }

    @Override
    public void executeThreadSafe(ThreadSafeOptions options) {
        this.executeWithOptions(options);
    }

    @Override
    public void resetDataCache(ThreadSafeOptions options) {
        // todo
    }
}
