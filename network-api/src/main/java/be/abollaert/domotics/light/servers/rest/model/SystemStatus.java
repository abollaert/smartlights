package be.abollaert.domotics.light.servers.rest.model;

import be.abollaert.domotics.light.api.Driver;

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
	public SystemStatus(final Driver driver) {
		this.softwareVersion = driver.getVersion();
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
