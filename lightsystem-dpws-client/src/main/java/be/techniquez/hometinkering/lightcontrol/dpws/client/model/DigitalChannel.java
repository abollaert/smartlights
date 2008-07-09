package be.techniquez.hometinkering.lightcontrol.dpws.client.model;

/**
 * Digital channel, found on a digital board.
 * 
 * @author alex
 *
 */
public final class DigitalChannel extends Channel<DigitalBoard> {

	/**
	 * Creates a new digital channel. 
	 * 
	 * @param 	board			The board.
	 * @param 	channelNumber	The channel number.
	 */
	public DigitalChannel(final DigitalBoard board, final int channelNumber) {
		super(board, channelNumber);
	}
}
