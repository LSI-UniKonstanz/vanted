// ==============================================================================
//
// OptionsDialog.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: OptionsDialog.java,v 1.6 2010/12/22 13:05:35 klukas Exp $

package org.graffiti.options;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.collections15.map.HashedMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.core.ImageBundle;
import org.graffiti.core.StringBundle;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.View;

/**
 * Represents the options dialog.
 * 
 * @version $Revision: 1.6 $
 */
public class OptionsDialog
					extends JDialog
					implements ActionListener, TreeSelectionListener {
	// ~ Static fields/initializers =============================================
	
	private static Logger logger = Logger.getLogger(OptionsDialog.class);
	
	static {
		logger.setLevel(Level.INFO);
	}
	
	public static final String CAT_VIEWS = "Views";
	public static final String CAT_ALGORITHMS = "Algorithms";
	public static final String CAT_TABS = "Tabs";
	public static final String CAT_MISC = "Misc";
	public static final String CAT_TOOLS = "Tools";
	public static final String CAT_ATTR_COMP = "Visualisation";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The <code>StringBundle</code> of this options dialog. */
	private static StringBundle sBundle = StringBundle.getInstance();
	
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	// private Hashtable panes;
	
	/** The <code>ImageBundle</code> of this options dialog. */
	private ImageBundle iBundle = ImageBundle.getInstance();
	
	/** DOCUMENT ME! */
	private JButton apply;
	
	/** DOCUMENT ME! */
	private JButton cancel;
	
	/** DOCUMENT ME! */
	private JButton ok;
	
	/** DOCUMENT ME! */
	private JLabel currentLabel;
	
	/** DOCUMENT ME! */
//	private JPanel cardPanel;
	
	/** DOCUMENT ME! */
	private JTree paneTree;
	
	/** DOCUMENT ME! */
	// private OptionGroup editGroup;
	
	/** DOCUMENT ME! */
//	private OptionGroup pluginsGroup;
	private Map<String, OptionGroup> mapCategories;
	
	
	JScrollPane optionPaneContainer;
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for OptionsDialog.
	 * 
	 * @param parent
	 *           the parent of this dialog
	 * @throws HeadlessException
	 *            DOCUMENT ME!
	 */
	public OptionsDialog(Frame parent)
						throws HeadlessException {
		super(parent, sBundle.getString("options.dialog.title"), true);
		
		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		setContentPane(content);
		
		content.setLayout(new BorderLayout());
		
		JPanel stage = new JPanel(new BorderLayout());
		stage.setBorder(new EmptyBorder(0, 6, 0, 0));
		content.add(stage, BorderLayout.CENTER);
		
		/*
		 * currentLabel displays the path of the currently selected
		 * OptionPane at the top of the stage area
		 */
		currentLabel = new JLabel();
		currentLabel.setHorizontalAlignment(SwingConstants.LEFT);
//		currentLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
//							Color.black));
		stage.add(currentLabel, BorderLayout.NORTH);
		
//		cardPanel = new JPanel(new CardLayout());
//		cardPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		OverviewOptionPane overviewOptionPane = new OverviewOptionPane();
		overviewOptionPane.init(null);
		optionPaneContainer = new JScrollPane(overviewOptionPane);
//		optionPaneContainer.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1,
//							Color.blue));
		stage.add(optionPaneContainer, BorderLayout.CENTER);
		
		paneTree = new JTree(createOptionTreeModel());
		
		paneTree.setCellRenderer(new PaneNameRenderer());
		paneTree.putClientProperty("JTree.lineStyle", "Angled");
		paneTree.setShowsRootHandles(true);
		paneTree.setRootVisible(false);
		content.add(new JScrollPane(paneTree,
							ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
							ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.WEST);
		
		JPanel buttons = new JPanel();
		buttons.setBorder(new EmptyBorder(12, 0, 0, 0));
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createGlue());
		
		ok = new JButton(sBundle.getString("common.ok"));
		ok.addActionListener(this);
		ok.setIcon(iBundle.getImageIcon("icon.common.ok"));
		buttons.add(ok);
		buttons.add(Box.createHorizontalStrut(6));
		getRootPane().setDefaultButton(ok);
		apply = new JButton(sBundle.getString("common.apply"));
		apply.addActionListener(this);
		apply.setIcon(iBundle.getImageIcon("icon.common.apply"));
		buttons.add(apply);
		buttons.add(Box.createHorizontalStrut(6));
		cancel = new JButton(sBundle.getString("common.cancel"));
		cancel.addActionListener(this);
		cancel.setIcon(iBundle.getImageIcon("icon.common.cancel"));
		buttons.add(cancel);
		
		buttons.add(Box.createGlue());
		
		content.add(buttons, BorderLayout.SOUTH);
		
		// register the Options dialog as a TreeSelectionListener.
		// this is done before the initial selection to ensure that the
		// first selected OptionPane is displayed on startup.
		paneTree.getSelectionModel().addTreeSelectionListener(this);
		
		
		
		// paneTree.expandPath(new TreePath( TODO
		// new Object[] { paneTree.getModel().getRoot(), editGroup }));
		paneTree.setSelectionRow(0);
		
		
		
		// parent.hideWaitCursor(); TODO
//		pack();
		setSize(600, 400);
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	// ~ Methods ================================================================
	
	
	private void prepareTree(OptionGroup parent) {
		Set<Class<? extends PreferencesInterface>> preferencingClasses = PreferenceManager.getInstance().getPreferencingClasses();
		
		mapCategories = new HashedMap<>();
		
		OptionGroup categoryNode;
		
		//get all the interfaces and their classnames (simpleName) and sort them by
		// view, algorithm, tool, tab superclasses/interfaces
		
		for(Class<? extends PreferencesInterface> preferencesClass : preferencingClasses) {
			try {
				PreferencesInterface instance = (PreferencesInterface)preferencesClass.newInstance();
				List<Parameter> defaultParameters = instance.getDefaultParameters();
				if(defaultParameters == null)
					continue;
				
				if(instance instanceof View) {
					categoryNode = getCategoryNode(CAT_VIEWS);
					addOptionGroup(categoryNode, parent);
					logger.debug("added new view category");
					
				} else if (instance instanceof InspectorTab) {
					categoryNode = getCategoryNode(CAT_TABS);
					addOptionGroup(categoryNode, parent);
					logger.debug("added new tabs category");
				} else if (instance instanceof Algorithm) {
					categoryNode = getCategoryNode(CAT_ALGORITHMS);
					addOptionGroup(categoryNode, parent);
					logger.debug("added new algorithm category");
				} else if (instance instanceof Tool) {
					categoryNode = getCategoryNode(CAT_TOOLS);
					addOptionGroup(categoryNode, parent);
					logger.debug("added new Tool category");
				} else if(instance instanceof AttributeComponent) {
					categoryNode = getCategoryNode(CAT_ATTR_COMP);
					addOptionGroup(categoryNode, parent);
				}
				else {
					categoryNode = getCategoryNode(CAT_MISC);
					addOptionGroup(categoryNode, parent);
					logger.debug("added new misc category");
				}
			
				logger.debug("added new category");

				
				Preferences preferences = PreferenceManager.getPreferenceForClass(instance.getClass());
				
				for(Parameter param : defaultParameters) {
					Object value = preferences.get(param.getName(), null);
					if(value != null) {
						param.setValue(value);
					}
				}
				
				String name = instance.getPreferencesAlternativeName();
				if(name == null)
					name = instance.getClass().getSimpleName();
				addOptionPane(getParameterOptionPane(name, defaultParameters, instance.getClass()), categoryNode);
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
//		DefaultMutableTreeNode classnode = new DefaultMutableTreeNode();
		
		//create tree entry for each class
		
		//create subtree entry for possible nodes beneath that class
		
		// get the parameters from the preferences of that class or subnode
		
		// copy the attributes or use the default ones and set the value from
		// the preferences object of that class or subnode
		
		// the parameter will be the data object of that treenode
		// the name of the treenode will be the name of the class or the name of the
		// subnode of that class
		// for each click on a tree node load the 
		// parameters panel that dispalys the editviewcomponents
		// on save, store the parameter in the preferences
		// maybe we can reuse the panel that creates the panel with the components for the 
		// algorithms
	}

	private OptionPane getParameterOptionPane(String name, List<Parameter> defaultParameters, Class<? extends PreferencesInterface> clazz) {

		ParameterOptionPane paramOptionPane = new ParameterOptionPane(name, defaultParameters, clazz);
		
		
		return paramOptionPane;
	}

	private OptionGroup getCategoryNode(String cat) {
		OptionGroup categoryNode;
		if((categoryNode = mapCategories.get(cat)) == null) {
			categoryNode = new OptionGroup(cat);
			mapCategories.put(cat, categoryNode);
		}
		return categoryNode;
	}
	
	/**
	 * Called, if a button in the dialog is pressed.
	 * 
	 * @param e
	 *           the action event.
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		if (src == ok) {
			ok();
		} else
			if (src == cancel) {
				cancel();
			} else
				if (src == apply) {
					ok(false);
				}
	}
	
	/**
	 * Adds the given option group to the list of option groups.
	 * 
	 * @param group
	 *           the option group to add.
	 */
//	public void addOptionGroup(OptionGroup group) {
//		addOptionGroup(group, pluginsGroup);
//	}
	
	/**
	 * Adds the given option pane to the list of option panes.
	 * 
	 * @param pane
	 *           the option pane to add to the list.
	 */
//	public void addOptionPane(OptionPane pane) {
//		addOptionPane(pane, pluginsGroup);
//	}
	
	/**
	 * Handles the &quot;cancel&quot; button.
	 */
	public void cancel() {
		dispose();
	}
	
	/**
	 * Handles the &quot;ok%quot; button.
	 */
	public void ok() {
		ok(true);
	}
	
	/**
	 * Handles the &quot;ok&quot;- and &quot;apply&quot;-buttons.
	 * 
	 * @param dispose
	 *           DOCUMENT ME!
	 */
	public void ok(boolean dispose) {
		OptionTreeModel m = (OptionTreeModel) paneTree.getModel();
		((OptionGroup) m.getRoot()).save();
		
		/* This will fire the PROPERTIES_CHANGED event */

		// editor.propertiesChanged(); TODO
		// Save settings to disk
		// editor.prefs.sync(); TODO
		// get rid of this dialog if necessary
		
		PreferenceManager.storePreferences();
		
		if (dispose) {
			dispose();
//			setVisible(false);
		}
	}
	
	/**
	 * Called, iff a value in the tree was selected.
	 * 
	 * @param e
	 *           the tree selection event.
	 */
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getPath();
		
		if ((path == null) ||
							!(path.getLastPathComponent() instanceof OptionPane)) {
			return;
		}
		
		OptionPane optionPane = (OptionPane)path.getLastPathComponent();
		
		/*
		for (int i = paneTree.isRootVisible() ? 0 : 1; i <= lastIdx; i++) {
			if (nodes[i] instanceof OptionPane) {
				optionPane = (OptionPane) nodes[i];
				name = optionPane.getName();
			} else
				if (nodes[i] instanceof OptionGroup) {
					name = ((OptionGroup) nodes[i]).getName();
				} else {
					continue;
				}
			
			if (name != null) {
				String label = sBundle.getString("options." + name + ".label");
				
				if (label == null) {
					buf.append(name);
				} else {
					buf.append(label);
				}
			}
			
			if (i != lastIdx) {
				buf.append(": ");
			}
		}
		
		 */
		currentLabel.setText(optionPane.getName());
		
		optionPane.init(null);
		
		optionPaneContainer.setViewportView(optionPane.getOptionDialogComponent());
		invalidate();
//		revalidate();
//		pack();
		
		
//		((CardLayout) cardPanel.getLayout()).show(cardPanel, name);
		
		
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param child
	 *           DOCUMENT ME!
	 * @param parent
	 *           DOCUMENT ME!
	 */
	private void addOptionGroup(OptionGroup child, OptionGroup parent) {
		Enumeration<?> enum2 = child.getMembers();
		
		while (enum2.hasMoreElements()) {
			Object elem = enum2.nextElement();
			
			if (elem instanceof OptionPane) {
				addOptionPane((OptionPane) elem, child);
			} else
				if (elem instanceof OptionGroup) {
					addOptionGroup((OptionGroup) elem, child);
				}
		}
		
		parent.addOptionGroup(child);
	}
	
	/**
	 * Adds the given option pane to the tree of option panes.
	 * 
	 * @param pane
	 *           the pane to add to the tree
	 * @param parent
	 *           the parent option group of the give option pane.
	 */
	private void addOptionPane(OptionPane pane, OptionGroup parent) {
//		String name = pane.getName();
//		cardPanel.add(pane.getOptionDialogComponent(), name);
		parent.addOptionPane(pane);
	}
	
	/**
	 * creates the optiontreemodel.
	 * IMPORTANT! It uses an own overlay model, which will only be created and 
	 * visualized if treenodes are added in here.
	 * Later changes to the model will not be visible to the Dialog
	 * 
	 * @return DOCUMENT ME!
	 */
	private OptionTreeModel createOptionTreeModel() {
		OptionTreeModel paneTreeModel = new OptionTreeModel();
		OptionGroup rootGroup = (OptionGroup) paneTreeModel.getRoot();
		
		addOptionPane(new OverviewOptionPane(), rootGroup);
		
		prepareTree(rootGroup);
		
		AddonsOptionsPane addonsPane = new AddonsOptionsPane();
		addOptionPane(addonsPane, rootGroup);
		
		
		return paneTreeModel;
	}
	
	// ~ Inner Classes ==========================================================
	
	/**
	 * Represents the tree of option panes.
	 * 
	 * @version $Revision: 1.6 $
	 */
	class OptionTreeModel
						implements TreeModel {
		/** The list of event listeners. */
		private EventListenerList listenerList = new EventListenerList();
		
		/** The root node. */
		private OptionGroup root = new OptionGroup(null);
		
		/**
		 * Returns the child of parent at index index.
		 * 
		 * @param parent
		 *           DOCUMENT ME!
		 * @param index
		 *           DOCUMENT ME!
		 * @return DOCUMENT ME!
		 */
		public Object getChild(Object parent, int index) {
			if (parent instanceof OptionGroup) {
				return ((OptionGroup) parent).getMember(index);
			} else {
				return null;
			}
		}
		
		/**
		 * Returns the number of childs of the given parent.
		 * 
		 * @param parent
		 *           DOCUMENT ME!
		 * @return DOCUMENT ME!
		 */
		public int getChildCount(Object parent) {
			if (parent instanceof OptionGroup) {
				return ((OptionGroup) parent).getMemberCount();
			} else {
				return 0;
			}
		}
		
		/**
		 * Returns the index of the given child.
		 * 
		 * @param parent
		 *           DOCUMENT ME!
		 * @param child
		 *           DOCUMENT ME!
		 * @return DOCUMENT ME!
		 */
		public int getIndexOfChild(Object parent, Object child) {
			if (parent instanceof OptionGroup) {
				return ((OptionGroup) parent).getMemberIndex(child);
			} else {
				return -1;
			}
		}
		
		/**
		 * Returns <code>true</code>, iff the specified node is a leaf. Leafs
		 * are option panes.
		 * 
		 * @param node
		 *           DOCUMENT ME!
		 * @return DOCUMENT ME!
		 */
		public boolean isLeaf(Object node) {
			return node instanceof OptionPane;
		}
		
		/**
		 * Returns the root node.
		 * 
		 * @return DOCUMENT ME!
		 */
		public Object getRoot() {
			return root;
		}
		
		/**
		 * Adds the given tree model listener.
		 * 
		 * @param l
		 *           DOCUMENT ME!
		 */
		public void addTreeModelListener(TreeModelListener l) {
			listenerList.add(TreeModelListener.class, l);
		}
		
		/**
		 * Removes the given tree model listener.
		 * 
		 * @param l
		 *           DOCUMENT ME!
		 */
		public void removeTreeModelListener(TreeModelListener l) {
			listenerList.remove(TreeModelListener.class, l);
		}
		
		/**
		 * DOCUMENT ME!
		 * 
		 * @param path
		 *           DOCUMENT ME!
		 * @param newValue
		 *           DOCUMENT ME!
		 */
		public void valueForPathChanged(TreePath path, Object newValue) {
			/* this model may not be changed by the TableCellEditor */
		}
		
		/**
		 * Called, if a number of nodes changed their state.
		 * 
		 * @param source
		 *           DOCUMENT ME!
		 * @param path
		 *           DOCUMENT ME!
		 * @param childIndices
		 *           DOCUMENT ME!
		 * @param children
		 *           DOCUMENT ME!
		 */
		protected void fireNodesChanged(Object source, Object[] path,
							int[] childIndices, Object[] children) {
			Object[] listeners = listenerList.getListenerList();
			
			TreeModelEvent modelEvent = null;
			
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] != TreeModelListener.class)
					continue;
				
				if (modelEvent == null) {
					modelEvent = new TreeModelEvent(source, path, childIndices,
										children);
				}
				
				((TreeModelListener) listeners[i + 1]).treeNodesChanged(modelEvent);
			}
		}
		
		/**
		 * Called, iff some nodes are inserted in the tree model.
		 * 
		 * @param source
		 *           DOCUMENT ME!
		 * @param path
		 *           DOCUMENT ME!
		 * @param childIndices
		 *           DOCUMENT ME!
		 * @param children
		 *           DOCUMENT ME!
		 */
		protected void fireNodesInserted(Object source, Object[] path,
							int[] childIndices, Object[] children) {
			Object[] listeners = listenerList.getListenerList();
			
			TreeModelEvent modelEvent = null;
			
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] != TreeModelListener.class) {
					continue;
				}
				
				if (modelEvent == null) {
					modelEvent = new TreeModelEvent(source, path, childIndices,
										children);
				}
				
				((TreeModelListener) listeners[i + 1]).treeNodesInserted(modelEvent);
			}
		}
		
		/**
		 * Called, iff some nodes are removed from the tree model.
		 * 
		 * @param source
		 *           DOCUMENT ME!
		 * @param path
		 *           DOCUMENT ME!
		 * @param childIndices
		 *           DOCUMENT ME!
		 * @param children
		 *           DOCUMENT ME!
		 */
		protected void fireNodesRemoved(Object source, Object[] path,
							int[] childIndices, Object[] children) {
			Object[] listeners = listenerList.getListenerList();
			
			TreeModelEvent modelEvent = null;
			
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] != TreeModelListener.class)
					continue;
				
				if (modelEvent == null) {
					modelEvent = new TreeModelEvent(source, path, childIndices,
										children);
				}
				
				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(modelEvent);
			}
		}
		
		/**
		 * Called, iff the tree structure changed.
		 * 
		 * @param source
		 *           DOCUMENT ME!
		 * @param path
		 *           DOCUMENT ME!
		 * @param childIndices
		 *           DOCUMENT ME!
		 * @param children
		 *           DOCUMENT ME!
		 */
		protected void fireTreeStructureChanged(Object source, Object[] path,
							int[] childIndices, Object[] children) {
			Object[] listeners = listenerList.getListenerList();
			
			TreeModelEvent modelEvent = null;
			
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] != TreeModelListener.class) {
					continue;
				}
				
				if (modelEvent == null) {
					modelEvent = new TreeModelEvent(source, path, childIndices,
										children);
				}
				
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(modelEvent);
			}
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @author $Author: klukas $
	 * @version $Revision: 1.6 $ $Date: 2010/12/22 13:05:35 $
	 */
	class PaneNameRenderer
						extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		/** DOCUMENT ME! */
		private Font groupFont;
		
		/** DOCUMENT ME! */
		private Font paneFont;
		
		/**
		 * Creates a new PaneNameRenderer object.
		 */
		public PaneNameRenderer() {
			paneFont = UIManager.getFont("Tree.font");
			groupFont = paneFont.deriveFont(Font.BOLD);
		}
		
		/**
		 * DOCUMENT ME!
		 * 
		 * @param tree
		 *           DOCUMENT ME!
		 * @param value
		 *           DOCUMENT ME!
		 * @param selected
		 *           DOCUMENT ME!
		 * @param expanded
		 *           DOCUMENT ME!
		 * @param leaf
		 *           DOCUMENT ME!
		 * @param row
		 *           DOCUMENT ME!
		 * @param hasFocus
		 *           DOCUMENT ME!
		 * @return DOCUMENT ME!
		 */
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
							boolean selected, boolean expanded, boolean leaf, int row,
							boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, selected, expanded,
								leaf, row, hasFocus);
			
			String name = null;
			
			if (value instanceof OptionGroup) {
				name = ((OptionGroup) value).getName();
				this.setFont(groupFont);
			} else
				if (value instanceof OptionPane) {
					name = ((OptionPane) value).getName();
					this.setFont(paneFont);
				}
			
			if (name == null) {
				setText(null);
			} else {
				String label = sBundle.getString("options." + name + ".label");
				
				if (label == null) {
					setText(name);
				} else {
					setText(label);
				}
			}
			
			setIcon(null);
			
			return this;
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
