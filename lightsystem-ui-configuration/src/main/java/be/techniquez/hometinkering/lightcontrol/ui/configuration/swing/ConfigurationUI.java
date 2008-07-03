package be.techniquez.hometinkering.lightcontrol.ui.configuration.swing;

import java.awt.BorderLayout;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.ws4d.java.communication.DPWSException;

import be.techniquez.hometinkering.lightcontrol.dpws.client.DPWSClient;
import be.techniquez.hometinkering.lightcontrol.dpws.client.DPWSClientListener;
import be.techniquez.hometinkering.lightcontrol.dpws.client.impl.DPWSClientImpl;

/**
 * Configuration UI main class, starts up the application.
 * 
 * @author alex
 */
public final class ConfigurationUI extends JFrame {

	/** Logger instance for this class. */
	private static final Logger logger = Logger.getLogger(ConfigurationUI.class.getName());
	
	/** The tabbed pane. */
	private final JTabbedPane tabbedPane = new JTabbedPane();
	
	/** The DPWS client we use here. */
	private final DPWSClient client;
	
	/** The status label. */
	private final JLabel statusLabel;
	
	/**
	 * Creates and initializes the UI.
	 */
	private ConfigurationUI() {
		this.setLayout(new BorderLayout());
		
		//this.tabbedPane.addTab("Digital boards", this.digitalBoardPanel);
		//this.tabbedPane.addTab("Dimmer boards", this.dimmerBoardPanel);
		
		//this.getContentPane().add(this.tabbedPane, BorderLayout.CENTER);
		
		this.statusLabel = new JLabel("");
		this.getContentPane().add(this.statusLabel, BorderLayout.SOUTH);
		
		this.setTitle("Lightsystem configuration");
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setExtendedState(MAXIMIZED_BOTH);
		
		this.client = new DPWSClientImpl();
		this.client.addListener(new DPWSClientListenerImpl());
		
		this.setStatusMessage("Discovering devices on the network...");
	}
	
	/**
	 * Sets the status message.
	 * 
	 * @param 	message		The message that should be displayed.
	 */
	private final void setStatusMessage(final String message) {
		this.statusLabel.setText(message);
		this.statusLabel.updateUI();
	}
	
	/**
	 * Client listener.
	 * 
	 * @author alex
	 */
	private final class DPWSClientListenerImpl implements DPWSClientListener {

		/**
		 * {@inheritDoc}
		 */
		public final void clientInitialized() {
			setStatusMessage("Devices discovered, ready...");
			
			try {
				client.reload();
			} catch (DPWSException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Starts up the application.
	 * 
	 * @param 	args	Command line arguments that get passed to this class.
	 */
	public static final void main(final String[] args) {
		logger.info("Starting up the configuration UI...");
		
		new ConfigurationUI();
		
		logger.info("Done, started up...");
	}
}
