package be.abollaert.domotics.light.servers.rest.bootstrap;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.servers.rest.resources.StatusResource;

/**
 * Main entry point for the REST API.
 * 
 * @author alex
 */
public final class RestAPI extends Application {
	
	/** The driver instance. */
	private final Driver driver;

	public RestAPI(final Driver driver) {
		this.driver = driver;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Set<Object> getSingletons() {
		final Set<Object> resources = new HashSet<Object>();
		
		resources.add(new StatusResource(this.driver));
		
		return resources;
	}
}
