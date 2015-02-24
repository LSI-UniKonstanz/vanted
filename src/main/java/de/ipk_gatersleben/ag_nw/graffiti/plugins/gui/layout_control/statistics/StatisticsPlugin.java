/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.inspector.InspectorTab;

/**
 * @author matthiak
 *
 */
public class StatisticsPlugin extends EditorPluginAdapter{

	/**
	 * 
	 */
	public StatisticsPlugin() {
		this.tabs = new InspectorTab[]{
				new TabStatisticCompareSamples(),
				new TabStatisticsScatterMatrix(),
				new TabStatisticsCorrOneToN(),
				new TabStatisticsCorrNToN()
		};
	}
}
