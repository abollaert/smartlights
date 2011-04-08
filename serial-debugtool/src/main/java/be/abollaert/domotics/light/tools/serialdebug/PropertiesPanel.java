package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

class PropertiesPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final GridBagConstraints constraints = new GridBagConstraints();
	
	PropertiesPanel(final String title) {
		this.setBorder(BorderFactory.createTitledBorder(title));
		
		this.setLayout(new GridBagLayout());
		this.constraints.gridx = 0;
		this.constraints.gridy = 0;
		this.constraints.insets = new Insets(1, 2, 1, 2);
		
		this.constraints.fill = GridBagConstraints.HORIZONTAL;
	}
	
	final void addRow(final JComponent... components) {
		for (final JComponent component : components) {
			this.add(component, this.constraints);
			this.constraints.gridx++;
		}
		
		this.constraints.gridx = 0;
		this.constraints.gridy++;
	}

}
