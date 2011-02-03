package be.abollaert.domotics.light.api;

import java.util.Date;

/**
 * A switch log event.
 * 
 * @author alex
 *
 */
public final class SwitchEvent implements Comparable<SwitchEvent> {

	/** The module ID. */
	private final int moduleId;
	
	/** The channel number. */
	private final int channelNumber;
	
	/** The time stamp. */
	private final Date timestamp;
	
	/** Indicates whether the switch was pressed. */
	private final boolean on;
	
	/**
	 * Create an instance.
	 * 
	 * @param 	timestamp		The time stamp.
	 * @param	on
	 */
	public SwitchEvent(final int moduleId, final int channelNumber, final Date timestamp, final boolean on) {
		this.moduleId = moduleId;
		this.channelNumber = channelNumber;
		this.timestamp = timestamp;
		this.on = on;
	}

	/**
	 * @return the timestamp
	 */
	public final Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the pressed
	 */
	public final boolean isOn() {
		return on;
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
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + channelNumber;
		result = prime * result + moduleId;
		result = prime * result + (on ? 1231 : 1237);
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwitchEvent other = (SwitchEvent) obj;
		if (channelNumber != other.channelNumber)
			return false;
		if (moduleId != other.moduleId)
			return false;
		if (on != other.on)
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int compareTo(final SwitchEvent o) {
		if (o != null) {
			return this.getTimestamp().compareTo(o.getTimestamp());
		}
		
		return 1;
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder("Switch event : channel [");
		builder.append(this.channelNumber).append("] on module [").append(this.moduleId).append("], timestamp [");
		builder.append(this.timestamp).append("], resulting state [").append(this.on ? "on" : "off").append("]");
		
		return builder.toString();
	}
}
