package be.abollaert.domotics.light.servers.rest.model;

/**
 * System status class.
 * 
 * @author alex
 */
public final class SystemStatus {

	/** The software version. */
	private final String softwareVersion;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	softwareVersion		The sofware version.
	 */
	public SystemStatus(final String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	/**
	 * Returns the software version.
	 * 
	 * @return	The software version.
	 */
	public final String getSoftwareVersion() {
		return softwareVersion;
	}
}
