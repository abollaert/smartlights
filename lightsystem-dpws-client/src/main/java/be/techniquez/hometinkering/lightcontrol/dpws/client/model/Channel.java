package be.techniquez.hometinkering.lightcontrol.dpws.client.model;

import org.ws4d.java.communication.DPWSException;

/**
 * Light class. Base class for the digital as well as the dimmer lights.
 * 
 * @author alex
 */
@SuppressWarnings("unchecked")
public abstract class Channel<T extends Board> {

	/** The channel number. */
	private final int number;
	
	/** The name of the channel (ID). */
	private String name;
	
	/** The description of the channel. */
	private String description;
	
	/** The board. We need this to update and stuff. */
	private final T board;
	
	/**
	 * Creates a new channel for the number.
	 * 
	 * @param	board			The board this channel belongs to.
	 * @param 	channelNumber	The channel number of this channel.
	 */
	protected Channel(final T board, final int channelNumber) {
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
	
	/**
	 * Returns the board.
	 * 
	 * @return	The board.
	 */
	protected final T getBoard() {
		return this.board;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public final void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the number
	 */
	public final int getNumber() {
		return number;
	}
}
