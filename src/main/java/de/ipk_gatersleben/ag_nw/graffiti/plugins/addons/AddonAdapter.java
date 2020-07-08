package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.GenericPluginAdapter;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;

/**
 * Use to set up the Add-on initialization and icon.
 * 
 * @author Hendrik Rohn
 * @vanted.revision 2.6.5
 */
public abstract class AddonAdapter extends IPK_EditorPluginAdapter {

	/**
	 * Default constructor. Does initialization of the Add-on.
	 * 
	 * @throws AddonInstantiationRuntimeException
	 *             when Add-on couldn't be initialized
	 */
	public AddonAdapter() throws AddonInstantiationRuntimeException {
		super();
		try {
			initializeAddon();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("<html>Exception occured when initializing Add-on \""
					+ getClass().getCanonicalName() + "\":<br>" + e.getLocalizedMessage());

			throw new AddonInstantiationRuntimeException(e);
		}
	}

	/**
	 * Implement this inherited method to initialize the Add-on and possibly any
	 * related components.
	 */
	protected abstract void initializeAddon();

	/**
	 * Override to add custom Add-on icon.
	 */
	@Override
	public ImageIcon getIcon() {
		try {
			ImageIcon icon = new ImageIcon(
					GravistoService.getResource(GenericPluginAdapter.class, "addon-icon", "png"));

			return icon;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);

			return super.getIcon();
		}
	}
}
