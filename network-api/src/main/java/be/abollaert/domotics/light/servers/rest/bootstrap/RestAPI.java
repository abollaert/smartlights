package be.abollaert.domotics.light.servers.rest.bootstrap;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import be.abollaert.domotics.light.servers.rest.resources.StatusResource;

/**
 * Main entry point for the REST API.
 * 
 * @author alex
 */
public final class RestAPI extends Application {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Set<Class<?>> getClasses() {
		final Set<Class<?>> resources = new HashSet<Class<?>>();
		
		resources.add(StatusResource.class);
		
		return resources;
	}
}
