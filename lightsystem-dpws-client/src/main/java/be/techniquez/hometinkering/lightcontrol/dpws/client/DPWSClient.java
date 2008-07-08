package be.techniquez.hometinkering.lightcontrol.dpws.client;

import java.util.List;

import org.ws4d.java.communication.DPWSException;

import be.techniquez.hometinkering.lightcontrol.dpws.client.model.DigitalBoard;

/**
 * DPWS client interface specification.
 * 
 * @author alex
 *
 */
public interface DPWSClient {

	/**
	 * Adds a listener to the set of listeners.
	 * 
	 * @param 	listener	The listener to add.
	 */
	void addListener(final DPWSClientListener listener);
	
	/**
	 * Removes a listener to the set of listeners.
	 * 
	 * @param 	listener	The listener to remove.
	 */
	void removeListener(final DPWSClientListener listener);
	
	/**
	 * Returns the list of digital boards known on the system.
	 * 
	 * @return	The list of digital boards known on the system.
	 */
	List<DigitalBoard> getDigitalBoards();
	
	/**
	 * Reloads the DPWS client. This syncs it in effect with the remote
	 * system.
	 */
	void reload() throws DPWSException;
	
	/**
	 * Updates the light information for the given channel on the given board.
	 * 
	 * @param 	boardId				The ID of the board for which to update the information.
	 * @param 	channelNumber		The channel number on the board.
	 * @param 	lightName			The name of the light. 
	 * @param 	lightDescription	The description of the light.
	 * 
	 * @throws 	DPWSException		If a DPWS error occurs during the update.
	 */
	void updateLightInformation(final int boardId, final int channelNumber, final String lightName, final String lightDescription) throws DPWSException;
}
