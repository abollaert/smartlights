package be.abollaert.domotics.light.tools.serialdebug.action;

import java.awt.event.ActionEvent;
import java.io.IOException;

import be.abollaert.domotics.light.api.Driver;

/**
 * When invoked, switches off all lights.
 * 
 * @author alex
 *
 */
public abstract class AllLightsOffAction extends BaseAction {

	/**
	 * Create a new instance.
	 * 
	 * @param 	driver		The driver.
	 */
	public AllLightsOffAction(final Driver driver) {
		super(driver, "All lights off");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void actionPerformed(final ActionEvent event) {
		try {
			this.getDriver().allLightsOff();
		} catch (IOException e) {
			this.showError("IO error while switching off lights.", e);
		}
	}
}
