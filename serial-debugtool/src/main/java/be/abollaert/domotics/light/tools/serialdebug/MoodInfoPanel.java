package be.abollaert.domotics.light.tools.serialdebug;

import javax.swing.JLabel;
import javax.swing.JTextField;

import be.abollaert.domotics.light.api.Mood;

/**
 * Panel containing the basic mood information.
 * 
 * @author alex
 */
final class MoodInfoPanel extends PropertiesPanel {

	/** The name text field. */
	private final JTextField txtName = new JTextField(20);
	
	/** The text field containing the ID of the mood. */
	private final JTextField txtId = new JTextField(5);
	
	/** The mood. */
	private Mood mood;
	
	/**
	 * Create a new instance.
	 */
	MoodInfoPanel() {
		super("Mood information");
		
		this.buildUI();
		this.txtId.setEditable(false);
	}
	
	/**
	 * Build the UI.
	 */
	private final void buildUI() {
		this.addRow(new JLabel("ID"), this.txtId);
		this.addRow(new JLabel("Name"), this.txtName);
	}
	
	/**
	 * Set the mood.
	 * 
	 * @param 	mood		The mood to set.
	 */
	final void setMood(final Mood mood) {
		this.mood = mood;
		
		this.txtId.setText(String.valueOf(this.mood.getId()));
		this.txtName.setText(this.mood.getName());
	}
	
	/**
	 * Copy from the UI.
	 */
	final void copyFromUI() {
		this.mood.setName(this.txtName.getText());
	}
}
