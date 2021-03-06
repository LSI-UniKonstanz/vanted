/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Mar 3, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;

/**
 * @author klukas
 */
public class RimasTab extends InspectorTab implements PreferencesInterface {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9077025357135383654L;
	
	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}
	
	public RimasTab() {
		setLayout(new TableLayout(new double[][] { { 5, TableLayout.FILL, 5 }, { TableLayout.PREFERRED, 5,
				TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 5, TableLayout.FILL } }));
		
		add(new JLabel("<html>" + "<h2>RIMAS SBGN maps</h2>"), "1,0");
		add(new JLabelHTMLlink("<html><u>R</u>egulatory <u>I</u>nteraction <u>M</u>aps of <u>A</u>rabidopsis <u>S</u>eed Development<br><br>",
				"https://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/rimas"), "1,2");
		add(new JLabel(
				"<html>RIMAS is a web-based information portal which provides a comprehensive overview of regulatory pathways and genetic interactions during Arabidopsis embryo and seed development."),
				"1,4");
		add(new JLabel("<html>" + "Download or switch to a map:"), "1,6");
		add(getRIMASdownloadGUI(), "1,8");
	}
	
	@Override
	public List<Parameter> getDefaultParameters() {
		ArrayList<Parameter> arrayList = new ArrayList<Parameter>();
		arrayList.add(new BooleanParameter(true, PREFERENCE_TAB_SHOW,
				"Enable/Disable this option to show/hide the RIMAS Tab"));
		return arrayList;
	}
	
	@Override
	public void updatePreferences(Preferences preferences) {
	}
	
	@Override
	public String getPreferencesAlternativeName() {
		return "Rimas";
	}
	
	private JComponent getRIMASdownloadGUI() {
		String i = "<font color='gray'>@";
		ArrayList<JComponent> pathways = new ArrayList<JComponent>();
		pathways.add(TableLayout.get3Split(
				getPathwayButton("http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/rimas/Pathways/AFLB3LEC1.gml", "LEC1/AFL-B3 network",
						"s_lec1aflb3network.png"),
				new JLabel(), new JLabelHTMLlink(i, "http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/rimas/AFLB3network.htm"),
				TableLayout.FILL, 5, TableLayout.PREFERRED));
		pathways.add(TableLayout.get3Split(
				getPathwayButton("http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/rimas/Pathways/AFLB3%20maturation.gml",
						"LEC1/AFL-B3 factors and maturation gene control", "s_lec1aflb3_maturation.png"),
				new JLabel(), new JLabelHTMLlink(i, "http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/rimas/AFLB3maturation.htm"),
				TableLayout.FILL, 5, TableLayout.PREFERRED));
		pathways.add(TableLayout.get3Split(
				getPathwayButton("http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/rimas/Pathways/AFLB3%20hormones.gml",
						"LEC1/AFL-B3 factors and interactions with phytohormone metabolism",
						"s_lec1aflb3_hormones.png"),
				new JLabel(), new JLabelHTMLlink(i, "http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/rimas/AFLB3hormones.htm"),
				TableLayout.FILL, 5, TableLayout.PREFERRED));
		pathways.add(TableLayout.get3Split(
				getPathwayButton("http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/rimas/Pathways/seed%20reg%20epigenetics%202-FS.gml",
						"Epigenetic control of LEC1/AFL-B3 factors", "s_lec1aflb3_epigenetics.png"),
				new JLabel(), new JLabelHTMLlink(i, "http://kim25.wwwdns.kim.uni-konstanz.de/vanted/addons/rimas/AFLB3epigenetics.htm"),
				TableLayout.FILL, 5, TableLayout.PREFERRED));
		return TableLayout.getMultiSplitVertical(pathways, 5);
	}
	
	public static ImageIcon getIcon(String fn) {
		ClassLoader cl = RimasTab.class.getClassLoader();
		String path = RimasTab.class.getPackage().getName().replace('.', '/');
		try {
			ImageIcon i = new ImageIcon(cl.getResource(path + "/images/" + fn));
			return i;
		} catch (Exception e) {
			return null;
		}
	}
	
	protected JComponent getPathwayButton(final String url, String title, String image) {
		JButton res = new JButton("<html>" + title);
		// res.setIcon(getIcon(image));
		res.setOpaque(false);
		res.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!MainFrame.getInstance().lookUpAndSwitchToNamedSession(url.replaceAll("%20", " "))) {
					try {
						Graph g = MainFrame.getInstance()
								.getGraph(url.substring(url.lastIndexOf("/")).replaceAll("%20", " "), new URL(url));
						MainFrame.getInstance().showGraph(g, e);
					} catch (MalformedURLException e1) {
						ErrorMsg.addErrorMessage(e1);
					}
				}
			}
		});
		return res;
	}
	
	@Override
	public String getName() {
		return getTitle();
	}
	
	@Override
	public String getTitle() {
		return "RIMAS";
	}
	
	@Override
	public String getTabParentPath() {
		return "Pathways";
	}
	
	@Override
	public int getPreferredTabPosition() {
		return InspectorTab.TAB_LEADING;
	}
}
