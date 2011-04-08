package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.event.ActionEvent;
import java.io.IOException;

import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.tools.serialdebug.action.BaseAction;

abstract class ActivateMoodAction extends BaseAction {
	
	/**
	 * Create a new instance.
	 */
	ActivateMoodAction(final Driver driver) {
		super(driver, "Activate");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void actionPerformed(final ActionEvent event) {
		try {
			this.getMood().activate();
		} catch (IOException e) {
			this.showError("IO error while activating mood.", e);
		}
	}
	
	/**
	 * Returns the ID of the mood to activate.
	 * 
	 * @return	The ID of the mood to activate.
	 */
	abstract Mood getMood();
}
