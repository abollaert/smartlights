package be.abollaert.domotics.light.server.kernel.persistence;

import java.util.Date;
import java.util.List;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.SwitchEvent;

/**
 * The storage takes care of the persistence.
 * 
 * @author alex
 */
public interface Storage {
	
	/**
	 * Load the configuration for a particular channel.
	 * 
	 * @param 	moduleId				The ID of the module.
	 * @param 	channelNumber			The channel number.
	 * 
	 * @return	The stored configuration, <code>null</code> if there is no stored configuration.
	 * 
	 * @throws 	StorageException		If a storage error occurs.
	 */
	StoredChannelConfiguration loadChannelConfiguration(final int moduleId, final int channelNumber) throws StorageException;
	
	/**
	 * Starts the storage engine.
	 * 
	 * @throws	StorageException		If a storage error occurs.
	 */
	void start() throws StorageException;
	
	/**
	 * Stops the storage engine.
	 * 
	 * @throws 	StorageException		If a storage error occurs.
	 */
	void stop() throws StorageException;

	/**
	 * Save the configuration for a particular channel.
	 * 
	 * @param 	moduleId				The ID of the module.
	 * @param 	channelNumber			The channel number.
	 * @param 	configuration			The new configuration.
	 */
	void saveChannelConfiguration(int moduleId, int channelNumber, StoredChannelConfiguration configuration);
	
	/**
	 * Logs an on/off event.
	 * 
	 * @param 	moduleId			The module ID.
	 * @param 	channelNumber		The channel number.
	 * @param 	on					<code>true</code> if on, <code>false</code> if not.
	 */
	void logOnOffEvent(final int moduleId, final int channelNumber, final boolean on);
	
	/**
	 * Log a dim event.
	 * 
	 * @param 	moduleId			The module ID.
	 * @param 	channelNumber		The channel number.
	 * @param 	percentage			The percentage.
	 */
	void logDimEvent(final int moduleId, final int channelNumber, final int percentage);
	
	/**
	 * Get the switch events for the given period.
	 * 
	 * @param 	moduleId			The ID of the module.
	 * @param 	channelNumber		The channel number.
	 * @param 	startDate			The start date. When null, epoch is assumed.
	 * @param 	endDate				The end date. When null, now is assumed.
	 * 
	 * @return	The {@link List} of switch events which occurred for the given channel during the given period. Ordered ascendingly.
	 */
	List<SwitchEvent> getSwitchEventsForPeriod(final int moduleId, final int channelNumber, final Date startDate, final Date endDate);
	
	/**
	 * Add the given mood to storage.
	 * 
	 * @param 	moodId		The ID of the mood.
	 * @param 	moodName	The name of the mood.
	 */
	StoredMoodInfo saveMoodInformation(final int moodId, final String moodName);
	
	/**
	 * Loads the stored mood info for the given ID.
	 * 
	 * @param 		moodId		The mood ID.
	 * 
	 * @return		The Mood information, null if none.
	 */
	StoredMoodInfo loadMoodInformationFor(final int moodId);
	
	/**
	 * Add a switch element to the given mood.
	 * 
	 * @param 	moodId				The ID of the mood.
	 * @param 	moduleId			The module ID.
	 * @param 	channelNumber		The channel number.
	 * @param 	state				The requested state.
	 */
	void saveMoodSwitchElement(final int moodId, final int moduleId, final int channelNumber, final ChannelState state);
	
	/**
	 * Add a dim element to the given mood.
	 * 
	 * @param 		moodId			The ID of the mood.
	 * @param 		moduleId		The module ID.
	 * @param 		channelNumber	The channel number.
	 * @param 		percentage		The percentage.
	 */
	void saveMoodDimElement(final int moodId, final int moduleId, final int channelNumber, final int percentage);
	
	/**
	 * Remove the given switch element from the given mood.
	 * 	
	 * @param 		moodId			The ID of the mood.
	 * @param 		moduleId		The module ID.
	 * @param 		channelNumber	The channel number.
	 */
	void removeMoodSwitchElement(final int moodId, final int moduleId, final int channelNumber);
	
	/**
	 * Removes the given dim element from the given mood.
	 * 
	 * @param 		moodId			The ID of the mood.
	 * @param 		moduleId		The module ID.
	 * @param 		channelNumber	The channel number.
	 */
	void removeMoodDimElement(final int moodId, final int moduleId, final int channelNumber);
	
	/**
	 * Removes the given mood.
	 * 
	 * @param	 moodId		The ID of the mood to remove.
	 */
	void removeMood(final int moodId);
	
	/**
	 * Returns basic information about the stored moods.
	 * 
	 * @return	Basic information about the stored moods.
	 */
	List<StoredMoodInfo> getStoredMoods();
	
	/**
	 * Returns the switch elements that are attached to the given mood.
	 * 
	 * @param 		moodId		The ID of the mood.
	 * 
	 * @return		The switch elements attached to it.
	 */
	List<StoredSwitchMoodElement> getSwitchElementsForMood(final int moodId);
	
	/**
	 * Returns the dim elements that are attached to the given mood.
	 * 		
	 * @param 		moodId		The ID of the mood.
	 * 
	 * @return		The dim elements attached to it.
	 */
	List<StoredDimMoodElement> getDimElementsForMood(final int moodId);
}
