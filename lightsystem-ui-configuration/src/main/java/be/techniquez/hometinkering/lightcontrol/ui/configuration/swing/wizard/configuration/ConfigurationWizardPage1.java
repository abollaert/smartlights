package be.techniquez.hometinkering.lightcontrol.ui.configuration.swing.wizard.configuration;

import javax.swing.JDialog;

import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;

/**
 * First step of the first use wizard. This page let's you configure the boards using the unconfigured boards.
 * 
 * @author alex
 *
 */
public final class ConfigurationWizardPage1 extends WizardPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of this wizard page.
	 */
	public ConfigurationWizardPage1() {
		super("unconfiguredboards", "Select boards");
		
		this.setLongDescription("These boards are connected to the system but have not yet been configured, please select the ones you want to configure now...");
		this.setForwardNavigationMode(Wizard.MODE_CAN_CONTINUE);
		
		this.setBusy(true);
		
		// Load the boards...
		
		this.setBusy(false);
	}
	
}
