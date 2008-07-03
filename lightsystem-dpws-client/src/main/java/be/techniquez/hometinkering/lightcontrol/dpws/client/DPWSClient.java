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
}
