package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import org.graffiti.managers.pluginmgr.RSSfeedDefinition;
@Deprecated
public interface ProvidesRSSfeed {

	RSSfeedDefinition getRSSfeed();

	boolean hasRSSfeedDefined();

}
