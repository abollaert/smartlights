package be.abollaert.domotics.light.tools.serialdebug;

public interface ModelListener {

	/**
	 * Called when a mood is removed.
	 * 
	 * @param 	moodId		The ID of the mood.
	 */
	void modelChanged(final ModelEvent<?> modelEvent);
}
