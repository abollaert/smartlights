package be.techniquez.hometinkering.lightcontrol.dpws.server.services;

import org.ws4d.java.service.EventedAction;
import org.ws4d.java.service.Parameter;
import org.ws4d.java.service.ParameterType;

/**
 * Abstract DPWS event.
 * 
 * @author alex
 *
 */
public abstract class AbstractDPWSEvent extends EventedAction {
	
	/** The namespace. */
	private final String namespace;

	/**
	 * Creates a new DPWS event.
	 * 
	 * @param 	eventName		The name of the event.
	 * @param 	service			The service that hosts the event.
	 * @param 	parameters		The parameters associated to the event.
	 */
	protected AbstractDPWSEvent(final String eventName, final AbstractDPWSService service) {
		super(eventName);
		
		this.namespace = service.getEventsNamespace();
	}
	
	/**
	 * Adds a parameter to the event.
	 * 
	 */
	protected final void addParameter(final String name, final ParameterType type) {
		final Parameter parameter = new Parameter(name, this.namespace, type);
		this.addOutputParameterDefinition(parameter);
	}
}
