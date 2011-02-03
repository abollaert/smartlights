package be.abollaert.domotics.light.server.kernel.persistence;

/**
 * Stored info about a switch element.
 * 
 * @author alex
 *
 */
public final class StoredDimMoodElement {
	
	private final int moodId;
	
	private final int moduleId;
	
	private final int channelNumber;
	
	private final int targetPercentage;

	/**
	 * Create a new instance, immutable class. 
	 * 
	 * @param 	moodId				The ID of the mood.
	 * @param	moduleId			The ID of the module.
	 * @param 	channelNumber		The channel number of the channel on said module.
	 * @param 	targetPercentage	The requested percentage.
	 */
	public StoredDimMoodElement(final int moodId, final int moduleId, final int channelNumber, final int targetPercentage) {
		super();
		this.moodId = moodId;
		this.moduleId = moduleId;
		this.channelNumber = channelNumber;
		this.targetPercentage = targetPercentage;
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
	 * @return the target percentage
	 */
	public final int getTargetPercentage() {
		return targetPercentage;
	}
}
