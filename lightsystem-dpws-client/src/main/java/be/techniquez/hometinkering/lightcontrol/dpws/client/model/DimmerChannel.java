package be.techniquez.hometinkering.lightcontrol.dpws.client.model;

/**
 * Dimmer channel, is found on a dimmer board naturally.
 * 
 * @author alex
 */
public final class DimmerChannel extends Channel {

	/**
	 * Creates a new dimmer channel.
	 * 
	 * @param 	board			The board.
	 * @param 	channelNumber	The channel number.
	 */
	DimmerChannel(final DimmerBoard board, final int channelNumber) {
		super(board, channelNumber);
		// TODO Auto-generated constructor stub
	}

}
