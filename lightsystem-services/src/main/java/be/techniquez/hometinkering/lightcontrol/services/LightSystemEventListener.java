package be.techniquez.hometinkering.lightcontrol.services;

import be.techniquez.hometinkering.lightcontrol.model.DigitalLight;
import be.techniquez.hometinkering.lightcontrol.model.DimmerLight;

/**
 * Interface that gets implemented by interested parties when they want to know
 * whether some status gets changed in the repository and they want to get notifued
 * of it.
 * 
 * @author alex
 *
 */
public interface LightSystemEventListener {

	/**
	 * Called by the repository when a dimmer light status has changed.
	 * 
	 * @param 	dimmerLight		The dimmer light for which the status has changed.
	 */
	void dimmerLightStatusChanged(final DimmerLight dimmerLight);
	
	/**
	 * Called by the repository when a digital light status has changed.
	 * 
	 * @param 	digitalLight	The digital light whose status has changed.
	 */
	void digitalLightStatusChanged(final DigitalLight digitalLight);
}
