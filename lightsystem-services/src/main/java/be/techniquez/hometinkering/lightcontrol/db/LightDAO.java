package be.techniquez.hometinkering.lightcontrol.db;

import java.util.Map;

import be.techniquez.hometinkering.lightcontrol.model.DigitalLight;
import be.techniquez.hometinkering.lightcontrol.model.DimmerLight;

/**
 * DAO definition for the lights.
 * 
 * @author alex
 */
public interface LightDAO {
	
	/**
	 * Gets the configured digital lights.
	 * 
	 * @return	The configured digital lights.
	 */
	Map<String, DigitalLight> getConfiguredDigitalLights();
	
	/**
	 * Gets the configured dimmer lights.
	 * 
	 * @return	The configured dimmer lights.
	 */
	Map<String, DimmerLight> getConfiguredDimmerLights();
}
