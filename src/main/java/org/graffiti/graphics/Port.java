// ==============================================================================
//
// Port.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Port.java,v 1.6 2010/12/22 13:05:33 klukas Exp $

package org.graffiti.graphics;

/**
 * A port comprises a name and a coordinate.
 */
public class Port {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private String name;
	
	/** DOCUMENT ME! */
	private double x;
	
	/** DOCUMENT ME! */
	private double y;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for Port.
	 * 
	 * @param name
	 *           DOCUMENT ME!
	 * @param x
	 *           DOCUMENT ME!
	 * @param y
	 *           DOCUMENT ME!
	 */
	public Port(String name, double x, double y) {
		this.name = name;
		this.x = x;
		this.y = y;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the name.
	 * 
	 * @param name
	 *           The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the x coordinate of this port.
	 * 
	 * @param x
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * Returns the x ccordinate of this port.
	 * 
	 * @return double
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Sets the y ccordinate of this port.
	 * 
	 * @param y
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * Returns the y ccordinate of this port.
	 * 
	 * @return double
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof Port)) {
			return false;
		}
		
		Port po = (Port) obj;
		
		return (name.equals(po.name) && (x == po.x) && (y == po.y));
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
