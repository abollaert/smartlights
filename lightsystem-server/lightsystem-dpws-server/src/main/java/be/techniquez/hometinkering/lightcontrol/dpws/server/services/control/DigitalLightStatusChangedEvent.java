package be.techniquez.hometinkering.lightcontrol.dpws.server.services.control;

import org.ws4d.java.service.ParameterType;

import be.techniquez.hometinkering.lightcontrol.dpws.server.services.AbstractDPWSEvent;
import be.techniquez.hometinkering.lightcontrol.model.DigitalLight;

/**
 * Evented action. This is essentially an event that gets fired when a digital light
 * changes status.
 * 
 * @author alex
 */
public final class DigitalLightStatusChangedEvent extends AbstractDPWSEvent {
	
	/** Name of this action. */
	static final String NAME = "DigitalLightStatusChanged";

	/**
	 * Creates a new event.
	 * 
	 * @param 	service		The service.
	 */
	public DigitalLightStatusChangedEvent(final LightControlService service) {
		super(NAME, service);
		
		this.addParameter("LightID", ParameterType.PARAMETER_TYPE_STRING);
		this.addParameter("Status", ParameterType.PARAMETER_TYPE_BOOLEAN);
		
		service.addAction(this);
	}
	
	/**
	 * Fires for the given 
	 * @param digitalLight
	 */
	public final void fire(final DigitalLight digitalLight) {
		this.getOutputParameter("LightID").setValue(digitalLight.getName());
		this.getOutputParameter("Status").setValue(String.valueOf(digitalLight.isOn()));
		
		this.fire();
	}
}
