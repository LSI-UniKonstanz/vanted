package org.vanted.updater;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.ErrorMsg;
import org.FolderPanel;
import org.GuiRow;
import org.SearchFilter;
import org.graffiti.editor.MainFrame;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.Addon;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonManagerPlugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.NewsHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.RSSFeedManager;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskPanelEntry;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ScanForAddonUpdates {

	private boolean hasupdates = false;
	
	private boolean isFinished = false;
	
	private ArrayList<Object> res;
	
	public boolean hasUpdates() {
		/*
		 * refreshNews is normally asynchron.
		 * we make it synchron with help of a monitor
		 */
		addonUpdatesAvailable();
		while(! isFinished()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hasupdates;
	}


	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	
	
	private void addonUpdatesAvailable() {
		
		
		
		final RSSFeedManager rfm = RSSFeedManager.getInstance();
		rfm.loadRegisteredFeeds();
		rfm.setWordWrap(60);
		NewsHelper.refreshNews(rfm, new Runnable() {
			

			public void run() {
				
				res = new ArrayList<Object>();
				boolean found = false;
				for (JComponent jc : rfm.getNewsComponents()) {
					if (jc instanceof FolderPanel) {
						FolderPanel fp = (FolderPanel) jc;
						fp.setCondensedState(false);
						fp.addSearchFilter(getSearchFilter());
						fp.setMaximumRowCount(3, true);
						fp.setShowCondenseButton(false);
						fp.layoutRows();
						fp.addCollapseListenerDialogSizeUpdate();
						fp.setTitle(fp.getTitle().substring(0, fp.getTitle().indexOf(" (")) + " - " + fp.getFixedSearchFilterMatchCount() + " messages");
						if (fp.getFixedSearchFilterMatchCount() > 0) {
							found = true;
							res.add("");
							res.add(jc);
						}
					}
				}
				if (!found) {
					res.add("");
					res.add(new JLabel(
							"<html>" +
									"Currently, there is no new or additional " +
									"Add-on available for direct download."));
				}
//				
				if(hasupdates) {
					final BackgroundTaskPanelEntry backgroundTaskPanelEntry = new BackgroundTaskPanelEntry(false);
					
					final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
							"Updates for Addons found", "");
					//<html><small>Goto Edit->Preferences->Addon Manager<br/>Click on 'Install configure Addons'<br/>Click on 'Find Addons / Updates'
					status.setCurrentStatusValue(100);
					backgroundTaskPanelEntry.setStatusProvider(status, "Info", "Info");
					backgroundTaskPanelEntry.setTaskFinished(false, 0);
					JButton findAddonsUpdates = new JButton("Open Addons Manager");
					findAddonsUpdates.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							backgroundTaskPanelEntry.removeStatusPanel();				
							Object[] input = MyInputHelper.getInput("[OK]", "Direct Add-on Download", res.toArray());		
						}
					});
					backgroundTaskPanelEntry.add(findAddonsUpdates, "1,5");
					
					MainFrame.getInstance().addStatusPanel(backgroundTaskPanelEntry);
				}
				setFinished(true);
			}
			
			private SearchFilter getSearchFilter() {
				return new SearchFilter() {
					public boolean accept(GuiRow gr, String searchText) {
						if (gr.left == null || gr.right == null || searchText == null)
							return true;
						JButton downloadButton = (JButton) ErrorMsg.findChildComponent(gr.right, JButton.class);
						if (downloadButton != null) {
							String addOnProperty = (String) downloadButton.getClientProperty("addon-version");
							String oldVersion = "";
							if (addOnProperty != null && addOnProperty.length() > 0 && addOnProperty.contains("/v")) {
								// "vanted3d/v0.4"
								String addOnName;
								if(addOnProperty.contains("/req"))
									addOnName = addOnProperty.substring(0, addOnProperty.indexOf("/req"));
								else
									addOnName = addOnProperty.substring(0, addOnProperty.indexOf("/v"));
								String addOnVersion = addOnProperty.substring(addOnProperty.indexOf("/v") + "/v".length());
								for (Addon a : AddonManagerPlugin.getInstance().getAddons()) {
									if (a.getDescription() != null) {
										String n = a.getName();
										String v = a.getDescription().getVersion();
										if (addOnName.equalsIgnoreCase(n)) {
											if (v.compareToIgnoreCase(addOnVersion) >= 0)
												return false;
											else
												oldVersion = v;
										}
									}
								}
								if (oldVersion != null && oldVersion.length() > 0) {
									hasupdates = true;
									downloadButton.setText("<html>Update Add-on from v" + oldVersion + " to v" + addOnVersion + "");
								}
								return FolderPanel.getDefaultSearchFilter(null).accept(gr, searchText);
							}
						}
						return false;
					}
				};
			}
		}, null);
	}
}
