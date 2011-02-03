package be.abollaert.domotics.light.api;

/**
 * Mood element.
 * 
 * @author alex
 */
abstract class AbstractMoodElement implements MoodElement {
	
	/** The module ID. */
	private final int moduleId;
	
	/** The channel number. */
	private final int channelNumber;
	
	/**
	 * A switch mood element.
	 * 
	 * @param 	moduleId			The ID of the module.
	 * @param 	channelNumber		The channel number.
	 */
	AbstractMoodElement(final int moduleId, final int channelNumber) {
		this.moduleId = moduleId;
		this.channelNumber = channelNumber;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final int getModuleId() {
		return this.moduleId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final int getChannelNumber() {
		return this.channelNumber;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + channelNumber;
		result = prime * result + moduleId;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractMoodElement other = (AbstractMoodElement) obj;
		if (channelNumber != other.channelNumber)
			return false;
		if (moduleId != other.moduleId)
			return false;
		return true;
	}
}
