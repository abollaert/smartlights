/**
 * 
 */
package be.techniquez.hometinkering.lightcontrol.dpws.client.model;

import be.techniquez.hometinkering.lightcontrol.dpws.client.DPWSClient;

/**
 * Dimmer board.
 * 
 * @author alex
 */
public final class DimmerBoard extends Board {

	/**
	 * Creates a new dimmer board instance.
	 * 
	 * @param 	id					The ID of the board.
	 * @param 	softwareVersion		The version of the firmware running on the board.
	 * @param 	driverName			The name of the driver loading the board.
	 * @param 	client				The client loading the board.
	 */
	public DimmerBoard(final int id, final String softwareVersion, final String driverName, final DPWSClient client) {
		super(id, softwareVersion, driverName, client);
	}

}
