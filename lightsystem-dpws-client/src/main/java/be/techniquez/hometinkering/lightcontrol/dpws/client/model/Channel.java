package be.techniquez.hometinkering.lightcontrol.dpws.client.model;

import org.ws4d.java.communication.DPWSException;

/**
 * Light class. Base class for the digital as well as the dimmer lights.
 * 
 * @author alex
 */
public abstract class Channel {

	/** The channel number. */
	private final int number;
	
	/** The name of the channel (ID). */
	private String name;
	
	/** The description of the channel. */
	private String description;
	
	/** The board. We need this to update and stuff. */
	private final Board board;
	
	/**
	 * Creates a new channel for the number.
	 * 
	 * @param	board			The board this channel belongs to.
	 * @param 	channelNumber	The channel number of this channel.
	 */
	protected Channel(final Board board, final int channelNumber) {
		this.number = channelNumber;
		this.board = board;
	}
	
	/**
	 * Updates the metadata of this channel.
	 */
	public final void updateMetadata() {
		try {
			this.board.getDPWSClient().updateLightInformation(this.board.getId(), this.number, this.name, this.description);
		} catch (DPWSException e) {
			e.printStackTrace();
		}
	}
}
