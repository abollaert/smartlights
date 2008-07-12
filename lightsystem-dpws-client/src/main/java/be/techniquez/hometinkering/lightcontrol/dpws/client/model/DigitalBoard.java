package be.techniquez.hometinkering.lightcontrol.dpws.client.model;

import be.techniquez.hometinkering.lightcontrol.dpws.client.DPWSClient;

/**
 * Represents a digital board.
 * 
 * @author alex
 */
public final class DigitalBoard extends Board<DigitalChannel> {

	
	/**
	 * Creates a new instance.
	 * 
	 * @param id					The ID of the board.
	 * @param softwareVersion		The version of the firmware running on the board.
	 * @param driverName			The name of the driver loading the board.
	 * @param client				The client used.
	 */
	public DigitalBoard(final int id, final String softwareVersion, final String driverName, final DPWSClient client) {
		super(id, softwareVersion, driverName, client);
	}
}
