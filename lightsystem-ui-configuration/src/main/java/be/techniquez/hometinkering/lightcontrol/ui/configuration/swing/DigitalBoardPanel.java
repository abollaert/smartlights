package be.techniquez.hometinkering.lightcontrol.ui.configuration.swing;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Digital board panel. This one holds one digital board.
 * 
 * @author alex
 */
final class DigitalBoardPanel extends JPanel {
	
	/** The panel that contains the channels. */
	private final JPanel channelsPanel = new JPanel();
	
	/**
	 * Creates a new instance.
	 */
	DigitalBoardPanel() {
		this.setLayout(new BorderLayout());
		
		final JPanel boardInformationPanel = new JPanel();
		boardInformationPanel.add(new JLabel("Board ID [1] (driver [Arduino] version [1.0])"));
		this.add(boardInformationPanel, BorderLayout.WEST);
		
		this.channelsPanel.setLayout(new GridLayout(5, 3));
		
		for (int i = 0; i < 5; i++) {
			this.channelsPanel.add(new JLabel("Channel " + i));
			this.channelsPanel.add(new JComboBox(new Object[] { "KEUKEN", "LIVING", "GARAGE" }));
			this.channelsPanel.add(new JButton("Test"));
		}
		
		this.add(this.channelsPanel, BorderLayout.CENTER);
	}
}