/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.ErrorMsg;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class TranspathXMLparser extends DefaultHandler {
	
	Map<String, TranspathEntityType> entries;
	
	String activeEnvironment = "";
	
	String entitystartEndTag = null;
	
	Class<?> entityType;
	
	TranspathEntityType currentEntity = null;
	
	public TranspathXMLparser(Map<String, TranspathEntityType> entries, Class<?> entityType, String entitystartEndTag) {
		this.entries = entries;
		this.entityType = entityType;
		this.entitystartEndTag = entitystartEndTag;
	}
	
	@Override
	public void startElement(String uri, String localName, String qname, Attributes attr) {
		if (entitystartEndTag.equals(qname)) {
			activeEnvironment = "";
			try {
				currentEntity = (TranspathEntityType) entityType.getDeclaredConstructor().newInstance();
			} catch (InstantiationException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (IllegalAccessException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (IllegalArgumentException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (InvocationTargetException e) {
				ErrorMsg.addErrorMessage(e);
				ErrorMsg.addErrorMessage(e);
			} catch (SecurityException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (NoSuchMethodException e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else
			activeEnvironment += "/" + qname;
	}
	
	@Override
	public void endElement(String uri, String localName, String qname) {
		if (activeEnvironment.endsWith("/" + qname))
			activeEnvironment = activeEnvironment.substring(0,
					activeEnvironment.length() - "/".length() - qname.length());
		if (currentEntity != null && qname.equals(entitystartEndTag)) {
			entries.put(currentEntity.getKey(), currentEntity);
			currentEntity = null;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) {
		String s = new String(ch, start, length).trim();
		if (currentEntity != null && activeEnvironment != null && activeEnvironment.length() > 0 && s.length() > 0)
			currentEntity.processXMLentityValue(activeEnvironment, s);
	}
}
