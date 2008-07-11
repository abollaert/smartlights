package be.techniquez.hometinkering.lightcontrol.dpws.server.services.control;

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
	
	/**
	 * Creates a new light control service. This is done by spring.
	 * 
	 * @param 	namespace		The namespace in which the service resides.
	 * @param 	porttype		The port type of the action..
	 */
	public LightControlService(final String namespace, final String porttype, final RepositoryService repository) {
		super(namespace, porttype);
		
		// We are going to listen for events on the repo...
		repository.addListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void digitalLightStatusChanged(final DigitalLight digitalLight) {
		final DigitalLightStatusChangedEvent event = (DigitalLightStatusChangedEvent)this.getAction(DigitalLightStatusChangedEvent.NAME);
		System.out.println("Fire event...");
		event.fire(digitalLight);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void dimmerLightStatusChanged(final DimmerLight dimmerLight) {
		final DimmerLightStatusChangedEvent event = (DimmerLightStatusChangedEvent)this.getAction(DimmerLightStatusChangedEvent.NAME);
		System.out.println("Fire event...");
		event.fire(dimmerLight);
	}
}
