package be.abollaert.domotics.light.server.kernel.persistence;

/**
 * Stored channel configuration. Immutable.
 * 
 * @author alex
 */
public final class StoredChannelConfiguration {
	
	/**
	 * The name of the channel.
	 */
	private final String name;
	
	/** <code>true</code> if logging is enabled, <code>false</code> if it is not. */
	private final boolean loggingEnabled;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	name				The name of the channel.
	 * @param 	enableLogging		<code>true</code> if logging is enabled, <code>false</code> if not.
	 */
	public StoredChannelConfiguration(final String name, final boolean enableLogging) {
		this.name = name;
		this.loggingEnabled = enableLogging;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @return the loggingEnabled
	 */
	public final boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (loggingEnabled ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StoredChannelConfiguration other = (StoredChannelConfiguration) obj;
		if (loggingEnabled != other.loggingEnabled)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
