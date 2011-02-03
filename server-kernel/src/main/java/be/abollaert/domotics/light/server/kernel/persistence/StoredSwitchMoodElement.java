package be.abollaert.domotics.light.server.kernel.persistence;

import be.abollaert.domotics.light.api.ChannelState;

/**
 * Stored info about a switch element.
 * 
 * @author alex
 *
 */
public final class StoredSwitchMoodElement {
	
	private final int moodId;
	
	private final int moduleId;
	
	private final int channelNumber;
	
	private final ChannelState requestedState;

	/**
	 * Create a new instance, immutable class. 
	 * 
	 * @param 	moodId				The ID of the mood.
	 * @param	moduleId			The ID of the module.
	 * @param 	channelNumber		The channel number of the channel on said module.
	 * @param 	requestedState		The requested state.
	 */
	public StoredSwitchMoodElement(final int moodId, final int moduleId, final int channelNumber, final ChannelState requestedState) {
		super();
		this.moodId = moodId;
		this.moduleId = moduleId;
		this.channelNumber = channelNumber;
		this.requestedState = requestedState;
	}

	/**
	 * @return the moodId
	 */
	public final int getMoodId() {
		return moodId;
	}

	/**
	 * @return the moduleId
	 */
	public final int getModuleId() {
		return moduleId;
	}

	/**
	 * @return the channelNumber
	 */
	public final int getChannelNumber() {
		return channelNumber;
	}

	/**
	 * @return the requestedState
	 */
	public final ChannelState getRequestedState() {
		return requestedState;
	}
}
