package org.graffiti.options;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.Timer;

import net.iharder.dnd.FileDrop;

import org.AttributeHelper;
import org.JMButton;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.GenericPluginAdapter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonManagerPlugin;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class AddonsOptionsPane extends AbstractOptionPane{

	private static double border = 5;
	
	protected AddonsOptionsPane() {
		super("Addon Manager");
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOptionName() {
		// TODO Auto-generated method stub
		return "Addon Manager";
	}

	@Override
	public JComponent getOptionDialogComponent() {
		// TODO Auto-generated method stub
		return super.getOptionDialogComponent();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}

	@Override
	protected void initDefault() {
		createUI();
	}

	@Override
	protected void saveDefault() {
		// TODO Auto-generated method stub
		
	}
	
	private void createUI() {
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, 2 * border, TableLayoutConstants.PREFERRED, 2 * border,
					TableLayoutConstants.PREFERRED, 2 * border, TableLayoutConstants.PREFERRED, 2 * border,
				} }; // Rows
		setLayout(new TableLayout(size));

		
		add(TableLayout.get3Split(getAddOnManagerButton(), null, getPreferencesFolderButton(), TableLayout.FILL, 4,
				TableLayout.FILL, 0, 0), "1,1");
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
}
