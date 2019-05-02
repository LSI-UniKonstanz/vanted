/*************************************************************************************
 * The Exemplary Add-on is (c) 2008-2011 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package org.vanted.addons.exampleaddon;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * Algorithms should usually extend {@link AbstractEditorAlgorithm}. Algorithms
 * may be called by
 * <p>
 * <code>
 * GravistoService.getInstance().runAlgorithm(alg)</code>
 * <p>
 * Then everything is done for you: Attaching the graph from the actual {@link EditorSession}, calling the <code>check</code>-method, constructing a parameter
 * dialog to set options (here you have to override the <code>
 * getDescription></code> -method, <code>getParameters</code> and <code>setParameters</code> manually) and
 * executes the algorithm.<br>
 * The "getCategory"- and "getName"- strings are used to sort the algorithm into the menu at the top of vanted. Also you have to specify for which views an
 * algorithm will be active (usually all, which are != null).
 * <p>
 * This algorithm does nothing, except managing your workdays :-)
 * 
 * @author Hendrik Rohn
 */
public class DoNothingAlgorithm extends AbstractEditorAlgorithm {

	private String day = "Monday";
	private View activeview;

	@Override
	public String getDescription() {
		return "<html>Here you can write what<br>the algorithm is doing<p>";
	}

	@Override
	public void check() throws PreconditionException {
		// check, if there is just one node selected
		if (selection.getNumberOfNodes() != 1)
			throw new PreconditionException("Please select exactly one node!");
	}

	/**
	 * For the different types of parameters a special component will be
	 * created, for example using a {@link BooleanParameter} will create a
	 * checkbox, an {@link IntegerParameter} will create a spinner and so on.
	 */
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new ObjectListParameter(day,
							"what is to be entered:", "Set title", new Object[] { "Monday",
												"Tuesday", "Wednesday", "Thursday", "Friday" }) };
	}

	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		day = (String) params[i++].getValue();
	}

	// creates a menuitem
	public String getName() {
		return "Exemplary Algorithm";
	}

	// in which JMenu do you want it, if it is not there it will be
	// automatically constructed
	// if you do not want the command in the menu just return null
	@Override
	public String getCategory() {
		return "Nodes";
	}

	public void execute() {

		// is sometimes useful:
		getSelectedOrAllGraphElements();

		// Allows to set the progress and text of the status-bar-component at
		// the right lower corner
		final BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
							"short Text", "long text");

		// The task to be run, which can use the status-reference to set the
		// status-bar position
		Runnable myTask = new Runnable() {
			public void run() {
				System.out.println(0 + " seconds computed");
				status.setCurrentStatusValue(0);
				for (int i = 1; i < 6; i++) {
					try {
						Thread.sleep(1000);
						System.out.println(i + " seconds computed");
						status.setCurrentStatusValue(20 * i);

						// if the user presses the stop-button you can react on
						// that (here we just stop working)
						if (status.wantsToStop())
							return;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		// the task which will be executed even if the user has stopped the
		// thread
		Runnable myFinishTask = new Runnable() {
			public void run() {
				MainFrame.showMessageDialog("<html>You will work longer on "
									+ day + ".", "Actual View: " + activeview);
			}
		};

		// Here we actually start the task in a new thread with status
		// information
		BackgroundTaskHelper.issueSimpleTask("My task",
							"Computing something...", myTask, myFinishTask, status);

		// if you want to have the statusbar in a dialog (instead at the right
		// lower corner) use this command:
		// BackgroundTaskHelper.issueSimpleTaskInWindow(taskName, progressText,
		// backgroundTask, finishSwingTask, sp, modal, autoclose);

		// just if you want to get the mainframe and create a new Editorsession
		// getMainFrame().createNewSession();

		// if you want to create a new graph from file
		// getMainFrame().loadGraph(file);

	}

	public boolean activeForView(View v) {
		activeview = v;
		return v != null;
	}

	@Override
	public void reset() {
		// shouldn't be commented out, because otherwise the graph-reference
		// will remain and a memory leak occur
		super.reset();
	}

}
