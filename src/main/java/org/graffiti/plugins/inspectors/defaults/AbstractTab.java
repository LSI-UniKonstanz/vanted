// ==============================================================================
//
// AbstractTab.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractTab.java,v 1.21 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.CompositeAttribute;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.AttributeListener;
import org.graffiti.event.TransactionEvent;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;
import org.graffiti.util.DelayThread;

/**
 * Represents an inspector tab.
 * 
 * @version $Revision: 1.21 $
 */
public abstract class AbstractTab
		extends InspectorTab
		implements SessionListener, SelectionListener, AttributeListener {
	// ~ Instance fields ========================================================
	
	private static Logger logger = Logger.getLogger(AbstractTab.class);
	static {
		logger.setLevel(Level.INFO);
	}
	
	private static final long serialVersionUID = 1L;
	
	/** The attribute used to display the tree. */
	protected Attribute collAttr;
	
	/** The root node of the displayed tree. */
	protected DefaultMutableTreeNode rootNode;
	
	/** The elements that are displayed by this tab. */
	protected Collection<? extends Attributable> attributables;
	
	private TreeSelectionListener myTreeSelectionListener = new MyTreeSelectionListener();
	
	/**
	 * Avoids duplicate updates
	 */
	
	private DelayThread delayThreadAttributeChanged;
	
	private DelayThread delayThreadAttributeAddedRemoved;
	
	AbstractTab instance;
	
	/**
	 * Creates a new AbstractTab object.
	 */
	public AbstractTab() {
		super();
		instance = this;
		
		delayThreadAttributeChanged = new DelayThread(new DelayThread.DelayedCallback() {
			
			@Override
			public void call(AttributeEvent e) {
				logger.debug("editPanel.updateTable");
				if (e != null)
					/*
					 * until i find a better solution, this will be it.
					 * The problem is, that recreating a ChartAttribute (through rebuildTreeAction)
					 * triggers an attributechanged event.. that will trigger again this update
					 * and so we get an infinite update loop
					 * This doesn't happen when we only update the table
					 * But.. Node and Edgetabs don't work properly when we only update the table
					 * especially labels will not change, if updated using the LabelEditor dialog
					 * So. for now.. we check, which tab acutally this is (Node/Edge/Graph tab are children of
					 * Abstracttab) and trigger the action appropriately
					 */
					if (instance instanceof GraphTab)
						editPanel.updateTable(e.getAttribute());
					else
						rebuildTreeAction();
			}
		});
		delayThreadAttributeChanged.setName(getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1) + ": DelayThread Attribute Changes");
		delayThreadAttributeChanged.start();
		
		delayThreadAttributeAddedRemoved = new DelayThread(new DelayThread.DelayedCallback() {
			
			@Override
			public void call(AttributeEvent e) {
				logger.debug("rebuildTreeAction");
				rebuildTreeAction();
			}
		});
		delayThreadAttributeAddedRemoved.setName(getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1)
				+ ": DelayThread for Added/Removed Attribute");
		delayThreadAttributeAddedRemoved.start();
		
		setLayout(TableLayout.getLayout(TableLayoutConstants.FILL, TableLayoutConstants.FILL));
		editPanel = new DefaultEditPanel(getEmptyDescription());
		editPanel.setOpaque(false);
		add(editPanel, "0,0");
		validate();
	}
	
	public String getEmptyDescription() {
		return "Properties of active selection/session are editable at this place.";
	}
	
	public String getTabNameForAttributeDescription() {
		return getTitle();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Called after an attribute has been added.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	public void postAttributeAdded(AttributeEvent e) {
		if (!isShowing())
			return;
		
		if (attributables != null)
			if (attributables.contains(e.getAttribute().getAttributable())) {
				// startRebuildTreeActionThread();
				delayThreadAttributeAddedRemoved.setAttributeEvent(e);
				
			}
	}
	
	/**
	 * Called after an attribute has been changed.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	public void postAttributeChanged(AttributeEvent e) {
		if (!isShowing())
			return;
		// logger.setLevel(Level.DEBUG);
		logger.debug("postAttributeChanged");
		if (attributables != null && attributables.contains(e.getAttribute().getAttributable())) {
			delayThreadAttributeChanged.setAttributeEvent(e);
		}
	}
	
	/**
	 * Called after an attribute has been removed.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	public void postAttributeRemoved(AttributeEvent e) {
		if (!isShowing())
			return;
		if (attributables != null)
			if (attributables.contains(e.getAttribute().getAttributable())) {
				// startRebuildTreeActionThread();
				delayThreadAttributeAddedRemoved.setAttributeEvent(e);
			}
	}
	
	/**
	 * Called just before an attribute is added.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	public void preAttributeAdded(AttributeEvent e) {
	}
	
	/**
	 * Called before a change of an attribute takes place.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	/**
	 * Called just before an attribute is removed.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	public void preAttributeRemoved(AttributeEvent e) {
	}
	
	protected void rebuildTreeAction() {
		
		/** The tree view of the attribute hierarchy. */
		logger.debug("rebuildtree for classname: " + this.getClass().getName());
		JTree attributeTree;
		
		// save current selection
		String oldMarkedPath = null;
		
		// start new tree with given attribute at root
		Attribute newAttr = collAttr;
		
		if (attributables != null && !attributables.isEmpty()) {
			newAttr = (attributables.iterator().next()).getAttributes();
			this.collAttr = newAttr;
		} else {
			newAttr = null;
			collAttr = null;
		}
		this.rootNode = new DefaultMutableTreeNode(new BooledAttribute(
				newAttr, true));
		synchronized (this.rootNode) {
			attributeTree = new JTree(this.rootNode);
			attributeTree.putClientProperty("JTree.lineStyle", "Angled");
			attributeTree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			
			/*
			 * build attribute hierarchy of newAttr starting at root and
			 * mark oldMarkedPath
			 */
			TreePath selectedTreePath = null;
			DefaultMutableTreeNode selectedNode = fillNode(this.rootNode, newAttr,
					attributables, oldMarkedPath);
			if (selectedNode != null) {
				selectedTreePath = new TreePath(selectedNode.getPath());
			}
			attributeTree.addTreeSelectionListener(myTreeSelectionListener);
			if (selectedTreePath == null) {
				attributeTree.setSelectionRow(0);
				attributeTree.expandRow(0);
			} else {
				attributeTree.setSelectionPath(selectedTreePath);
				attributeTree.scrollPathToVisible(selectedTreePath);
			}
			attributeTree.makeVisible(selectedTreePath);
		}
		// }
		// rebuildActionNeeded = false;
	}
	
	@Override
	public void componentShown(ComponentEvent e) {
		rebuildTreeAction();
	}
	
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		if (!isShowing())
			return;
		logger.debug("transactionFinished");
		delayThreadAttributeAddedRemoved.setAttributeEvent(null);
	}
	
	@Override
	public boolean isSelectionListener() {
		return true;
	}
	
	public void selectionChanged(SelectionEvent e) {
		logger.debug("selectionChanged");
		if (!isShowing())
			return;
		rebuildTreeAction();
	}
	
	public void sessionChanged(Session s) {
		
		if (s != null && s.getGraph() != null) {
			logger.debug("session changed sessionname: " + s.getGraph().getName());
			if ("org.graffiti.plugins.inspectors.defaults.EdgeTab".equals(getClass().getName()))
				logger.debug("for edgetab");
			editPanel.setListenerManager(s.getGraph().getListenerManager());
			s.getGraph().getListenerManager().addDelayedAttributeListener(this);
			if (s instanceof EditorSession) {
				EditorSession es = (EditorSession) s;
				editPanel.setGraphElementMap(es.getGraphElementsMap());
				/*
				 * attributables will be set during a following selectionChanged event
				 */
			}
		} else {
			this.attributables = null;
			this.collAttr = null;
			this.rootNode = null;
//			s.getGraph().getListenerManager().removeAttributeListener(this);
			editPanel.showEmpty();
		}
		
		if (!isShowing())
			return;
		
		rebuildTreeAction();
	}
	
	public void sessionDataChanged(Session s) {
		// empty
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param graphElements
	 *           DOCUMENT ME!
	 */
	protected void rebuildTree(Collection<? extends Attributable> graphElements) {
		attributables = graphElements;
		rebuildTreeAction();
	}
	
	/**
	 * Builds a tree of the hierarchy starting at attr and appends it to
	 * treeNode.
	 * 
	 * @param treeNode
	 *           DOCUMENT ME!
	 * @param attr
	 *           DOCUMENT ME!
	 * @param graphElements
	 *           DOCUMENT ME!
	 */
	@SuppressWarnings("unused")
	private void fillNode(DefaultMutableTreeNode treeNode, Attribute attr,
			Collection<Attributable> graphElements) {
		this.fillNode(treeNode, attr, graphElements, null);
	}
	
	/**
	 * Same as <code>fillNode(DefaultMutableTreeNode treeNode, Attribute attr)
	 * </code> but additionally selects the given attribute at markedPath.
	 * 
	 * @param treeNode
	 *           DOCUMENT ME!
	 * @param attr
	 *           DOCUMENT ME!
	 * @param graphElements
	 *           DOCUMENT ME!
	 * @param markedPath
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private DefaultMutableTreeNode fillNode(DefaultMutableTreeNode treeNode,
			Attribute attr, Collection<? extends Attributable> graphElements, String markedPath) {
		DefaultMutableTreeNode returnTreeNode = null;
		DefaultMutableTreeNode newNode;
		
		boolean allHave = true;
		boolean allSameValue = true;
		boolean allChildrenSameValue = true;
		Collection<Attribute> attrs;
		if (attr == null)
			return null;
		if (attr instanceof CollectionAttribute) {
			attrs = ((CollectionAttribute) attr).getCollection().values();
		} else
			if (attr instanceof CompositeAttribute) {
				attrs = new LinkedList<Attribute>();
				
				try {
					attrs.add(((CompositeAttribute) attr).getAttributes());
				} catch (RuntimeException e) {
					Object attributeValue = attr.getValue();
					
					// check if present in all graph elements
					if (graphElements.size() > 1) {
						for (Iterator<? extends Attributable> geit = graphElements.iterator(); geit.hasNext();) {
							try {
								Attribute oAttr = ((Attributable) geit.next())
										.getAttribute(attr.getPath().substring(1));
								
								if (!attributeValue.equals(oAttr.getValue())) {
									allSameValue = false;
									
									break;
								}
							} catch (AttributeNotFoundException anfe) {
								// found graph element that has no such attribute
								allHave = false;
								
								break;
							}
						}
					}
					
					if (allHave) {
						newNode = new DefaultMutableTreeNode(new BooledAttribute(attr, allSameValue));
						
						if (attr.getPath().equals(markedPath)) {
							returnTreeNode = newNode;
						}
					}
					
					return returnTreeNode;
				}
			} else {
				allHave = true;
				allSameValue = true;
				
				Attribute attribute = attr;
				// System.out.println("AAA: "+attribute.getClass().getSimpleName());
				Object attributeValue = attribute.getValue();
				
				// check if present in all graph elements
				if (graphElements.size() > 1) {
					for (Attributable geit : graphElements) {
						try {
							Attribute oAttr = geit.getAttribute(attribute.getPath().substring(1));
							
							if (!attributeValue.equals(oAttr.getValue())) {
								allSameValue = false;
								
								break;
							}
						} catch (AttributeNotFoundException anfe) {
							// found graph element that has no such attribute
							allHave = false;
							
							break;
							
						}
					}
				}
				
				if (allHave) {
					newNode = new DefaultMutableTreeNode(new BooledAttribute(attribute,
							allSameValue));
					
				}
				
				return returnTreeNode;
			}
		
		attrs = new ArrayList<Attribute>(attrs);
		
		for (Iterator<Attribute> it = attrs.iterator(); it.hasNext();) {
			allHave = true;
			allSameValue = true;
			
			Attribute attribute = (Attribute) it.next();
			Object attributeValue = attribute.getValue();
			
			// check if present in all graph elements
			if (graphElements.size() > 1) {
				for (Iterator<? extends Attributable> geit = graphElements.iterator(); geit.hasNext();) {
					try {
						Attribute oAttr = ((Attributable) geit.next())
								.getAttribute(attribute.getPath().substring(1));
						
						if (allSameValue && !attributeValue.equals(oAttr.getValue())) {
							allSameValue = false;
							
							// break;
						}
					} catch (AttributeNotFoundException anfe) {
						// found graph element that has no such attribute
						allHave = false;
						allChildrenSameValue = false;
						break;
					} catch (NullPointerException e) {
						// found graph element that has no such attribute
						allHave = false;
						allChildrenSameValue = false;
						break;
					}
				}
			}
			
			if (allHave) {
				newNode = new DefaultMutableTreeNode(new BooledAttribute(attribute,
						allSameValue));
				
				if (returnTreeNode == null) {
					returnTreeNode = fillNode(newNode, attribute, graphElements,
							markedPath);
				} else {
					fillNode(newNode, attribute, graphElements, null);
				}
				
				treeNode.add(newNode);
				
				if (!((BooledAttribute) newNode.getUserObject()).getBool())
					allChildrenSameValue = false;
				
				if (attribute.getPath().equals(markedPath)) {
					returnTreeNode = newNode;
				}
			}
		}
		
		/*
		 * if the children have the same value, set the bool variable accordingly for the parent one
		 * Important for component attributes, where the parent is a Map or collection which is compared as object not identical
		 */
		if (allChildrenSameValue)
			((BooledAttribute) treeNode.getUserObject()).setBool(allChildrenSameValue);
		
		// if (attr.getPath().equals(markedPath)) {
		// returnTreePath = new TreePath(treeNode.getPath());
		// }
		return returnTreeNode;
	}
	
	// ~ Inner Classes ==========================================================
	/**
	 * Implements valueChanged method that updates the table according to the
	 * selection in the tree.
	 */
	class MyTreeSelectionListener implements TreeSelectionListener {
		/**
		 * DOCUMENT ME!
		 * 
		 * @param e
		 *           DOCUMENT ME!
		 */
		public void valueChanged(TreeSelectionEvent e) {
			TreePath treePath = e.getNewLeadSelectionPath();
			
			if (treePath != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath
						.getLastPathComponent();
				
				if (node == null)
					return;
				
				// Attribute attr = (Attribute)node.getUserObject();
				editPanel.buildTable(node, attributables, getTabNameForAttributeDescription());
			}
		}
	}
	
	@Override
	public boolean visibleForView(View v) {
		return v != null && (v instanceof GraphView);
	}
	
	public void selectionListChanged(SelectionEvent e) {
		// empty
	}
	
	public void transactionStarted(TransactionEvent e) {
		// empty
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
