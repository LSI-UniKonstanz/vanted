package org.vanted.scaling;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import org.graffiti.editor.MainFrame;
import org.vanted.scaling.resources.ScaledFontUIResource;


/**
 * <i>Notice:</i> it is advisable to not access it directly, but through
 * the provided {@link ScalingCoordinator}, because of factor conversions.
 * 
 * This is the extra scaler, for scaling external to the LAF-defaults aspects
 * of components. It is a kind of co-scaler to any of the other types of Scalers.
 * It could be used for any user-related modifications that are not LAF-related. 
 * For example, user-specified icons are outside of scope of the LAF Defaults 
 * and thus may be extracted from the component tree and rescaled here. 
 * 
 * @param mainFrame the application's main frame to get the children 
 * 					components therewith.
 */
public class XScaler extends BasicScaler {

	/**A set holding the hash-codes of the components with modified fonts.*/
	private HashSet<Integer> modified;
	
	/**A listener for text changes to HTML-styled components. */
	private PropertyChangeListener textListener = new TextListener();
	
	/**Holding the native tags of the HTML-formatted texts.
	 * The String is encoded with information regarding all
	 * contained &#60;small> and &#60;big> tags. */
	private static HashMap<Integer, String> tags = new HashMap<Integer, String>();
	
	/** 
	 * Constructs a new XScaler for upcoming component co-scaling, of 
	 * non-LookAndFeel related specifics.
	 *  
	 * @param scaleFactor the working scaling factor, a ratio between 
	 * current actual DPI and to be emulated DPI.
	 * 
	 */
	public XScaler(float scaleFactor) {
		super(scaleFactor);
	}
	
	/**
	 * Scan and scale the defaults of the components placed in the specified
	 * {@link Container} <code>container</code>.
	 * 
	 * @param container, whose components are to be modified
	 * 
	 * @throws OutOfMemoryError when the allocated heap memory is not enough
	 * to hold all large scaled components, such as Icons. 
	 */
	public void init(Container container) throws OutOfMemoryError {
		if (container == null)
			return;
		
		//ensure some extra capacity beforehand
		System.gc();
		
		doExternalScaling(container);
	}
	
	/**
	 * Specify here application specific update of the 
	 * affected listeners, given there are any affected.
	 */
	public void notifyListeners() {
		//Vanted specific: update the state of the underlying Action
		MainFrame.getInstance().updateActions();
	}
	
	/**
	 * Worker scaling method. Consider using the utility method 
	 * {@link XScaler#init(Container)} instead.
	 *  
	 * @param c container, whose components are to be modified
	 * 
	 * @throws OutOfMemoryError when the allocated heap memory is not enough
	 * to hold all large scaled components, such as Icons. 
	 */
	public void doExternalScaling(Container c) throws OutOfMemoryError {		
		Container container;
		
		if (c instanceof Frame)
			container = ((JRootPane) ((Frame) c).getComponents()[0]);
		else
			container = c;
		
		scaleComponentsOf(container);
		
		//Update Listeners (app-specific)
		notifyListeners();
		
	}	

	private void scaleComponentsOf(Container container) {
		for (Component c : container.getComponents()) {
						
			//delegate further extraction
			if (c instanceof JComponent) {
				
				//TODO order
				
				//Font
				coscaleFont((JComponent) c);
				
				//Insets
				coscaleInsets((JComponent) c);
				
				//Icons
				coscaleIcons((JComponent) c);
				
			}
			
			//go further down recursively
			if (c instanceof Container)
				scaleComponentsOf((Container) c);
		}
	}

	/**
	 * Scales all components that have their font not modified by the
	 * LAF-Scalers for one reason or another up to this point. Additionally,
	 * it handles HTML-formatted strings having &#60;small> and &#60;big> tags.
	 * 
	 * @param component the JComponent, whose Font is to be scaled
	 */
	private void coscaleFont(JComponent component) {		
		/**
		 * We know that instances of ScaledFontUIResource are already
		 * processed. If not we compare the DPIs to make a conclusion.
		 * Then we modify accordingly.
		 */
		if (!(component.getFont() instanceof ScaledFontUIResource) || 
				(component.getFont() instanceof ScaledFontUIResource && 
						((ScaledFontUIResource) component.getFont()).getDPI() != 
						Toolkit.getDefaultToolkit().getScreenResolution() / scaleFactor)) {
			
			component.setFont(modifyFont(null, component.getFont()));
		}
		
		modifyHTMLFont(component);
	}
	
	/**
	 * More precisely handle <small> and <big> HTML tags. These are problematic,
	 * because they do not respect a font change on its own. So we replace them
	 * and achieve a similar look using &#60;font size="-1"> with accordingly 
	 * processed value. With the help of a {@link TextListener} we are able to
	 * also detect dynamic changes.
	 * 
	 * @param component containing HTML-styled tags to scale
	 * 
	 * @see {@link XScaler#isHTMLStyled(String)}
	 */
	private void modifyHTMLFont(JComponent component) {
		//the Component's text
		String t;
		
		if (component instanceof JLabel) {
			JLabel label = (JLabel) component;
			t = label.getText();

			processText(t, label);
		} if (component instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) component;
			t = button.getText();
			
			processText(t, button);
		} if (component instanceof JTextComponent) {
			JTextComponent text = (JTextComponent) component;
			t = text.getText();
			
			processText(t, text);
		}
	}

	/**
	 * Worker method, overloaded for {@link JLabel}.
	 * Processes the text, given it is HTML-styled, see 
	 * {@link XScaler#isHTMLStyled(String)}, by performing 
	 * parsing, substitution, removal and installation of {@link TextListener}
	 * and text setting, if necessary.
	 * 
	 * @param t text
	 * @param label JLabel
	 */
	private void processText(String t, JLabel label) {
		if (isHTMLStyled(t)) {
			//save the initial tags and their order for later 
			storeTags(label, t);
			//convert tags to font size tag
			t = parseHTMLtoFontSize(t, label);
			
			if (t.equals(label.getText()))
				return;
			
			//remove listener to avoid looping
			handleTextListener(label, true);
			
			label.setText(t);

			//install listener for subsequent dynamic changes
			handleTextListener(label, false);
		}

	}
	
	/**
	 * Worker method, overloaded for {@link AbstractButton}.
	 * Processes the text, given it is HTML-styled, see 
	 * {@link XScaler#isHTMLStyled(String)}, by performing 
	 * parsing, substitution, removal and installation of {@link TextListener}
	 * and text setting, if necessary.
	 * 
	 * @param t text
	 * @param button AbstractButton
	 */
	private void processText(String t, AbstractButton button) {
		if (isHTMLStyled(t)) {
			//save the initial tags for later
			storeTags(button, t);
			//convert tags to font size
			t = parseHTMLtoFontSize(t, button);
			
			if (t.equals(button.getText()))
				return;
			
			//remove listener to avoid looping
			handleTextListener(button, true);
			
			button.setText(t);
			
			//install listener for subsequent dynamic changes
			handleTextListener(button, false);
		}
	}
	
	/**
	 * Worker method, overloaded for {@link JTextComponent}.
	 * Processes the text, given it is HTML-styled, see 
	 * {@link XScaler#isHTMLStyled(String)}, by performing 
	 * parsing, substitution, removal and installation of {@link TextListener}
	 * and text setting, if necessary.
	 * 
	 * @param t text
	 * @param text JTextComponent
	 */
	private void processText(String t, JTextComponent text) {
		if (isHTMLStyled(t)) {
			//save the initial tags for later
			storeTags(text, t);
			//convert tags to font size
			t = parseHTMLtoFontSize(t, text);
			
			if (t.equals(text.getText()))
					return;
			
			//remove listener to avoid looping
			handleTextListener(text, true);
			
			text.setText(t);
			
			//install listener for subsequent dynamic changes
			handleTextListener(text, false);
		}
	}
	
	/**
	 * Checks whether the given text uses font modifying HTML tags in its 
	 * specification.
	 * 
	 * @param text the String to be parsed
	 * 
	 * @return true if styled with font-modifying tags
	 */
	private boolean isHTMLStyled(String text) {
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
	 * the field {@link XScaler#tags}. As key is used the hash-code
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
	private void storeTags(JComponent component, String text) {
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
			tags.put(component.hashCode(), value);
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
	private String parseHTMLtoFontSize(String text, JComponent component) {
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
	 * Adds a new TextListener or removes previously added 
	 * TextListener.<p>
	 * 
	 * A simple boolean to flag is used, because of
	 * some memory overhead. The more intuitive way would be to
	 * use a supporting data structure and remove if previously
	 * added. This, because of the constant handling of some of the
	 * components may bring some unwanted overhead.
	 * 
	 * @param component
	 * @param remove true to remove previously added listener
	 */
	private void handleTextListener(JComponent component, boolean remove) {
		
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
	 * {@link XScaler#storeTags(JComponent, String)} for that, and
	 * preserves their initial order. So a simple replacement therewith
	 * may take place.
	 * 
	 * @param component to get native tags back
	 * 
	 * @return an array with the native tags in order of appearance
	 */
	private String[] parseTagValues(JComponent component) {
		String value = tags.get(component.hashCode());
		ArrayList<String> tagsList = new ArrayList<>();
	
		//iterate until all tags are place in the list
		while(value.length() > 0) {
			if (value.startsWith("<small>")) {
				tagsList.add("<small>");
				value = value.replaceFirst("<small>", "");
			}
			
			if (value.startsWith("<big>")) {
				tagsList.add("<big>");
				value = value.replaceFirst("<big>", "");
			}
			
		}
		
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
	 * Modifies non-null Insets of JComponent.
	 *  
	 * @param component the JComponent, whose Insets are to be scaled
	 *
	 */
	private void coscaleInsets(JComponent component) {
		Insets old = null;
		
		if (component.getBorder() != null &&
				!component.getBorder().getBorderInsets(component)
					.equals(new Insets(0,0,0,0))) {
			
			old = component.getBorder().getBorderInsets(component);
			
			Insets newi = getModifiedInsets(old);
			Border empty = BorderFactory.createEmptyBorder(
					newi.top - old.top,
					newi.left - old.left,
					newi.bottom - old.bottom,
					newi.right - old.right);
			Border compound = BorderFactory.createCompoundBorder(empty,
					component.getBorder());
				
			component.setBorder(compound);
		} else if (!component.getInsets().equals(new Insets(0,0,0,0))) {
			Insets newi = getModifiedInsets(component.getInsets());
			Border empty = new EmptyBorder(newi);
			//reset border to modify insets
			component.setBorder(empty);
		}
		
		//conditions not met --> exit.
	}

	/**
	 * Modifies the direct icons of the following JComponents:<p>
	 * 
	 * <b>AbstractButton</b>,<br>
	 * <b>JLabel</b>,<br>
	 * <b>JOptionPane</b>,<br>
	 * <b>JTabbedPane</b>.<br>
	 * 
	 * @param component a possible Component containing Icon
	 */
	private void coscaleIcons(JComponent component) {		
		if (component instanceof AbstractButton) {	
			if (component instanceof JMenu) {
				for (Component item: ((JMenu) component).getMenuComponents())
					if (item instanceof AbstractButton)
						modifySetIcon((AbstractButton) item);
			} else
				modifySetIcon((AbstractButton) component);
		} else if (component instanceof JLabel)
			modifySetIcon((JLabel) component);
		else if (component instanceof JOptionPane)
			modifySetIcon((JOptionPane) component);
		else if (component instanceof JTabbedPane)
			modifySetIcon((JTabbedPane) component);
	}
	
	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * AbstractButton.
	 * 
	 * @param button AbstractButton
	 */
	private void modifySetIcon(AbstractButton button) {
		Icon i; //any icon
		Icon disabled = null;
		Icon disabledSelected = null;
		Icon pressed = null;
			
		if ((i = button.getDisabledIcon()) != null && !i.equals(button.getIcon()))
			//save it (setIcon nullifies disabledIcon)
			disabled = i;
			
		if ((i = button.getDisabledSelectedIcon()) != null &&
				!i.equals(button.getIcon()))
			//again save it!
			disabledSelected = i;
			
		if ((i = button.getPressedIcon()) != null && !i.equals(button.getIcon()))
			//again save it!
			pressed = i;
			
		if ((i = button.getIcon()) != null)
			button.setIcon(getModifiedIcon(null, i));
			
		if ((i = button.getRolloverIcon()) != null && !i.equals(button.getIcon()))
			button.setRolloverIcon(getModifiedIcon(null, i));
			
		if ((i = button.getRolloverSelectedIcon()) != null && !i.equals(button.getIcon()))
			button.setRolloverSelectedIcon(getModifiedIcon(null, i));
			
		if ((i = button.getSelectedIcon()) != null && !i.equals(button.getIcon()))
			button.setSelectedIcon(getModifiedIcon(null, i));
			
		if (disabled != null)
			button.setDisabledIcon(getModifiedIcon(null, disabled));
			
		if (disabledSelected != null)
			button.setDisabledSelectedIcon(getModifiedIcon(null, disabledSelected));
			
		if (pressed != null)
			button.setPressedIcon(getModifiedIcon(null, pressed));

		if (button instanceof JMenuItem) {
			//JMenuItem specifics come here
		} else
			//we do not scale menu gaps (some are unset)
			button.setIconTextGap((int) (button.getIconTextGap() * scaleFactor));	
			
		button.validate();
	}
	
	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * JLabel.
	 * 
	 * @param label JLabel
	 */
	private void modifySetIcon(JLabel label) {
		Icon i; //any icon
		Icon disabled = null;
			
		if ((i = label.getDisabledIcon()) != null && !i.equals(label.getIcon()))
			//see above
			disabled = i;
			
		if ((i = label.getIcon()) != null)
			label.setIcon(getModifiedIcon(null, i));
			
		if (disabled != null)
			label.setDisabledIcon(getModifiedIcon(null, disabled));
			
		label.setIconTextGap((int) (label.getIconTextGap() * scaleFactor));	
			
		label.validate();
	}
	
	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * JOptionPane.
	 *
	 * @param pane JOptionPane
	 */
	private void modifySetIcon(JOptionPane pane) {
		Icon i; //any icon
		
		if ((i = pane.getIcon()) != null)
			pane.setIcon(getModifiedIcon(null, i));
			
		pane.validate();
	}

	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * JTabbedPane.
	 *
	 * @param pane JTabbedPane
	 */
	private void modifySetIcon(JTabbedPane pane) {
		Icon i;
		int j = 0;
		
		while(pane.getTabCount() > 0 && (i = pane.getIconAt(j)) != null) {
			pane.setIconAt(j, getModifiedIcon(null, i));
			j++;
		}
	}
	
	@SuppressWarnings("unused")
	/**
	 * Useful for avoiding a doubly modifications of a component.
	 * 
	 * @param component
	 * @return
	 */
	private boolean isModified(JComponent component) {
		if (modified == null) {
			modified = new HashSet<Integer>();
			//Autoboxing comes into play
			modified.add(component.hashCode());
			
			return false;
		} 
		
		if (!modified.contains(component.hashCode())) {
			modified.add(component.hashCode());
			
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	/**
	 * Reset the supporting data structures.
	 */
	private void clear() {
		modified = null;
	}
	
	
	private class TextListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			modifyHTMLFont((JComponent) evt.getSource());
		}
		
	}	
}