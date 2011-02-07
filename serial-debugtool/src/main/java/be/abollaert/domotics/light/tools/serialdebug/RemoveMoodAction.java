package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;

abstract class RemoveMoodAction extends BaseAction {
	
	/** Logger definition. */
	private static final Logger logger = Logger
			.getLogger(RemoveMoodAction.class.getName());

	/**
	 * Create a new instance.
	 */
	RemoveMoodAction(final Driver driver) {
		super(driver, "Remove");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void actionPerformed(final ActionEvent event) {
		final Mood moodToDelete = this.getMoodToDelete();
		
		if (moodToDelete != null) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Remove mood : [" + moodToDelete + "]");
			}
			
			try {
				this.getDriver().removeMood(moodToDelete.getId());
			
				EventDispatcher.getInstance().dispatchModelEvent(new ModelEvent<Mood>(ModelEvent.Type.REMOVE, ModelEvent.Model.MOOD, moodToDelete));
			} catch (IOException e) {
				this.showError("Error while removing mood.", e);
			}
		} else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Mood to delete is null.");
			}
		}
	}
	
	abstract Mood getMoodToDelete();
}
