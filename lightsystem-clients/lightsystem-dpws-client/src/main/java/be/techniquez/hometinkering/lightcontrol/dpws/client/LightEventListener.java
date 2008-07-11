package be.techniquez.hometinkering.lightcontrol.dpws.client;

import be.techniquez.hometinkering.lightcontrol.dpws.client.model.DigitalChannel;
import be.techniquez.hometinkering.lightcontrol.dpws.client.model.DimmerChannel;

public interface LightEventListener {

	/**
	 * Called when the status of a digital light has changed.
	 * 
	 * @param 	channel		The channel.
	 */
	void DigitalLightStatusChanged(final DigitalChannel channel);
	
	/**
	 * Called when the status of a dimmer channel has changed.
	 * 
	 * @param 	channel		The channel.
	 */
	void DimmerLightStatusChanged(final DimmerChannel channel);
}
