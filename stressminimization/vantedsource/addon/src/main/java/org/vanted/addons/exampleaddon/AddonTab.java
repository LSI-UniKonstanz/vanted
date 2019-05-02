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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.FolderPanel;
import org.FolderPanel.Iconsize;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.GraphListener;
import org.graffiti.event.ListenerNotFoundException;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;

/**
 * Declares a Tab in the sidepanel of Vanted. As you can see it implements
 * several Listeners.
 * Listeners are part of the "Observer" design pattern (see literature), where
 * you have an Observer (in Vanted usually called "*Manager" oder
 * "*ListenerManager") and some observed objects (usually called "*Listener".
 * The manager usually is written with an "Singleton" design pattern, which
 * guarantees, that exactly just one object of this class exists and you have
 * static access to it.
 * The manager gets informed, if something happens and forwards this event to
 * all registered listeners. These listeners (observed objects) have to
 * implement several methods, stating what to do with such kind of events.
 * Registering usually is situated in the constructor of the listener object. {@link ViewListener} can be registered with
 * <code>MainFrame.getInstance().addViewListener(this)</code> and then the
 * mainframe will notice this object every time the "viewChanged". {@link GraphListener} will listen to events like "node added" or similiar and
 * are registered with <code>graph.getListenerManager().addDelayedGraphListener(this);</code>. {@link SessionListener} will be noticed if a session changed.
 * In Vanted a {@link Session} or {@link EditorSession} is directly linked to
 * exactly one {@link Graph} and vice versa. A {@link Session} can have many {@link View}s. A graph holds obviously the graph-structure (nodes, edges and
 * their attributes), whereas a view generates a graphical representation of
 * this graph. The session can be seen as a mediator between both.
 * 
 * @author Hendrik Rohn
 */
public class AddonTab extends InspectorTab
					implements
					ViewListener,
					SessionListener,
					GraphListener,
					SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// it is dangerous to save a graph. if you need it be sure to release the
	// reference on null-sessions or views
	private Graph graph;

	/**
	 * Please refer to the implementation of InspectorTab to see the inherited
	 * methods and fields.
	 */
	public AddonTab() {
		setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL },
							{ TableLayoutConstants.FILL, 10, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL } }));

	}

	@Override
	public String getName() {
		return getTitle();
	}

	/**
	 * Don't forget to set the title, otherwise the tab won't be shown.
	 */
	@Override
	public String getTitle() {
		return "Exemplary Tab";
	}

	/**
	 * Vanted will ask every time all tabs, when a view changes, if this tab
	 * wants to be hidden or not.
	 */
	@Override
	public boolean visibleForView(View v) {
		return true;
	}

	/**
	 * For demonstration purposes this tab listens to a view change and just
	 * prints out the view and creates an exemplary {@FolderPanel} .
	 */
	public void viewChanged(View newView) {
		removeAll();
		JTextArea ta = new JTextArea("<html>new View active:<br>" + newView);
		ta.setLineWrap(true);
		ta.setBorder(new BevelBorder(0));
		add(ta, "0,0");

		// creates a new jcomponent, which is widely used in vanted
		String title = "No View Open";
		if (newView != null)
			title = newView.getViewName();
		FolderPanel fp = new FolderPanel(title, false, true, false, null);
		fp.setColumnStyle(50, TableLayoutConstants.FILL);
		fp.addGuiComponentRow(new JLabel("IPK"), new JLabelHTMLlink("link",
							"http://www.ipk-gatersleben.de/Internet"), true, 5);
		fp
							.addGuiComponentRow(
												new JLabel("PBI"),
												new JLabelHTMLlink(
																	"link",
																	"http://pgrc-35.ipk-gatersleben.de/portal/page/portal/PG_BICGH/P_BICGH/Groups/Plant_Bioinformatics"),
												true, 5);
		fp.addGuiComponentRow(new JLabel("GABI"), new JLabelHTMLlink("link",
							"http://www.gabi.de/"), true, 5);
		fp.addGuiComponentRow(new JLabel("DFG"), new JLabelHTMLlink("link",
							"http://www.dfg.de/"), true, 5);
		fp.setIconSize(Iconsize.MIDDLE);
		fp.setMaximumRowCount(2);
		fp.setCondensedState(newView == null);
		// fp.addCollapseListenerDialogSizeUpdate();
		fp.layoutRows();
		add(fp, "0,2");
		validate();

		if (newView == null)
			;// free resources

	}

	/**
	 * For demonstration purposes this tab listens also to session changes to
	 * demonstrate how you can use graphlisteners. The problem here is that a
	 * graphlistener has to be registered for the graph you want to listen, and
	 * the session tracks which graph is the actually viewed graph. If this
	 * graph changes, we have to unregister the graphlistener from the old graph
	 * and have to register the new graph, in order to be able to listen to
	 * graphevents.
	 */
	public void sessionChanged(Session s) {
		if (s != null) {
			if (graph != null) {
				try {
					graph.getListenerManager().removeGraphListener(this);
				} catch (ListenerNotFoundException e) {
					ErrorMsg.addErrorMessage(e);
				}
			}

			graph = s.getGraph();
			graph.getListenerManager().addDelayedGraphListener(this);
		} else
			graph = null; // important, because if you don't free sessions,
		// views or graphs you will get a memory leak
	}

	public void sessionDataChanged(Session s) {
		// 

	}

	public void postEdgeAdded(GraphEvent e) {
		// 

	}

	public void postEdgeRemoved(GraphEvent e) {
		// 

	}

	public void postGraphCleared(GraphEvent e) {
		// 

	}

	/**
	 * As you can see, if a graph is open, every time a node is added we get a
	 * printout.
	 */
	public void postNodeAdded(GraphEvent e) {
		MainFrame.showMessage("Node added!", MessageType.INFO);
	}

	public void postNodeRemoved(GraphEvent e) {
		// 

	}

	public void preEdgeAdded(GraphEvent e) {
		// 

	}

	public void preEdgeRemoved(GraphEvent e) {
		// 

	}

	public void preGraphCleared(GraphEvent e) {
		// 

	}

	public void preNodeAdded(GraphEvent e) {
		// 

	}

	public void preNodeRemoved(GraphEvent e) {
		// 

	}

	/**
	 * Transactions can be used to bundle graph-changes and refresh the view
	 * afterwards. Usually you call
	 * <p>
	 * <code>view.getGraph().getListenerManager().transactionStarted(redrawbt);</code>
	 * <p>
	 * do your stuff like attributes changing and then
	 * <p>
	 * <code>view.getGraph().getListenerManager().transactionFinished(redrawbt);</code>
	 * <p>
	 * Now you can check what happened and react to that. For an implementation for a view see the method in {@link GraffitiView}.
	 */
	public void transactionFinished(TransactionEvent e,
						BackgroundTaskStatusProviderSupportingExternalCall status) {
		// 

	}

	public void transactionStarted(TransactionEvent e) {
		// 

	}

	/**
	 * If you want to listen to Selection events then (for an InspectorTab) you
	 * have to implement the {@link SelectionListener} interface and overwrite
	 * the <code>isSelectionListener</code> class and the tab will automatically
	 * be registered. All other objects have to be manually registered with <code>MainFrame.getInstance().addSelectionListener(this);</code>
	 */
	public void selectionChanged(SelectionEvent e) {
		// 

	}

	public void selectionListChanged(SelectionEvent e) {
		// 

	}

	@Override
	public boolean isSelectionListener() {
		return true;
	}

}
