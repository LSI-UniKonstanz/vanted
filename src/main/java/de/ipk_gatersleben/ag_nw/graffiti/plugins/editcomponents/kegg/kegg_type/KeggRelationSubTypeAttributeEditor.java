/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggRelationSubTypeAttributeEditor
					extends AbstractValueEditComponent {
	
	protected ArrayList<? super JComponent> keggRelationSubTypeSelection = new ArrayList<>();
	
	public KeggRelationSubTypeAttributeEditor(final Displayable disp) {
		super(disp);
		String curValuesString = ((KeggRelationSubTypeAttribute) getDisplayable()).getString();
		String[] curValues = curValuesString.split(";");
		for (String curVal : curValues) {
			JLabel rsts = new JLabel();
			rsts.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
			rsts.setOpaque(false);
			rsts.setText(curVal);
			rsts.setPreferredSize(new Dimension(30, (int) rsts.getPreferredSize().getHeight()));
			keggRelationSubTypeSelection.add(rsts);
		}
	}
	
	@SuppressWarnings("unchecked")
	public JComponent getComponent() {
		JComponent res = TableLayout.getMultiSplit((ArrayList<JComponent>) keggRelationSubTypeSelection);
		res.setOpaque(false);
		return res;
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			for (Object jcb : keggRelationSubTypeSelection) {
				((JLabel)jcb).setText(EMPTY_STRING);
			}
		} else {
			keggRelationSubTypeSelection.clear();
			String curValuesString = ((KeggRelationSubTypeAttribute) getDisplayable()).getString();
			String[] curValues = curValuesString.split(";");
			for (String curVal : curValues) {
				JLabel rsts = new JLabel();
				rsts.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
				rsts.setText(curVal);
				rsts.setPreferredSize(new Dimension(20, (int) rsts.getPreferredSize().getHeight()));
				keggRelationSubTypeSelection.add(rsts);
			}
		}
	}
	
	public void setValue() {
		boolean isOneEmpty = false;
		for (Object jcb : keggRelationSubTypeSelection) {
			if (((JLabel)jcb).getText().equals(EMPTY_STRING))
				isOneEmpty = true;
		}
		if (!isOneEmpty) {
			String rval = "";
			for (Object jcb : keggRelationSubTypeSelection) {
				rval = rval + ((JLabel)jcb).getText() + ";";
			}
			if (rval.endsWith(";"))
				rval = rval.substring(0, rval.length() - 1);
			((KeggRelationSubTypeAttribute) displayable).setString(rval);
		}
	}
}
