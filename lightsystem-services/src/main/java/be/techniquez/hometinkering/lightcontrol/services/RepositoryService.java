package be.techniquez.hometinkering.lightcontrol.services;

import java.util.List;
import java.util.Map;

import be.techniquez.hometinkering.lightcontrol.model.DigitalLight;
import be.techniquez.hometinkering.lightcontrol.model.DimmerLight;

public interface RepositoryService {

	/**
	 * Gets the connected digital lights that are configured in the database and connected
	 * to the system.
	 * 
	 * @return	The connected and configured digital lights.
	 */
	Map<String, DigitalLight> getConnectedDigitalLights();

	/**
	 * Gets the digital lights that are configured in the database but not connected
	 * to the system.
	 * 
	 * @return	The digital lights that are configured in the database but not connected.
	 */
	Map<String, DigitalLight> getDisconnectedDigitalLights();

	/**
	 * Gets the dimmer lights that are configured and connected.
	 * 
	 * @return	The dimmer lights that are configured and connected.
	 */
	Map<String, DimmerLight> getConnectedDimmerLights();

	/**
	 * Gets the dimmer lights that are configured in the database but connected
	 * to the system.
	 * 
	 * @return	The dimmer lights that are configured in the database but not connected
	 * 			to the system.
	 */
	Map<String, DimmerLight> getDisconnectedDimmerLights();

	/**
	 * Returns the digital light with the given ID.
	 * 
	 * @param 	id	The ID of the light.
	 * 
	 * @return	The digital light with the given ID, or null if none found.
	 */
	DigitalLight getDigitalLight(final String id);

	/**
	 * Returns the dimmer light with the given ID.
	 * 
	 * @param 	id	The ID of the light to return.
	 * 
	 * @return	The light, or null if none found.
	 */
	DimmerLight getDimmerLight(final String id);
	
	/**
	 * Adds the given listener to the eventlisteners.
	 * 
	 * @param 	listener	The listener to add.
	 */
	void addLightSystemEventListener(final LightSystemEventListener listener);
	
	/**
	 * Removes the given listener from the eventlisteners.
	 * 
	 * @param	listener	The listener to remove.
	 */
	void removeLightSystemEventListener(final LightSystemEventListener listener);
	
	/**
	 * Returns all the free digital channels that are currently connected to the system.
	 * 
	 * @return	The free digital channels that are currently connected to the system.
	 */
	Map<Integer, List<Integer>> getAllFreeDigitalChannels();
	
	/**
	 * Returns all the free dimmer channels that are currently connected to the system.
	 * 
	 * @return	The free dimmer channels that are currently connected to the system.
	 */
	Map<Integer, List<Integer>> getAllFreeDimmerChannels();
	
	/**
	 * Gets the configuration of the digital boards that are known to the system.
	 * 
	 * @return	The configuration of the digital boards that are known to the system. Values
	 * 			are the board ID mapped to a map of the channels and their lights or null if 
	 * 			the channel is not taken.
	 */
	Map<Integer, Map<Integer, DigitalLight>> getDigitalBoardsConfiguration();
	
	/**
	 * Gets the configuration of the dimmer boards that are known to the system.
	 * 
	 * @return	The dimmer configuration.
	 */	
	Map<Integer, Map<Integer, DimmerLight>> getDimmerBoardsConfiguration();
	
	/**
	 * Gets the name of the driver for the board with given ID.
	 * 
	 * @param 	boardId		The ID of the board.
	 * 
	 * @return	The name of the driver.
	 */
	String getDriverNameForBoard(final int boardId);
	
	/**
	 * Returns the number of channels the board with given ID holds.
	 * 
	 * @param 	boardId		The ID of the board for which we want to know
	 * 						the number of channels.
	 * 
	 * @return	The number of channels held on the board, -1 if not found.
	 */
	int getNumberOfChannelsOnBoard(final int boardId);
	
	/**
	 * Updates the channel with given number on the given board with the given information.
	 * 
	 * @param 	boardId				The ID of the board.
	 * @param 	channelNumber		The channel number.
	 * @param 	lightName			The name of the light.
	 * @param 	lightDescription	The description of the light.
	 */
	void updateChannel(final int boardId, final int channelNumber, final String lightName, final String lightDescription);
}