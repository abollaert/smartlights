package be.techniquez.hometinkering.lightcontrol.dpws.server.services;

import org.ws4d.java.service.Action;
import org.ws4d.java.service.Parameter;
import org.ws4d.java.service.ParameterType;

import be.techniquez.hometinkering.lightcontrol.services.RepositoryService;

/**
 * Base class for all actions.
 * 
 * @author alex
 */
public abstract class AbstractDPWSAction extends Action {
	
	/** The namespace in which this action resides. */
	private final String namespace;
	
	/** The repository service. */
	private final RepositoryService repository;
	
	/**
	 * Creates a new type of action.
	 * 
	 * @param 	actionName		The name of the action
	 * @param 	oneway			True if the event is one way, false if it isn't
	 * @param 	service			The service to which this event is tied.
	 */
	protected AbstractDPWSAction(final String actionName, final boolean oneway, final AbstractDPWSService service, final RepositoryService repositoryService) {
		super(actionName, oneway);
		
		this.namespace = service.getActionsNamespace();
		this.repository = repositoryService;
	}
	
	/**
	 * Adds an input parameter definition.
	 * 
	 * @param 	name		The name of the parameter.
	 * @param 	type		The type of the parameter (can be complex)
	 */
	protected final void addInputParameter(final String name, final ParameterType type) {
		final Parameter parameter = new Parameter(name, this.namespace, type);
		this.addInputParameterDefinition(parameter);
	}
	
	/**
	 * Adds an output parameter definition.
	 * 
	 * @param 	name		The name.
	 * @param 	type		The type.
	 */
	protected final void addOutputParameter(final String name, final ParameterType type) {
		final Parameter parameter = new Parameter(name, this.namespace, type);
		this.addOutputParameterDefinition(parameter);
	}
	
	/**
	 * Returns the namespace of this action.
	 * 
	 * @return	The namespace of this action.
	 */
	protected final String getNamespace() {
		return this.namespace;
	}
	
	/**
	 * Returns the repository.
	 * 
	 * @return	The repository.
	 */
	protected final RepositoryService getRepository() {
		return this.repository;
	}
}
