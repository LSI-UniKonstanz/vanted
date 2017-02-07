package org.vanted.scaling;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.Icon;
/**
 * All scalers should implement this interface. Alternatively, one could more
 * conveniently just extends the default scaler - {@link BasicScaler}. 
 * 
 * @author dim8
 *
 */
public interface Scaler {
	
	/**
	 * Used for synchronized LAFs, where only one Font change is sufficient
	 * (e.g. Nimbus). */
	void initialScaling();
	/**
	 * 
	 * @param key UIDefaults key
	 * @param original instance to be scaled
	 * 
	 * @return newly scaled instance
	 */
	Font modifyFont(Object key, Font original);
	
	/**
	 * 
	 * @param key UIDefaults key
	 * @param original instance to be scaled
	 * 
	 * @return newly scaled instance
	 */
	Icon modifyIcon(Object key, Icon original);
	
	/**
	 * 
	 * @param key UIDefaults key
	 * @param original instance to be scaled
	 * 
	 * @return newly scaled instance
	 */
	Integer modifyInteger(Object key, Integer original);

	/**
	 * 
	 * @param key UIDefaults key
	 * @param original instance to be scaled
	 * 
	 * @return newly scaled instance
	 */
	Insets modifyInsets(Object key, Insets original);

}