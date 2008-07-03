package be.techniquez.hometinkering.lightcontrol.dpws.client.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for the boards.
 * 
 * @author
 */
public abstract class Board {

	/** The board ID. */
	private final String id;
	
	/** The board software version. */
	private final String softwareVersion;
	
	/** The board driver name. */
	private final String driverName;
	
	/** The channels. */
	private final Map<Integer, String> channels = new HashMap<Integer, String>();
	
	/**
	 * Creates a new instance of the board.
	 * 
	 * @param 	id					The ID.
	 * @param 	softwareVersion		The software version.
	 * @param 	driverName			The driver name.
	 */
	public Board(final String id, final String softwareVersion, final String driverName) {
		this.id = id;
		this.softwareVersion = softwareVersion;
		this.driverName = driverName;
	}
	
	/**
	 * Sets the channel occupation for the given channel number with the given
	 * light identifier.
	 * 
	 * @param 	channelNumber		The channel number of the channel.
	 * @param 	light				The name of the light that occupies the channel.
	 */
	public final void setChannelOccupation(final int channelNumber, final String light) {
		this.channels.put(channelNumber, light);
	}
	
	/**
	 * Sets the channel with the given channel number as being unoccupied.
	 * 
	 * @param 	channelNumber	The channel number.
	 */
	public final void setChannelUnoccupied(final int channelNumber) {
		this.setChannelOccupation(channelNumber, null);
	}
	
	/**
	 * Returns the channels.
	 * 
	 * @return	The channels.
	 */
	public final Map<Integer, String> getChannels() {
		return this.channels;
	}

	/**
	 * Returns the ID of the board.
	 * 
	 * @return	The ID of the board.
	 */
	public final String getId() {
		return id;
	}

	/**
	 * Returns the software version of the software running on the board itself.
	 * 
	 * @return	The version of the software running on the board itself.
	 */
	public final String getSoftwareVersion() {
		return softwareVersion;
	}

	/**
	 * Returns the driver name.
	 * 
	 * @return	The driver name that loaded the board.
	 */
	public String getDriverName() {
		return driverName;
	}
} 
