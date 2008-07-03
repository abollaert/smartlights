package be.techniquez.hometinkering.lightcontrol.dpws.server.services;

import org.ws4d.java.service.AbstractAction;
import org.ws4d.java.service.HostedService;

/**
 * Abstract base class for the DPWS services, implements some common functionality
 * to get around the funky API in a more efficient and elegant way.
 * 
 * @author alex
 *
 */
public abstract class AbstractDPWSService extends HostedService {
	
	/** The actions namespace. */
	private final String actionsNamespace;
	
	/** The events namespace. */
	private final String eventsNamespace;
	
	/** The porttype. */
	private final String porttype;

	/**
	 * Creates a new instance using the given namespace and porttype.
	 * 
	 * @param namespace
	 * @param porttype
	 */
	protected AbstractDPWSService(final String namespace, final String porttype) {
		super(porttype, namespace);
		
		this.actionsNamespace = namespace;// + "/actions";
		this.eventsNamespace = namespace;// + "/events";
		this.porttype = porttype;
	}
	
	/**
	 * Returns the events namespace.
	 * 
	 * @return	The events namespace.
	 */
	final String getEventsNamespace() {
		return this.eventsNamespace;
	}
	
	/**
	 * Returns the actions namespace.
	 * 
	 * @return	The actions namespace.
	 */
	final String getActionsNamespace() {
		return this.actionsNamespace;
	}
	
	/**
	 * Returns the action that corresponds to the given action name.
	 * 
	 * @param 	actionName	The name of the action to return.
	 * 
	 * @return	The action corresponding to the given name.
	 */
	protected final AbstractAction getAction(final String actionName) {
		return this.getAction(actionName, this.porttype);
	}
}
