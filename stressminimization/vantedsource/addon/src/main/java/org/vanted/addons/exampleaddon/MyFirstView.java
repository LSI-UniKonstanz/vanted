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

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.EdgeEvent;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.NodeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.managers.AttributeComponentManager;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.MessageListener;
import org.graffiti.plugin.view.View2D;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;

/**
 * My own view! You inherit thousands of methods from {@link View2D}, which have
 * to be implemeneted by ... you! A view listens to many, many events and it is
 * quite complicated. Think about contacting us...
 * <p>
 * The view doesn't do much at all, but works for the different events.
 * 
 * @author Hendrik Rohn
 */

// public class MyFirstView extends GraffitiView
public class MyFirstView extends JComponent
		implements
		Printable,
		View2D,
		SelectionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * This shows how to create a view from code for a given {@link EditorSession}.
	 */
	public static MyFirstView createInternalExemplaryView(EditorSession es) {
		String viewname = "Exemplary View";
		if (es != null && es.getGraph() != null && es.getGraph().getName() != null)
			viewname = es.getGraph().getName();
		MyFirstView v = ((MyFirstView) MainFrame.getInstance().createInternalFrame(MyFirstView.class.getCanonicalName(), viewname, es, true));
		return v;
	}
	
	public MyFirstView() {
		super();
	}
	
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		// 
		return 0;
	}
	
	public void addMessageListener(MessageListener ml) {
		// 
		
	}
	
	/**
	 * cleanup references
	 */
	public void close() {
	}
	
	public void completeRedraw() {
		MainFrame.showMessage("Lets recreate View content", MessageType.INFO);
	}
	
	public JComponent getViewComponent() {
		JPanel pan = new JPanel();
		pan.add(new JLabel("Here you can paint everything you want!"));
		return pan;
	}
	
	public String getViewName() {
		// 
		return "Exemplary View";
	}
	
	public Set<AttributeComponent> getAttributeComponentsForElement(
			GraphElement ge) {
		// 
		return null;
	}
	
	public Map<?, ?> getComponentElementMap() {
		// 
		return null;
	}
	
	public GraphElementComponent getComponentForElement(GraphElement ge) {
		// 
		return null;
	}
	
	public Graph getGraph() {
		// 
		return null;
	}
	
	public boolean putInScrollPane() {
		// 
		return false;
	}
	
	public void removeMessageListener(MessageListener ml) {
		// 
		
	}
	
	public void repaint(GraphElement ge) {
		// 
		
	}
	
	public void setAttributeComponentManager(AttributeComponentManager acm) {
		// 
		
	}
	
	public void setGraph(Graph graph) {
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
	
	public void transactionStarted(TransactionEvent e) {
		// 
		
	}
	
	public void postInEdgeAdded(NodeEvent e) {
		// 
		
	}
	
	public void postInEdgeRemoved(NodeEvent e) {
		// 
		
	}
	
	public void postOutEdgeAdded(NodeEvent e) {
		// 
		
	}
	
	public void postOutEdgeRemoved(NodeEvent e) {
		// 
		
	}
	
	public void postUndirectedEdgeAdded(NodeEvent e) {
		// 
		
	}
	
	public void postUndirectedEdgeRemoved(NodeEvent e) {
		// 
		
	}
	
	public void preInEdgeAdded(NodeEvent e) {
		// 
		
	}
	
	public void preInEdgeRemoved(NodeEvent e) {
		// 
		
	}
	
	public void preOutEdgeAdded(NodeEvent e) {
		// 
		
	}
	
	public void preOutEdgeRemoved(NodeEvent e) {
		// 
		
	}
	
	public void preUndirectedEdgeAdded(NodeEvent e) {
		// 
		
	}
	
	public void preUndirectedEdgeRemoved(NodeEvent e) {
		// 
		
	}
	
	public void postDirectedChanged(EdgeEvent e) {
		// 
		
	}
	
	public void postEdgeReversed(EdgeEvent e) {
		// 
		
	}
	
	public void postSourceNodeChanged(EdgeEvent e) {
		// 
		
	}
	
	public void postTargetNodeChanged(EdgeEvent e) {
		// 
		
	}
	
	public void preDirectedChanged(EdgeEvent e) {
		// 
		
	}
	
	public void preEdgeReversed(EdgeEvent e) {
		// 
		
	}
	
	public void preSourceNodeChanged(EdgeEvent e) {
		// 
		
	}
	
	public void preTargetNodeChanged(EdgeEvent e) {
		// 
		
	}
	
	public void postAttributeAdded(AttributeEvent e) {
		// 
		
	}
	
	public void postAttributeChanged(AttributeEvent e) {
		// 
		
	}
	
	public void postAttributeRemoved(AttributeEvent e) {
		// 
		
	}
	
	public void preAttributeAdded(AttributeEvent e) {
		// 
		
	}
	
	public void preAttributeChanged(AttributeEvent e) {
		// 
		
	}
	
	public void preAttributeRemoved(AttributeEvent e) {
		// 
		
	}
	
	public void autoscroll(Point cursorLocn) {
		// 
		
	}
	
	public Insets getAutoscrollInsets() {
		// 
		return null;
	}
	
	public void zoomChanged(AffineTransform newZoom) {
		// 
		
	}
	
	public AffineTransform getZoom() {
		// 
		return null;
	}
	
	public void selectionChanged(SelectionEvent e) {
		MainFrame.showMessage("Selection changed!", MessageType.INFO);
	}
	
	public void selectionListChanged(SelectionEvent e) {
		// 
		
	}
	
	public void transactionFinished(TransactionEvent e,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		// 
		
	}
	
	/**
	 * These methods allow to add {@link JComponent}s top or bottom of or beside
	 * the view.
	 */
	
	public JComponent getViewToolbarComponentBackground() {
		// 
		return null;
	}
	
	public Object getViewToolbarComponentBottom() {
		// 
		return null;
	}
	
	public Object getViewToolbarComponentLeft() {
		// 
		return null;
	}
	
	public Object getViewToolbarComponentRight() {
		// 
		return null;
	}
	
	public Object getViewToolbarComponentTop() {
		// 
		return null;
	}
	
	public void closing(AWTEvent e) {
		// 
		
	}
	
	/**
	 * Here you can specify the tabs you don't want to see for the view.
	 */
	public boolean worksWithTab(InspectorTab tab) {
		// we allow all tabs, which want to be visible
		return true;
	}
	
	public boolean redrawActive() {
		// 
		return false;
	}
	
	@Override
	public void attributeChanged(Attribute attr) {
	}
	
}
