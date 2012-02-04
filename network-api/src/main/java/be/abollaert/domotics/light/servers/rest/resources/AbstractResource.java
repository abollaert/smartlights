package be.abollaert.domotics.light.servers.rest.resources;

import be.abollaert.domotics.light.api.Driver;

class AbstractResource {

	private final Driver driver;
	
	AbstractResource(final Driver driver) {
		this.driver = driver;
	}
	
	final Driver getDriver() {
		return this.driver;
	}
}
