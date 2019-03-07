// ==============================================================================
//
// HTMLSupport.java
//
// Copyright (c) 2017-2019, University of Konstanz
//
// ==============================================================================
package org.vanted.scaling.scalers.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;
import org.vanted.scaling.ComponentRegulator;

/**
 * It handles several HTML tags, see {@linkplain Tags#ALL} for detailed list.
 * These are problematic, because they do not respect a font change on its own
 * and are replaced by a calculated font size, e.g. &#60;font size="-1">. With
 * the help of a {@link TextListener} we dynamic changes are tracked.
 * 
 * @see {@link ComponentRegulator#scaleHTML(JComponent)}
 * 
 * @author D. Garkov
 */
class HTMLSupport {

	/** A listener for text changes to HTML-styled components. */
	private static PropertyChangeListener textListener = new TextListener();

	/**
	 * Holding the native tags of the HTML-formatted texts. The String is encoded
	 * with information regarding all contained &#60;small> and &#60;big> tags.
	 */
	private static HashMap<Integer, List<String>> tags = new HashMap<>();

	private HTMLSupport() {
	}

	/**
	 * Checks whether the given text uses font modifying HTML tags in its
	 * specification. HTML-formatted strings have &#60;small> and &#60;big> tags.
	 * 
	 * @param text the String to be parsed
	 * 
	 * @return true if styled with font-modifying tags
	 */
	static boolean isHTMLStyled(String text) {
		if (text != null && text.toLowerCase(Locale.ROOT).contains("<html>") && Tags.ALL.isTagged(text))
			return true;

		return false;
	}

	/**
	 * Stores all used tags of the form &#60;small> or &#60;big>, while preserving
	 * the order. Supporting data structure: {@link HTMLSupport#tags}. As key is
	 * used the hash-code of the given component, thus identifying it uniquely
	 * during a working session. It also supports more than one HTML text per
	 * component.
	 * <p>
	 * 
	 * Example:<br>
	 * <br>
	 * &#60;html>&#60;big>3&#60;small>&#60;br>&nbsp;nodes<br>
	 * &#60;big>&#60;small>
	 * 
	 * @param component used to get the key
	 * @param text      used to get the tags
	 */
	static void storeTags(JComponent component, String text) {
		if (Tags.SPECIAL.isTagged(text) && !tags.containsKey(component.hashCode())) {

			/*---Construct value---*/
			StringBuilder value = new StringBuilder();

			Map<String, Integer> occurrenceIndexer = Tags.SPECIAL.putAllValues(new HashMap<String, Integer>(), -1);
			boolean refresh = true;
			text = text.toLowerCase(Locale.ROOT);
			while (refresh) {
				refresh = false;
				for (String tag : occurrenceIndexer.keySet()) {
					int i = text.indexOf(tag);
					if (i < 0)
						occurrenceIndexer.put(tag, Integer.MIN_VALUE);
					else
						occurrenceIndexer.put(tag, i);

					refresh = (refresh == true) ? true : i != text.lastIndexOf(tag);
				}

				String tag = getLeastValuedKey(occurrenceIndexer);
				value.append(tag);

				text = text.substring(text.indexOf(tag));
				text = text.replaceFirst(tag, "");
			}

			/*---Insert kv-pair---*/
			int key = component.hashCode();
			List<String> list = tags.containsKey(key) ? tags.get(key) : new LinkedList<String>();
			list.add(value.toString());
			tags.put(key, list);
		}
	}

	private static String getLeastValuedKey(Map<String, Integer> map) {
		String first = "";
		int min = Integer.MAX_VALUE;
		for (Entry<String, Integer> e : map.entrySet()) {
			int v = e.getValue();
			if (v >= 0 && v < min) {
				min = v;
				first = e.getKey();
			}
		}

		return first;

	}

	/**
	 * This method replaces the tags, or adds right after them, with a calculated
	 * value according the current font size. If the font size is default, no
	 * replacement takes place, even the opposite, if previously replaced, they are
	 * now reset to reflect original look.
	 * <p>
	 * 
	 * Example(size is 6px):<br>
	 * <br>
	 * <code>&#60;html>&#60;small>A footnote</code><br>
	 * <code>&#60;html>&#60;font size="-1>A footnote</code><br>
	 * <p>
	 * 
	 * 
	 * Difference between &#60;small> and &#60;big> tags in font size is 2.
	 * 
	 * @see HTMLSupport#getFontFactor(JComponent, float)
	 * @see HTMLSupport#modifyTag(String, int)
	 * 
	 * @param text      to be parsed
	 * @param component owner of the text
	 * 
	 * @return String with replaced and processed according to the given font size
	 *         tags
	 */
	static String parseHTMLtoFontSize(String text, JComponent component) {
		int factor = HTMLSupport.getFontFactor(component, 12f);
		String pattern = "<FONT SIZE=\"";

		// Restore initial tagging
		if (factor == Integer.MAX_VALUE) {
			if (text.indexOf(pattern) == -1)
				return text;
			else {
				String[] parts = text.split(pattern);
				String[] initialTags = restoreTags(component);
				StringBuilder textBuilder = new StringBuilder(parts[0]);
				for (int i = 0, j = 0; i < parts.length - 1; i++, j++) {
					boolean wasReplaced = j < initialTags.length && !Tags.ADDITIVE.values.contains(initialTags[j]);

					textBuilder.append(wasReplaced ? initialTags[j] : "");
					textBuilder.append(parts[i + 1].replaceAll("^(-|\\+)*[1-9]+\">", ""));
				}

				return textBuilder.toString();
			}
		}

		// Insert font modifiers accordingly
		DecimalFormat df = new DecimalFormat("+#;-#");
		if (text.indexOf(pattern) == -1) { // from standard to emulated
			for (String tag : Tags.ALL.values)
				text = StringUtils.replace(text, tag,
						(Tags.ADDITIVE.values.contains(tag)) ? 
								tag + pattern + df.format(modifyTag(tag, factor)) + "\">" :
								pattern + df.format(modifyTag(tag, factor)) + "\">");
			return text;
		} else { // from emulated to emulated
			return text.replaceAll(pattern + "(-|\\+)*[1-9]+", pattern + df.format(factor));
		}
	}

	private static int modifyTag(String tag, int factor) {
		switch (tag) {
		case "<big>":
			return factor += 2;
		case "<small>":
			return factor -= 2;

		// add other modifiers here
		}

		return factor;
	}

	private static int getFontFactor(JComponent component, float anchorSize) {
		int font = component.getFont().getSize();
		float _DEFAULT_SIZE = anchorSize;
		float ratio = font / _DEFAULT_SIZE;

		// no emulated DPI => no change
		if (ratio == 1f)
			return Integer.MAX_VALUE;

		boolean negative = false;
		int factor = 0;

		// lower emulated DPI => bigger font size
		if (ratio > 1) {
			negative = false;
			float f = font;
			while (f > _DEFAULT_SIZE) {
				f = f / 2.0f;
				factor += 1;
			}
			// higher emulated DPI => smaller font size
		} else {
			negative = true;
			float f = font;

			factor = 1; // bias
			while (f < _DEFAULT_SIZE) {
				f += 2;
				factor += 1;
			}
		}

		if (negative)
			factor *= -1;

		return factor;
	}

	/**
	 * Adds new TextListener or removes previously added TextListener.
	 * <p>
	 * 
	 * A simple boolean flag is used, because of some memory overhead. The more
	 * intuitive way would be to use a supporting data structure and remove if
	 * previously added, but this may bring some unwanted overhead.
	 * 
	 * @param component
	 * @param remove    true to remove previously added listener
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
	 * This method restores the previously stored tags in an array form, see also
	 * {@link HTMLSupport#storeTags(JComponent, String)}, and preserves their
	 * initial order.
	 * 
	 * @param component to get native tags back
	 * 
	 * @return an array with the native tags in order of appearance
	 */
	static String[] restoreTags(JComponent component) {

		if (tags.get(component.hashCode()).isEmpty())
			return new String[] { "" };

		String value = tags.get(component.hashCode()).get(0);
		final String del = "<";

		ArrayList<String> tagslist = new ArrayList<>(Arrays.asList(value.split(del)));
		tagslist.removeAll(Collections.singleton(""));
		String[] result = tagslist.toArray(new String[tagslist.size()]);
		for (int i = 0; i < result.length; i++)
			result[i] = del + result[i];

		// remove processed value from the map's list
		tags.get(component.hashCode()).remove(0);

		return result;
	}

	/**
	 * Listener for any changes on a Swing Component regarding its text. See
	 * {@link HTMLSupport#handleTextListener(JComponent, boolean)} for usage.
	 * 
	 * @author D. Garkov
	 *
	 */
	private static class TextListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			ComponentRegulator.scaleHTML((JComponent) evt.getSource());
		}

	}

	public enum Tags {
		ALL("ALL", "<small>", "<big>", "<font size=\"", "<code>"),

		SPECIAL("SPECIAL", "<small>", "<big>", "<code>"),
		// we don't replace these tags, but add font modifier right after
		ADDITIVE("ADDITIVE", "<code>");

		private final Set<String> values;

		private Tags(String type, String... values) {
			this.values = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(values)));
		}

		public boolean isTagged(String text) {
			for (String tag : values)
				if (text.toLowerCase(Locale.ROOT).contains(tag))
					return true;

			return false;
		}

		public <T> Map<String, T> putAllValues(Map<String, T> map, T defaultValue) {
			for (String s : values)
				map.put(s, defaultValue);

			return map;
		}
	}
}