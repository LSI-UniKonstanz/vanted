package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.algorithm.Algorithm;

import javax.swing.*;

/**
 * See the documentation for {@link AddonAdapter}.
 * @see AddonAdapter
 */
public class MultilevelFrameworkAddon extends AddonAdapter {

    // not final to allow test to change it
    static String ICON_NAME = "mlf-logo";

    @Override
    protected void initializeAddon() {
        this.algorithms = new Algorithm[]{
                // The MLF plugin uses core methods to iterate over all available
                // plugins and determine which can be used in the MLF procedure.
                // cf LayoutAlgorithmWrapper#getPluginLayoutAlgs.
                // registering a layout algorithm here (in this addon) would not
                // make it usable in the MLF.
                // To do so, see LayoutAlgorithmWrapper#getSuppliedLayoutAlgs
                // and LayoutAlgorithmWrapper#layoutAlgWhitelist
                new MultilevelFrameworkLayouter()
        };
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
