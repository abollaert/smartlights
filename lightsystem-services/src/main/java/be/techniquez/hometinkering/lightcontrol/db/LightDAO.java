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
	
	/**
	 * Update the given channel with the given light info.
	 * 
	 * @param 	boardId				The ID of the board.
	 * @param 	channelNumber		The channel number.
	 * @param 	lightName			The name of the light.
	 * @param 	lightDescription	The description of the light.
	 */
	void updateChannel(final int boardId, final int channelNumber, final String lightName, final String lightDescription);
}
