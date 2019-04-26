package org.graffiti.plugin.inspector;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.AttributeListener;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * @author klukas
 */
public class SubtabHostTab extends InspectorTab
		implements SessionListener, ViewListener, ContainsTabbedPane, SelectionListener {
	private static final long serialVersionUID = -3810951162912767447L;

	static Logger logger = Logger.getLogger(SubtabHostTab.class);
	static {
		logger.setLevel(Level.INFO); // Adjust for debugging purposes
	}
	private List<InspectorTab> subtabs;

	JTabbedPane tabbedPane = new JTabbedPane();

	private LinkedHashSet<InspectorTab> hiddenTabs = new LinkedHashSet<InspectorTab>();

	/**
	 * 
	 */
	public SubtabHostTab(String title) {
		this.title = title;
		this.subtabs = new ArrayList<InspectorTab>();
		initComponents();
	}

	public SubtabHostTab(String title, Collection<InspectorTab> subtabs) {
		this.title = title;
		this.subtabs = new ArrayList<InspectorTab>(subtabs);
		initComponents();
	}

	public SubtabHostTab(String title, InspectorTab[] inspectorTabs) {
		Collection<InspectorTab> tabs = new ArrayList<InspectorTab>();
		for (InspectorTab it : inspectorTabs)
			tabs.add(it);
		this.title = title;
		this.subtabs = new ArrayList<InspectorTab>(tabs);
		initComponents();
	}

	private void initComponents() {
		double[][] sizeM = { { TableLayoutConstants.FILL }, // Columns
				{ TableLayoutConstants.FILL } }; // Rows

		setLayout(new TableLayout(sizeM));
		setBackground(null);
		setOpaque(false);

		for (InspectorTab tab : subtabs) {
			tabbedPane.addTab(tab.getTitle(), tab);
		}
		tabbedPane.validate();
		add(tabbedPane, "0,0");

		validate();
	}

	@Override
	public boolean visibleForView(View v) {
		boolean visible = false;
		for (InspectorTab tab : subtabs)
			visible = visible || ((tab.visibleForView(v) && (v == null || (v != null && v.worksWithTab(tab)))));
		return visible;
	}

	/**
	 * This componentShown callback is only called for the upmost tab and not it's
	 * children. To send the component shown to the visible children we recurse this
	 * event
	 * 
	 * @param e
	 */
	@Override
	public void componentShown(ComponentEvent e) {
		for (InspectorTab tab : subtabs)
			if (tab.isVisible())
				tab.componentShown(e);

	}

	public void sessionChanged(Session s) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof SessionListener) {
				SessionListener sl = (SessionListener) tab;
				sl.sessionChanged(s);
			}
		}
	}

	public Collection<InspectorTab> getTabs() {
		return subtabs;
	}

	/**
	 * Adds a tab to the inspector.
	 * 
	 * @param tab
	 *            the tab to add to the inspector.
	 */
	public synchronized void addTab(InspectorTab tab, ImageIcon icon) {
		if (!subtabs.contains(tab)) {
			subtabs.add(tab);
		}
		logger.debug("adding tab:" + tab.getTitle() + " to " + getTitle());
		switch (tab.getPreferredTabPosition()) {

		case InspectorTab.TAB_LEADING:
			tabbedPane.insertTab(tab.getTitle(), icon, tab, null, 0);
			break;
		case InspectorTab.TAB_TRAILING:
		default:
			tabbedPane.addTab(tab.getTitle(), icon, tab);
		}

	}

	/**
	 * Removes a tab from the inspector.
	 * 
	 * @param tab
	 *            the tab to remove from the inspector.
	 */
	public void removeTab(InspectorTab tab) {
		int idx = tabbedPane.indexOfTab(tab.getTitle());
		if (idx >= 0) {
			tabbedPane.removeTabAt(idx);
		}
		if (subtabs.contains(tab))
			subtabs.remove(tab);
	}

	public void hideTab(InspectorTab tab) {
		int idx = tabbedPane.indexOfTab(tab.getName());
		if (idx >= 0) {
			tabbedPane.removeTabAt(idx);
			hiddenTabs.add(tab);
		}
	}

	public void showTab(InspectorTab tab) {
		if (hiddenTabs.contains(tab)) {

			addTab(tab, tab.getIcon());
			hiddenTabs.remove(tab);
		}
	}

	public void sessionDataChanged(Session s) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof SessionListener) {
				SessionListener sl = (SessionListener) tab;
				sl.sessionDataChanged(s);
			}
		}
	}

	public void viewChanged(final View v) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (InspectorTab tab : subtabs) {
					if (!tab.visibleForView(v) || (v != null && !v.worksWithTab(tab))) {
						hideTab(tab);
					} else {
						showTab(tab);
					}
				}
				tabbedPane.validate();
			}
		});

	}

	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public boolean isSelectionListener() {
		return true;
	}

	public void selectionChanged(SelectionEvent e) {
		for (InspectorTab tab : subtabs) {
			if (tab.isSelectionListener()) {
				SelectionListener sl = (SelectionListener) tab;
				sl.selectionChanged(e);
			}
		}
	}

	public void selectionListChanged(SelectionEvent e) {
		for (InspectorTab tab : subtabs) {
			if (tab.isSelectionListener()) {
				SelectionListener sl = (SelectionListener) tab;
				sl.selectionListChanged(e);
			}
		}
	}

	public void postAttributeAdded(AttributeEvent e) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof AttributeListener) {
				AttributeListener l = (AttributeListener) tab;
				l.postAttributeAdded(e);
			}
		}
	}

	public void postAttributeChanged(AttributeEvent e) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof AttributeListener) {
				AttributeListener l = (AttributeListener) tab;
				l.postAttributeChanged(e);
			}
		}
	}

	public void postAttributeRemoved(AttributeEvent e) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof AttributeListener) {
				AttributeListener l = (AttributeListener) tab;
				l.postAttributeRemoved(e);
			}
		}
	}

	public void preAttributeAdded(AttributeEvent e) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof AttributeListener) {
				AttributeListener l = (AttributeListener) tab;
				l.preAttributeAdded(e);
			}
		}
	}

	public void preAttributeChanged(AttributeEvent e) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof AttributeListener) {
				AttributeListener l = (AttributeListener) tab;
				l.preAttributeChanged(e);
			}
		}
	}

	public void preAttributeRemoved(AttributeEvent e) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof AttributeListener) {
				AttributeListener l = (AttributeListener) tab;
				l.preAttributeRemoved(e);
			}
		}
	}

	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof AttributeListener) {
				AttributeListener l = (AttributeListener) tab;
				l.transactionFinished(e, status);
			}
		}
	}

	public void transactionStarted(TransactionEvent e) {
		for (InspectorTab tab : subtabs) {
			if (tab instanceof AttributeListener) {
				AttributeListener l = (AttributeListener) tab;
				l.transactionStarted(e);
			}
		}
	}

	@Override
	public void setEditPanelInformation(Map<?, ?> valueEditComponents, Map<GraphElement, GraphElement> map) {
		for (InspectorTab tab : subtabs) {
			if (tab.getEditPanel() != null) {
				tab.getEditPanel().setEditComponentMap(valueEditComponents);
				tab.getEditPanel().setGraphElementMap(map);
			}
		}
	}

	/**
	 * gets called each time a tab is added, to figure out the order of tab layout
	 * as given by their preferredTab Position parameter in InspectorTab
	 */
	private void sortTabs() {
		Collections.sort(subtabs, new Comparator<InspectorTab>() {

			@Override
			public int compare(InspectorTab o1, InspectorTab o2) {
				if (o1.getPreferredTabPosition() == o2.getPreferredTabPosition())
					return 0;
				else
					return o1.getPreferredTabPosition() < o2.getPreferredTabPosition() ? -1 : 1;
			}

		});
	}
}
