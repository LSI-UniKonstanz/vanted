package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.algorithm.Algorithm;

import javax.swing.*;

public class MultilevelFrameworkAddon extends AddonAdapter {

    // not final to allow test to change it
    static String ICON_NAME = "mlf-logo";

    @Override
    protected void initializeAddon() {
        this.algorithms = new Algorithm[]{
                new MultilevelFrameworkLayouter(),
        };
    }

    @Override
    public ImageIcon getIcon() {
        try {
            // TODO maybe edit the logo
            return new ImageIcon(GravistoService.getResource(this.getClass(), ICON_NAME, "png"));
        } catch (Exception e) {
            return super.getIcon();
        }
    }

}
