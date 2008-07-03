package be.techniquez.hometinkering.lightcontrol.dpws.server.services.configuration;

import java.util.logging.Logger;

import be.techniquez.hometinkering.lightcontrol.dpws.server.services.AbstractDPWSService;
import be.techniquez.hometinkering.lightcontrol.services.RepositoryService;

/**
 * The configuration service that gets exposed to clients on the network. It is used
 * to be able to configure the system from anywhere without needing to connect to the
 * system physically.
 * 
 * @author alex
 */
public final class LightSystemConfigurationService extends AbstractDPWSService {
	
	/** Logger instance that gets used in this class. */
	private static final Logger logger = Logger.getLogger(LightSystemConfigurationService.class.getName());

	/** The repository service. */
	private final RepositoryService repository;
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param 	namespace		The namespace in which this service resides
	 * @param 	porttype		The porttype of the service		
	 * @param 	repo			The repository that contains information about the
	 * 							backing system.
	 */
	public LightSystemConfigurationService(final String namespace, final String porttype, final RepositoryService repo) {
		super(namespace, porttype);
		
		this.repository = repo;
	}
}
