package be.techniquez.hometinkering.lightcontrol.ui.configuration.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.ws4d.java.communication.DPWSException;

import be.techniquez.hometinkering.lightcontrol.dpws.client.DPWSClient;
import be.techniquez.hometinkering.lightcontrol.dpws.client.DPWSClientListener;
import be.techniquez.hometinkering.lightcontrol.dpws.client.impl.DPWSClientImpl;
import be.techniquez.hometinkering.lightcontrol.dpws.client.model.DigitalBoard;
import be.techniquez.hometinkering.lightcontrol.ui.configuration.swing.components.panel.DigitalBoardPanel;
import be.techniquez.hometinkering.lightcontrol.ui.configuration.swing.wizard.configuration.ConfigurationWizardPage1;

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
		
		this.getContentPane().add(this.tabbedPane, BorderLayout.CENTER);
		
		this.statusLabel = new JLabel("");
		this.getContentPane().add(this.statusLabel, BorderLayout.SOUTH);
		
		this.setJMenuBar(this.createMenu());
		
		this.setTitle("Lightsystem configuration");
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
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
			setStatusMessage("Devices discovered, loading configuration...");
			
			reload();
			
			setStatusMessage("Done, you can start configuring...");
		}
	}
	
	/**
	 * Reload the entire UI.
	 */
	private final void reload() {
		try {
			this.client.reload();
			this.tabbedPane.removeAll();
			
			for (final DigitalBoard board : this.client.getDigitalBoards()) {
				this.tabbedPane.addTab("Digital board [" + board.getId() + "]", new DigitalBoardPanel(board));
			}
			
			SwingUtilities.updateComponentTreeUI(this);
		} catch (DPWSException e) {
			e.printStackTrace();
		}
	}
	
	private final JMenuBar createMenu() {
		final JMenuBar menu = new JMenuBar();
		
		final JMenu systemMenu = new JMenu("System");
		final JMenuItem reloadItem = new JMenuItem(new ReloadAction());
		systemMenu.add(reloadItem);
		
		menu.add(systemMenu);
		
		return menu;
	}
	
	/**
	 * Action that reloads the entire configuration.
	 * 
	 * @author alex
	 */
	private final class ReloadAction extends AbstractAction {
		
		private ReloadAction() {
			super();
			
			this.putValue(Action.NAME, "Reload");
			this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
			this.putValue(Action.LONG_DESCRIPTION, "Reloads the entire configuration");
		}

		/**
		 */
		public final void actionPerformed(final ActionEvent e) {
			setStatusMessage("Reloading the configuration...");
			
			reload();
			
			setStatusMessage("Done, configuration reloaded...");
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
