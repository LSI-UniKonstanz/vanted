package org.graffiti.options;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import net.iharder.dnd.FileDrop;

import org.AttributeHelper;
import org.JMButton;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.GenericPluginAdapter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonManagerPlugin;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * 
 * @vanted.revision 2.7
 */
public class AddonsOptionsPane extends AbstractOptionPane {

	private static final long serialVersionUID = 1L;
	private static double border = 5;

	/**
	 * @vanted.revision 2.7
	 */
	protected AddonsOptionsPane() {
		super("Add-on Manager");
	}

	@Override
	public String getCategory() {
		return null;
	}

	/**
	 * @vanted.revision 2.7
	 */
	@Override
	public String getOptionName() {
		return "Add-on Manager";
	}

	@Override
	public JComponent getOptionDialogComponent() {
		return super.getOptionDialogComponent();
	}

	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	protected void initDefault() {
		createUI();
	}

	@Override
	protected void saveDefault() {
	}

	/**
	 * @vanted.revision 2.7
	 */
	private void createUI() {
		double[][] size = {
				 // Columns
				{ border, TableLayoutConstants.FILL, border },
				 // Rows
				{ border, TableLayoutConstants.PREFERRED, border }
		};
		setLayout(new TableLayout(size));

		
		add(TableLayout.get4SplitVertical(
				new JLabel(getAddonManagerInfo()), getAddOnManagerButton(),
				new JLabel(getPreferencesFolderInfo()), getPreferencesFolderButton(),
				TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 0, 0), "1,1");
	}

	/**
	 * @vanted.revision 2.7
	 */
	private JButton getAddOnManagerButton() {
		final JButton result = new JMButton("<html>Install / Configure Add-ons");
		result.setIcon(GenericPluginAdapter.getAddonIcon());
		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddonManagerPlugin p = AddonManagerPlugin.getInstance();
				if (p == null)
					MainFrame.showMessageDialog(
							"Addon-Manager Plugin not loaded on startup. Please restart application.",
							"Internal Error");
				else
					p.showManageAddonDialog(SwingUtilities.windowForComponent(AddonsOptionsPane.this));
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
		JButton result = new JMButton("<html>Open Preferences Folder");

		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openPreferencesFolder();
			}
		});
		return result;
	}

	/**
	 * @since 2.7
	 */
	private static String getAddonManagerInfo() {
		return "<html>"
				+ "Add-on Manager helps find and install new add-ons; configure and update installed add-ons.<br><br>"
				+ "Add-ons can also be temporarily deactivated. To remove add-ons, open the add-on folder<br>"
				+ " (<i>Install / Configure Add-ons</i>  &#8594; <i>Open Add-on Folder</i>) and delete them from there.";
	}
	
	/**
	 * 
	 * @return
	 * @since 2.7
	 */
	private static String getPreferencesFolderInfo() {
		/*network downloads cache files -> cache files*/
		return "<html><br>"
				+ "Preferences folder contains stored quick searches, cache files, database and settings files.<br><br>"
				+ "Quick-search commands (<i>Edit</i> &#8594; <i>Search...</i>&#8594; <i>Create new menu command</i>) are stored as '.bsh'.<br>"
				+ "BSH files can be deleted from the file system, once the command is not needed anymore.";
	}

	/**
	 * @vanted.revision 2.7
	 */
	public static void openPreferencesFolder() {
		BackgroundTaskHelper.executeLaterOnSwingTask(100, new Runnable() {
			public void run() {
				AttributeHelper.showInBrowser(ReleaseInfo.getAppFolder());
			}
		});
	}
}
