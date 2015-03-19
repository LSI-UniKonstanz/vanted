/**
 * 
 */
package org.graffiti.editor.actions;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.iharder.dnd.FileDrop;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.HomeFolder;
import org.JMButton;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsFalse;
import org.SettingsHelperDefaultIsTrue;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.options.PreferencePanel;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugins.modes.defaults.MegaMoveTool;
import org.graffiti.plugins.modes.defaults.MegaTools;

import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonManagerPlugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.plugin_info.PluginInfoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.LookAndFeelWrapper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.ThemedLookAndFeelInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author matthiak
 *
 */
public class ShowPreferencesAction 
extends GraffitiAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6591986799740726360L;


	private JCheckBox keggEnabler;


	/**
	 * @param name
	 * @param mainFrame
	 * @param helpID
	 */
	public ShowPreferencesAction(MainFrame mainFrame) {
		super("edit.preferences", mainFrame, "editmenu_preferences");

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		JDialog dialog = new JDialog(mainFrame);
		dialog.setTitle("Vanted Preferences");
		
		dialog.setPreferredSize(new Dimension(450, 600));
		JScrollPane scrollpane = new JScrollPane(getSettings());
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(scrollpane, BorderLayout.CENTER);
		dialog.pack();
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setVisible(true);
	}

	private JPanel getSettings() {
		final JPanel res = new JPanel();
		res.setBackground(null);
		double border = 5;
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, 2 * border, TableLayoutConstants.PREFERRED, 2 * border,
					TableLayoutConstants.PREFERRED, 2 * border, TableLayoutConstants.PREFERRED, 2 * border,
					TableLayoutConstants.PREFERRED, 2 * border, TableLayoutConstants.PREFERRED, 5 * border,
					TableLayoutConstants.PREFERRED, 2 * border, TableLayoutConstants.PREFERRED, 5 * border,
					TableLayoutConstants.PREFERRED, 5 * border } }; // Rows
					res.setLayout(new TableLayout(size));

					JCheckBox helpEnabler = new JCheckBox("<html><font color='gray'>Help Functions (not yet available)");

					final JComboBox<LookAndFeelWrapper> lookSelection = new JComboBox<>();
					lookSelection.setOpaque(false);
					final JMButton saveLook = new JMButton("Save");
					saveLook.setEnabled(false);

					// // windows styles
					// if (AttributeHelper.windowsRunning()) {
					// String[] val = new String[] {
					// "org.fife.plaf.Office2003.Office2003LookAndFeel",
					// "org.fife.plaf.OfficeXP.OfficeXPLookAndFeel",
					// "org.fife.plaf.VisualStudio2005.VisualStudio2005LookAndFeel" };
					// String[] desc = new String[] { "Office 2003 Style", "Office XP Style",
					// "VisualStudio 2005 Style" };
					// int i = 0;
					// for (String v : val)
					// lookSelection.addItem(new LookAndFeelWrapper(new
					// LookAndFeelInfo(desc[i], v)));
					// i++;
					// }

					ThemedLookAndFeelInfo info = new ThemedLookAndFeelInfo("VANTED", "de.muntjak.tinylookandfeel.TinyLookAndFeel",
							"VANTED");
					lookSelection.addItem(new LookAndFeelWrapper(info));

					try {
						LookAndFeelWrapper avtiveLaF = null;
						String sel = UIManager.getLookAndFeel().getClass().getCanonicalName();
						for (LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
							LookAndFeelWrapper d = new LookAndFeelWrapper(lafi);
							if (d.getClassName().equals(sel))
								avtiveLaF = d;
							if (d.isValid() && !d.getName().equals("TinyLookAndFeel"))
								lookSelection.addItem(d);
						}
						if (avtiveLaF != null)
							lookSelection.setSelectedItem(avtiveLaF);
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
					lookSelection.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									LookAndFeelWrapper po = (LookAndFeelWrapper) lookSelection.getSelectedItem();
									if (po == null)
										return;

									po.activateTheme();

									try {
										UIManager.setLookAndFeel(po.getClassName());
										if (ReleaseInfo.isRunningAsApplet())
											SwingUtilities.updateComponentTreeUI(ReleaseInfo.getApplet());
										else
											SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
										MainFrame.getInstance().repaint();
										saveLook.setEnabled(true);
										saveLook.setText("Save");
										saveLook.requestFocus();
									} catch (Exception err) {
										saveLook.setEnabled(false);
										saveLook.setText("Error");
										ErrorMsg.addErrorMessage(err);
									} catch (Error e) {
										saveLook.setEnabled(false);
										saveLook.setText("N/A");
									}
								}
							});
						}
					});

					saveLook.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							LookAndFeelWrapper op = (LookAndFeelWrapper) lookSelection.getSelectedItem();
							if (op == null)
								return;

							try {
								new File(ReleaseInfo.getAppFolderWithFinalSep() + "setting_java_look_and_feel").delete();
								TextFile.write(ReleaseInfo.getAppFolderWithFinalSep() + "setting_java_look_and_feel", op.getClassName());
								saveLook.setText("saved");
								saveLook.setEnabled(false);
								lookSelection.requestFocus();
							} catch (Exception err) {
								ErrorMsg.addErrorMessage(err);
							}
						}
					});

					helpEnabler.addActionListener(getHelpEnabledSettingActionListener(helpEnabler));
					helpEnabler.setOpaque(false);
					boolean auto = ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp);
					helpEnabler.setSelected(auto);
					res.add(TableLayout.get3Split(new JLabel("Look and feel: "), lookSelection, saveLook, TableLayout.PREFERRED,
							TableLayout.FILL, TableLayout.PREFERRED), "1,1");

					keggEnabler = new JCheckBox("KEGG access");
					keggEnabler.setOpaque(false);
					try {
						if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").exists())
							keggEnabler.setSelected(true);
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
					keggEnabler.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							if (keggEnabler.isSelected()) {
								keggEnabler.setSelected(false);
								if (Main.doEnableKEGGaskUser()) {
									try {
										new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").createNewFile();
									} catch (IOException e) {
										ErrorMsg.addErrorMessage(e);
									}
									try {
										if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").exists())
											new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").delete();
									} catch (Exception e) {
										ErrorMsg.addErrorMessage(e);
									}
								} else {
									try {
										new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").createNewFile();
									} catch (Exception e) {
										ErrorMsg.addErrorMessage(e);
									}
									try {
										if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").exists())
											new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").delete();
									} catch (Exception e) {
										ErrorMsg.addErrorMessage(e);
									}
								}
							} else {
								try {
									new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").createNewFile();
								} catch (Exception e) {
									ErrorMsg.addErrorMessage(e);
								}
								try {
									if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").exists())
										new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").delete();
								} catch (Exception e) {
									ErrorMsg.addErrorMessage(e);
								}
							}
							try {
								if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").exists())
									keggEnabler.setSelected(true);
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						}
					});

					// helpEnabler
					res.add(getPluginConfigurationPanel(null, keggEnabler), "1,3");
					res.add(TableLayout.get3Split(getAddOnManagerButton(), null, getPreferencesFolderButton(), TableLayout.FILL, 4,
							TableLayout.FILL, 0, 0), "1,5");

					res.add(TableLayout.getSplit(getGridSettingEditor(), null, TableLayout.FILL, TableLayout.PREFERRED), "1,7");

					res.add(TableLayout.getSplit(null, null, TableLayout.PREFERRED, TableLayout.FILL), "1,9");

					res.add(new JLabel("<html>"
							+ "<font color='#BB22222'>After restarting the program the changed settings will be fully active."), "1,11");

					JButton btOptionsDialog = new JButton("Options Dialog");
					btOptionsDialog.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
//							OptionsDialog od = new OptionsDialog(MainFrame.getInstance());
//							od.setVisible(true);
							new PreferencePanel();
						}
					});
					res.add(btOptionsDialog, "1,13");
					
					JButton btSavePreferences = new JButton("Safe Preferences");
					btSavePreferences.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							PreferenceManager.storePreferences();
						}
					});
					res.add(btSavePreferences, "1,15");
					// final JLabel memLabel = GravistoService.getMemoryInfoLabel(false);
					// res.add(memLabel, "1,13");
					return res;
	}

	private JComponent getGridSettingEditor() {
		FolderPanel settings = new FolderPanel("Graph-View Settings", false, false, false, null);
		boolean enabled = new SettingsHelperDefaultIsTrue().isEnabled("graph_view_grid");
		MegaMoveTool.gridEnabled = enabled;

		Runnable enableGrid = new Runnable() {
			public void run() {
				MegaMoveTool.gridEnabled = true;
			}

		};
		Runnable disableGrid = new Runnable() {
			public void run() {
				MegaMoveTool.gridEnabled = false;
			}
		};
		Runnable enableZoom = new Runnable() {
			public void run() {
				MegaTools.MouseWheelZoomEnabled = true;
			}

		};
		Runnable disableZoom = new Runnable() {
			public void run() {
				MegaTools.MouseWheelZoomEnabled = false;
			}
		};
		JComponent gridCheckBox = new SettingsHelperDefaultIsTrue().getBooleanSettingsEditor("Enable Grid",
				"graph_view_grid", enableGrid, disableGrid);
		settings.addGuiComponentRow(null, gridCheckBox, false);

		JComponent databaseCheckBox = new SettingsHelperDefaultIsFalse().getBooleanSettingsEditor(
				"Database-based node statusbar-infos", "grav_view_database_node_status", enableGrid, disableGrid);
		settings.addGuiComponentRow(null, databaseCheckBox, false);

		// Database-based node statusbar-infos

		JComponent zoomCheckBox = new SettingsHelperDefaultIsTrue().getBooleanSettingsEditor(
				"<html>Mouse Wheel Zoom<br>(disable to scroll instead)", "graph_view_wheel_zoom", enableZoom, disableZoom);
		settings.addGuiComponentRow(null, zoomCheckBox, false);
		// settings.addGuiComponentRow(new JLabel("Move nodes/bends"), gridSize,
		// false);
		// settings.addGuiComponentRow(new JLabel("Resize small nodes"),
		// gridSizeSmall, false);
		// settings.addGuiComponentRow(new JLabel("Resize normal nodes"),
		// gridSizeNormal, false);
		// settings.addGuiComponentRow(new JLabel("Resize large nodes"),
		// gridSizeLarge, false);
		settings.layoutRows();
		return settings;
	}



	private ActionListener getHelpEnabledSettingActionListener(final JCheckBox helpEnabler) {
		ActionListener res = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (helpEnabler.isSelected())
						new File(ReleaseInfo.getAppFolderWithFinalSep() + "setting_help_enabled").createNewFile();
					else {
						new File(ReleaseInfo.getAppFolderWithFinalSep() + "setting_help_enabled").delete();
					}
				} catch (IOException err) {
					ErrorMsg.addErrorMessage(err);
				}

			}
		};
		return res;
	}

	private JComponent getPluginConfigurationPanel(final JComponent additionalSetting1,
			final JComponent additionalSetting2) {

		JLabel button = new JLabel("Loading of optional program features (");
		JLabel bt3 = new JLabel("):");
		JLabelHTMLlink bt2 = new JLabelHTMLlink("Reset", "Resets all settings to their default state. Restart needed!",
				new Runnable() {
			@Override
			public void run() {
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.startsWith("feature_disabled") || name.startsWith("feature_enabled");
					}
				};
				for (String settingsFile : new File(HomeFolder.getHomeFolder()).list(filter))
					new File(HomeFolder.getHomeFolder() + "/" + settingsFile).delete();
			}
		});

		final FolderPanel features = new FolderPanel(null, false, false, false, null);
		features.setBackground(null);
		features.setFrameColor(null, null, 0, 5);
		ErrorMsg.addOnAppLoadingFinishedAction(new Runnable() {
			public void run() {
				features.clearGuiComponentList();
				if (additionalSetting1 != null)
					features.addGuiComponentRow(null, additionalSetting1, false);
				if (additionalSetting2 != null)
					features.addGuiComponentRow(null, additionalSetting2, false);
				for (JComponent jc : getOptionalSettingsPanels()) {
					features.addGuiComponentRow(null, jc, false);
				}
				features.layoutRows();
			}
		});
		return TableLayout.getSplitVertical(TableLayout.get3Split(button, bt2, bt3, TableLayout.PREFERRED,
				TableLayout.PREFERRED, TableLayout.PREFERRED), features, TableLayout.PREFERRED, TableLayout.PREFERRED);
	}

	private Collection<JComponent> getOptionalSettingsPanels() {
		Collection<JComponent> result = new ArrayList<JComponent>();
		for (PluginEntry pe : MainFrame.getInstance().getPluginManager().getPluginEntries()) {
			if (pe.getDescription().isOptional()) {
				result.add(TableLayout.getSplit(getEnableDisableOption(pe), new JLabel(""), TableLayout.PREFERRED,
						TableLayout.FILL));
			}
		}
		return result;
	}

	private JComponent getEnableDisableOption(final PluginEntry pe) {
		JComponent setting;
		if (pe.getDescription().isOptionalDefaultTrue())
			setting = new SettingsHelperDefaultIsTrue().getBooleanSettingsEditor(pe.getDescription().getName(), pe
					.getDescription().getName(), null, null);
		else
			setting = new SettingsHelperDefaultIsFalse().getBooleanSettingsEditor(pe.getDescription().getName(), pe
					.getDescription().getName(), null, null);
		try {
			String desc = PluginInfoHelper.getSummaryInfo(false, pe.getDescription(), pe.getPlugin());
			setting.setToolTipText("<html>" + desc);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return setting;
	}

	private JButton getAddOnManagerButton() {
		final JButton result = new JMButton("<html>Install / Configure Add-ons");
		result.setIcon(GenericPluginAdapter.getAddonIcon());
		// makes no sense, will be chacked when clicking on button
		// if (AddonManagerPlugin.getInstance() == null) {
		// result.setEnabled(false);
		// result.setText("<html>" + result.getText() + "<br>(Add-on manager plugin not available)");
		// }
		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddonManagerPlugin p = AddonManagerPlugin.getInstance();
				if (p == null)
					MainFrame.showMessageDialog("Addon-Manager Plugin not loaded on startup. Please restart application.",
							"Internal Error");
				else
					p.showManageAddonDialog();
			}
		});

		final String oldText = result.getText();

		FileDrop.Listener fdl = new FileDrop.Listener() {
			public void filesDropped(File[] files) {
				if (files != null && files.length > 0) {
					for (File f : files)
						if (!f.getName().toLowerCase().endsWith(".jar")) {
							result.setText("<html>Some Files are not a valid Add-on!");
							Timer t = new Timer(5000, new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									result.setText(oldText);
								}
							});
							t.start();
							break;
						}
					if (AddonManagerPlugin.getInstance() == null) {
						MainFrame.showMessageDialog("Addon-Manager Plugin not loaded.", "Could not install Add-on(s)");
					} else
						AddonManagerPlugin.getInstance().process(Arrays.asList(files));
				}
			}
		};

		Runnable dragdetected = new Runnable() {
			public void run() {
				result.setText("<html><br><b>Drop file to install Add-on<br><br>");
			}
		};

		Runnable dragenddetected = new Runnable() {
			public void run() {
				if (!result.getText().contains("!"))
					result.setText(oldText);
			}
		};
		new FileDrop(null, result, null, false, fdl, dragdetected, dragenddetected);

		return result;
	}

	private JButton getPreferencesFolderButton() {
		JButton result = new JMButton("<html>Show Preferences Folder");

		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPreferencesFolder();
			}
		});
		return result;
	}


	public static void showPreferencesFolder() {
		MainFrame.showMessageDialog("<html>" + "The application preferences folder will be opened in a moment.<br>"
				+ "This folder contains downloaded database files, stored quick-searches,<br>"
				+ "network download cache files, and program settings files.<br>"
				+ "Quick searches (created with 'Edit/Search...': 'Create new menu command')<br>"
				+ "are stored as files the file name extension '.bsh'. Such a file may be<br>"
				+ "deleted, in case the custom search command is not needed any more.", "Information");
		BackgroundTaskHelper.executeLaterOnSwingTask(2000, new Runnable() {
			public void run() {
				AttributeHelper.showInBrowser(ReleaseInfo.getAppFolder());
			}
		});
	}


	@Override
	public boolean isEnabled() {
		return true;
	}

}
