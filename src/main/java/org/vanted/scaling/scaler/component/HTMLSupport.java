package org.vanted.scaling.scaler.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;

import org.vanted.scaling.ComponentRegulator;

/**
 * More precisely handles <small> and <big> HTML tags. These are problematic,
 * because they do not respect a font change on its own. So we replace them
 * and achieve a similar look using &#60;font size="-1"> with accordingly 
 * processed value. With the help of a {@link TextListener} we are able to
 * also detect dynamic changes.
 * 
 * @param component containing HTML-styled tags to scale
 * 
 * @see {@link ComponentRegulator#scaleHTML(JComponent)}
 * 
 * @author dim8
 */
class HTMLSupport {
	
	/**A listener for text changes to HTML-styled components. */
	private static PropertyChangeListener textListener = new TextListener();
	
	/**Holding the native tags of the HTML-formatted texts.
	 * The String is encoded with information regarding all
	 * contained &#60;small> and &#60;big> tags. */
	private static HashMap<Integer, List<String>> tags = new HashMap<>();

	private HTMLSupport() {}
	
	/**
	 * Checks whether the given text uses font modifying HTML tags in its 
	 * specification. HTML-formatted strings have &#60;small> and &#60;big> tags.
	 * 
	 * @param text the String to be parsed
	 * 
	 * @return true if styled with font-modifying tags
	 */
	static boolean isHTMLStyled(String text) {
		//text, lowered
		String tlow = (text == null) ? null : text.toLowerCase(Locale.ROOT);
		String fontsize = "<font size=\"";
		
		if (text != null && tlow.contains("<html>") && 
				(tlow.contains("<small>") || tlow.contains("<big>") ||
						tlow.contains(fontsize)))
			return true;
		
		return false;
	}

	/**
	 * Stores all used tags of the form &#60;small> or &#big>, while 
	 * preserving the order. The data structures that holds them is
	 * the field {@link HTMLSupport#tags}. As key is used the hash-code
	 * of the given component, thus identifying it uniquely during 
	 * each working session.<p>
	 * 
	 * Example:<br><br>
	 * &#60;html>&#60;big>3&#60;small>&#60;br>&nbsp;nodes<br>
	 * &#60;big>&#60;small>
	 * 
	 * @param component used to get the key
	 * @param text used to get the tags
	 */
	static void storeTags(JComponent component, String text) {
		//text, lowered
		String tlow = (text == null) ? null : text.toLowerCase(Locale.ROOT);
		
		if ((tlow.contains("<small>") || tlow.contains("<big>")) && 
				!tags.containsKey(component.hashCode())) {

			/*---Construct value---*/
			String value = "";
			
			while (text.length() > 0) {
				//test for 'small' before 'big' or 'small' all alone
				if (text.indexOf("<small>") < text.indexOf("<big>") || 
						(text.contains("<small>") && !text.contains("<big>"))) {
					value += "<small>";
					text = text.substring(text.indexOf("<small>"));
					text = text.replaceFirst("<small>", "");
				}
				
				//analogously
				if (text.indexOf("<big>") < text.indexOf("<small>") || 
						(text.contains("<big>") && !text.contains("<small>"))) {
					value += "<big>";
					text = text.substring(text.indexOf("<big>"));
					text = text.replaceFirst("<big>", "");
				}
				
				//stopping criterion
				if (!text.contains("<small>") && !text.contains("<big>"))
					break;
			}
			
			/*---Insert kv-pair---*/
			int key = component.hashCode();
			List<String> list = 
					tags.containsKey(key) ? tags.get(key) : new LinkedList<String>();
			list.add(value);
			tags.put(key, list);
		}
	}
	
	/**
	 * This method replaces the tags <small> and <big> with a calculated
	 * value according the current font size. If the font size is default,
	 * no replacement takes place, even the opposite, if previously replaced,
	 * they are now reset. This arises from the fact that the above mentioned 
	 * tags do not respect a font change.<p>
	 * 
	 * Example(size is 6px):<br><br>
	 * <code>&#60;html>&#60;small>A footnote</code><br>
	 * <code>&#60;html>&#60;font size="-1>A footnote</code><br><p>
	 * 
	 * 
	 * Difference between &#60;small> and &#60;big> tags in font size is 2.
	 * @param text to be parsed
	 * @param component owner of the text
	 * 
	 * @return String with replaced and processed according to the given font
	 *  size tags
	 */
	static String parseHTMLtoFontSize(String text, JComponent component) {
		float _DEFAULT_SIZE = 12f;
		int font = component.getFont().getSize();
		float ratio = font / _DEFAULT_SIZE;

		String sign = "";
		int factor = 0;
		
		//lower emulated DPI => bigger font size 
		if (ratio > 1) {
			sign = "+";
			float f = font;
			while (f > _DEFAULT_SIZE) {
				f = f / 2.0f;
				factor += 1;
			}
		//higher emulated DPI => smaller font size
		} else if (ratio < 1) {
			sign = "-";
			float f = font;
			
			//normalize the factor (no side effects)
			factor = 1;
			while (f < _DEFAULT_SIZE) {
				f += 2;
				factor += 1;
			}
		//no emulated DPI => no change
		} else
			factor = -1;
		
		String pattern = "<FONT SIZE=\"";
			
		if (factor == -1) {
			if (text.indexOf(pattern) == -1)
				return text;
			else {
				String[] parts = text.split(pattern);
				String[] initialTags = parseTagValues(component);
				for (int i = 0, j = 0; i < parts.length - 1; i += 2, j++) {
					text = parts[i] + (j < initialTags.length ? initialTags[j]
											: "");
					
					text = text + parts[i+1].replaceAll("^(-|\\+)*[1-9]{1}\">", "");
				}

				return text;
			}
		}
		
		if (text.indexOf(pattern) == -1) {
			//Replace all <small>, given any
			text = text.replaceAll("<small>", pattern + sign + factor +"\">");
			//Replace all <big>, given any and adjust factor accordingly
			factor += 2;
			text = text.replaceAll("<big>", pattern + sign + factor +"\">");			
		} else {
			String[] parts = text.split(pattern);
			for (int i = 0; i < parts.length - 1; i += 2) {
				text = parts[i] + pattern;
				text = text + sign + factor //for big, factor has already been set accordingly
						+ parts[i+1].substring(parts[1].indexOf('\"'));
			}
		}
		
		return text;
	}

	/**
	 * Adds new TextListener or removes previously added 
	 * TextListener.<p>
	 * 
	 * A simple boolean flag is used, because of
	 * some memory overhead. The more intuitive way would be to
	 * use a supporting data structure and remove if previously
	 * added, but this may bring some unwanted overhead.
	 * 
	 * @param component
	 * @param remove true to remove previously added listener
	 */
	static void handleTextListener(JComponent component, boolean remove) {
		
		/*------------------------------Removal------------------------------*/
		if (remove) {
			component.removePropertyChangeListener("text", textListener);

			return;
		}
		
		/*-----------------------------Addition-----------------------------*/
		component.addPropertyChangeListener("text", textListener);
	}
	
	/**
	 * This method restores the previously stored tags, see also
	 * {@link HTMLSupport#storeTags(JComponent, String)} for that, and
	 * preserves their initial order. So a simple replacement therewith
	 * may take place.
	 * 
	 * @param component to get native tags back
	 * 
	 * @return an array with the native tags in order of appearance
	 */
	static String[] parseTagValues(JComponent component) {
		
		if (tags.get(component.hashCode()).isEmpty())
			return new String[]{""};
		
		String value = tags.get(component.hashCode()).get(0);
		ArrayList<String> tagsList = new ArrayList<>();
	
		//iterate until all tags are placed in the list
		while (value.length() > 0) {
			if (value.startsWith("<small>")) {
				tagsList.add("<small>");
				value = value.replaceFirst("<small>", "");
			}
			
			if (value.startsWith("<big>")) {
				tagsList.add("<big>");
				value = value.replaceFirst("<big>", "");
			}
			
		}
		//remove processed value from the list
		tags.get(component.hashCode()).remove(0);
		
		//construct the array
		String[] array = new String[tagsList.size()];
		int i = 0;
		while (i < tagsList.size()) {
			array[i] = tagsList.get(i);
			i++;
		}
			
		return array;
	}
	
	/**
	 * Listener for any changes on a Swing Component regarding its text.
	 * See {@link HTMLSupport#handleTextListener(JComponent, boolean)} for 
	 * usage.
	 * 
	 * @author dim8
	 *
	 */
	private static class TextListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			ComponentRegulator.scaleHTML((JComponent) evt.getSource());
		}
		
	}

}