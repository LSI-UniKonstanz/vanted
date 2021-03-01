// ==============================================================================
//
// MatrixView.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MatrixView.java,v 1.7 2010/12/22 13:06:20 klukas Exp $

package org.graffiti.plugins.views.matrix;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeConsumer;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.EdgeEvent;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.NodeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.view.AbstractView;
import org.graffiti.plugin.view.MessageListener;

/**
 * Provides a matrix view for the given graph.
 * 
 * @version $Revision: 1.7 $
 * @vanted.revision 2.7.0
 */
public class MatrixView extends AbstractView implements ActionListener, AttributeConsumer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1378617686038001465L;
	
	// ~ Instance fields ========================================================
	/** The table, which contains the matrix of the graph. */
	private JTable matrixView;
	
	/** The table model. */
	private MatrixModel matrix;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new matrix view from the given graph.
	 */
	public MatrixView() {
		this(null);
	}
	
	/**
	 * Constructs a new matrix view.
	 * 
	 * @param graph
	 *           the graph to be displayed.
	 */
	public MatrixView(Graph graph) {
		super(graph);
		
		setLayout(new BorderLayout());
		
		matrixView = new JTable() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 2586891616659172017L;
			
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				if (c instanceof JComponent) {
					MatrixModel mm = (MatrixModel) getModel();
					JComponent jc = (JComponent) c;
					jc.setToolTipText(mm.getColumnName(vColIndex) + " - " + mm.getRowName(rowIndex));
				}
				return c;
			}
		};
		matrixView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		matrixView.setColumnSelectionAllowed(true);
		matrixView.setCellSelectionEnabled(true);
		add(matrixView, BorderLayout.CENTER);
	}
	
	// ~ Methods ================================================================
	
	@Override
	public Insets getAutoscrollInsets() {
		return new Insets(0, 0, 0, 0);
	}
	
	public CollectionAttribute getEdgeAttribute() {
		return null; // this view does not depend on any edge attributes
	}
	
	@Override
	public void setGraph(Graph graph) {
		currentGraph = graph;
		matrix = new MatrixModel(graph);
		matrixView.setModel(matrix);
		updateGUI();
	}
	
	public CollectionAttribute getGraphAttribute() {
		return null; // this view does not depend on any graph attributes
	}
	
	public CollectionAttribute getNodeAttribute() {
		return null; // this view does not depend on any node attributes
	}
	
	@Override
	public JComponent getViewComponent() {
		return this;
	}
	
	@Override
	public String getViewName() {
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent action) {
		// Object src = action.getSource();
	}
	
	@Override
	public void addMessageListener(MessageListener ml) {
	}
	
	@Override
	public void autoscroll(Point arg0) {
	}
	
	@Override
	public void close() {
	}
	
	@Override
	public void completeRedraw() {
		setGraph(currentGraph);
	}
	
	@Override
	public void postAttributeAdded(AttributeEvent e) {
	}
	
	@Override
	public void postAttributeChanged(AttributeEvent e) {
	}
	
	@Override
	public void postAttributeRemoved(AttributeEvent e) {
	}
	
	@Override
	public void postDirectedChanged(EdgeEvent e) {
	}
	
	@Override
	public void postEdgeAdded(GraphEvent e) {
		matrix.postEdgeAdded(e);
	}
	
	@Override
	public void postEdgeRemoved(GraphEvent e) {
		matrix.postEdgeRemoved(e);
		updateGUI();
	}
	
	@Override
	public void postEdgeReversed(EdgeEvent e) {
	}
	
	@Override
	public void postGraphCleared(GraphEvent e) {
		matrix.postGraphCleared(e);
		updateGUI();
	}
	
	@Override
	public void postInEdgeAdded(NodeEvent e) {
	}
	
	@Override
	public void postInEdgeRemoved(NodeEvent e) {
	}
	
	@Override
	public void postNodeAdded(GraphEvent e) {
		matrix.postNodeAdded(e);
		updateGUI();
	}
	
	@Override
	public void postNodeRemoved(GraphEvent e) {
		matrix.postNodeRemoved(e);
		updateGUI();
	}
	
	@Override
	public void postOutEdgeAdded(NodeEvent e) {
	}
	
	@Override
	public void postOutEdgeRemoved(NodeEvent e) {
	}
	
	@Override
	public void postSourceNodeChanged(EdgeEvent e) {
	}
	
	@Override
	public void postTargetNodeChanged(EdgeEvent e) {
	}
	
	@Override
	public void postUndirectedEdgeAdded(NodeEvent e) {
	}
	
	@Override
	public void postUndirectedEdgeRemoved(NodeEvent e) {
	}
	
	@Override
	public void preAttributeAdded(AttributeEvent e) {
	}
	
	@Override
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	@Override
	public void preAttributeRemoved(AttributeEvent e) {
	}
	
	@Override
	public void preDirectedChanged(EdgeEvent e) {
	}
	
	@Override
	public void preEdgeAdded(GraphEvent e) {
	}
	
	@Override
	public void preEdgeRemoved(GraphEvent e) {
	}
	
	@Override
	public void preEdgeReversed(EdgeEvent e) {
	}
	
	@Override
	public void preGraphCleared(GraphEvent e) {
	}
	
	@Override
	public void preInEdgeAdded(NodeEvent e) {
	}
	
	@Override
	public void preInEdgeRemoved(NodeEvent e) {
	}
	
	@Override
	public void preNodeAdded(GraphEvent e) {
	}
	
	@Override
	public void preNodeRemoved(GraphEvent e) {
	}
	
	@Override
	public void preOutEdgeAdded(NodeEvent e) {
	}
	
	@Override
	public void preOutEdgeRemoved(NodeEvent e) {
	}
	
	@Override
	public void preSourceNodeChanged(EdgeEvent e) {
	}
	
	@Override
	public void preTargetNodeChanged(EdgeEvent e) {
	}
	
	@Override
	public void preUndirectedEdgeAdded(NodeEvent e) {
	}
	
	@Override
	public void preUndirectedEdgeRemoved(NodeEvent e) {
	}
	
	@Override
	public void removeMessageListener(MessageListener ml) {
	}
	
	public void repaint(GraphElement ge) {
	}
	
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		matrix.transactionFinished(e, status);
		updateGUI();
	}
	
	@Override
	public void transactionStarted(TransactionEvent e) {
		matrix.transactionStarted(e);
		updateGUI();
	}
	
	@Override
	public void attributeChanged(Attribute attr) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Extracts the name of this view class. It has to be overridden by all extended
	 * subclasses of this class.
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	protected String extractName() {
		return this.getClass().getName();
	}
	
	/**
	 * @see org.graffiti.plugin.view.AbstractView#informMessageListener(String, int)
	 */
	protected void informMessageListener(String message, int type) {
	}
	
	/**
	 * Updates the gui.
	 */
	private void updateGUI() {
		TableColumn column;
		
		for (int i = 0; i < matrixView.getColumnCount(); i++) {
			column = matrixView.getColumnModel().getColumn(i);
			column.setPreferredWidth(18);
			column.setMaxWidth(18);
			column.setMinWidth(18);
		}
	}
	
	@Override
	public boolean putInScrollPane() {
		return true;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
