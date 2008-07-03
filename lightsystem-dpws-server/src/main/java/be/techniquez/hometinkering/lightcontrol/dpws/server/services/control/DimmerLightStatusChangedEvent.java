package be.techniquez.hometinkering.lightcontrol.dpws.server.services.control;

import org.ws4d.java.service.ParameterType;

import be.techniquez.hometinkering.lightcontrol.dpws.server.services.AbstractDPWSEvent;
import be.techniquez.hometinkering.lightcontrol.model.DimmerLight;

/**
 * Event that gets sent when the dimmer light changes status.
 * 
 * @author alex
 *
 */
public final class DimmerLightStatusChangedEvent extends AbstractDPWSEvent {

	/** The name of this event. */
	static final String NAME = "DimmerLightStatusChangedEvent";
	
	/**
	 * Creates a new instance of this event type, using the given service as parent.
	 * 
	 * @param 	service		The parent service for the event.
	 */
	public DimmerLightStatusChangedEvent(final LightControlService service) {
		super(NAME, service);
		
		this.addParameter("LightID", ParameterType.PARAMETER_TYPE_STRING);
		this.addParameter("Status", ParameterType.PARAMETER_TYPE_BOOLEAN);
		this.addParameter("DimPercentage", ParameterType.PARAMETER_TYPE_INTEGER);
		
		service.addAction(this);
	}
	
	/**
	 * Fires the event for the given dimmer light.
	 * 
	 * @param 	dimmerLight		The light that caused the event to be fired.
	 */
	public final void fire(final DimmerLight dimmerLight) {
		this.getOutputParameter("LightID").setValue(dimmerLight.getLightIdentifier());
		this.getOutputParameter("Status").setValue(String.valueOf(dimmerLight.isOn()));
		this.getOutputParameter("DimPercentage").setValue(String.valueOf(dimmerLight.getPercentage()));
		
		this.fire();
	}
}
