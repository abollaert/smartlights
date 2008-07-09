package be.techniquez.hometinkering.lightcontrol.dpws.client.model;

/**
 * Digital channel, found on a digital board.
 * 
 * @author alex
 *
 */
public final class DigitalChannel extends Channel {

	/**
	 * Creates a new digital channel. 
	 * 
	 * @param 	board			The board.
	 * @param 	channelNumber	The channel number.
	 */
	DigitalChannel(final DigitalBoard board, final int channelNumber) {
		super(board, channelNumber);
		
		this.board = board;
	}

	/** The board on which this channel resides. */
	private final DigitalBoard board;
}
