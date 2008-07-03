package be.techniquez.hometinkering.lightcontrol.dpws.server.services.control;

import java.util.logging.Logger;

import be.techniquez.hometinkering.lightcontrol.dpws.server.services.AbstractDPWSService;
import be.techniquez.hometinkering.lightcontrol.model.DigitalLight;
import be.techniquez.hometinkering.lightcontrol.model.DimmerLight;
import be.techniquez.hometinkering.lightcontrol.services.LightSystemEventListener;
import be.techniquez.hometinkering.lightcontrol.services.RepositoryService;

/**
 * Defines a light control service.
 * 
 * @author alex
 */
public final class LightControlService extends AbstractDPWSService implements LightSystemEventListener {
	
	/** Logger instance used in this class. */
	private static final Logger logger = Logger.getLogger(LightControlService.class.getName());
	
	/**
	 * Creates a new light control service. This is done by spring.
	 * 
	 * @param 	namespace		The namespace in which the service resides.
	 * @param 	porttype		The port type of the action..
	 */
	public LightControlService(final String namespace, final String porttype, final RepositoryService repository) {
		super(namespace, porttype);
		
		// We are going to listen for events on the repo...
		repository.addLightSystemEventListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void digitalLightStatusChanged(final DigitalLight digitalLight) {
		logger.info("Repository says digital light [" + digitalLight.getLightIdentifier() + "] has changed status, firing event...");
		
		final DigitalLightStatusChangedEvent event = (DigitalLightStatusChangedEvent)this.getAction(DigitalLightStatusChangedEvent.NAME);
		event.fire(digitalLight);
		
		logger.info("Done, event has been fired...");
	}

	/**
	 * {@inheritDoc}
	 */
	public final void dimmerLightStatusChanged(final DimmerLight dimmerLight) {
		logger.info("Repository says dimmer light [" + dimmerLight.getLightIdentifier() + "] has changed status, firing event...");
		
		final DimmerLightStatusChangedEvent event = (DimmerLightStatusChangedEvent)this.getAction(DimmerLightStatusChangedEvent.NAME);
		event.fire(dimmerLight);
		
		logger.info("Done, event has been fired...");
	}
}
