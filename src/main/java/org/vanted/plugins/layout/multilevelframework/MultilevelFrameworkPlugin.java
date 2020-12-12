package org.vanted.plugins.layout.multilevelframework;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

import javax.swing.*;

/**
 * 
 * Since 2.8.0 the Multilevel Framework add-on is integrated into the core of
 * Vanted as Vanted plug-in.
 * 
 * @since 2.8.0
 */
public class MultilevelFrameworkPlugin extends GenericPluginAdapter {

	// not final to allow test to change it
	static String ICON_NAME = "mlf-logo";

	public MultilevelFrameworkPlugin() {
		super();

		this.algorithms = new Algorithm[] {
				// The MLF plugin uses core methods to iterate over all available
				// plugins and determine which can be used in the MLF procedure.
				// cf LayoutAlgorithmWrapper#getPluginLayoutAlgs.
				// registering a layout algorithm here (in this addon) would not
				// make it usable in the MLF.
				// To do so, see LayoutAlgorithmWrapper#getSuppliedLayoutAlgs
				// and LayoutAlgorithmWrapper#layoutAlgWhitelist
				new MultilevelFrameworkLayout() };
	}

	@Override
	public ImageIcon getIcon() {
		try {
			return new ImageIcon(GravistoService.getResource(this.getClass(), ICON_NAME, "png"));
		} catch (Exception e) {
			return super.getIcon();
		}
	}

}
