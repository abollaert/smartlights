package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.Mood;

/**
 * Action that is attached to a Save Mood operation.
 * 
 * @author alex
 */
abstract class SaveMoodAction extends BaseAction {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(SaveMoodAction.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new save mood action.
	 */
	SaveMoodAction() {
		super("Save");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void actionPerformed(final ActionEvent event) {
		try {
			this.getMood().save();
			this.afterSuccess();
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "IO error while saving mood : [" + e.getMessage() + "]", e);
			}
			
			this.showError("Error while saving mood", e.getMessage());
		}
	}
	
	/**
	 * Returns the mood to save.
	 * 
	 * @return
	 */
	abstract Mood getMood();
}
