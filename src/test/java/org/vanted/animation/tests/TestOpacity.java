/**
 * 
 */
package org.vanted.animation.tests;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

/**
 * @author matthiak
 */
public class TestOpacity extends EditorPluginAdapter {

	/**
	 * 
	 */
	public TestOpacity() {
		algorithms = new Algorithm[] { new AnimatorTestSuit(), new RainbowSwappingTest(), new VantedSentenceTest(),
				new OpacityAlgorithm() };
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main.startVanted(args, "org/vanted/animation/tests/TestOpacity.xml");
	}

}
