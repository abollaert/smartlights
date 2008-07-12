package be.techniquez.hometinkering.lightcontrol.dpws.client.model;

/**
 * Dimmer channel, is found on a dimmer board naturally.
 * 
 * @author alex
 */
public final class DimmerChannel extends Channel<DimmerBoard> {

	/**
	 * Creates a new dimmer channel.
	 * 
	 * @param 	board			The board.
	 * @param 	channelNumber	The channel number.
	 */
	public DimmerChannel(final DimmerBoard board, final int channelNumber) {
		super(board, channelNumber);
	}

	/**
	 * @return the dimPercentage
	 */
	public final int getDimPercentage() {
		return this.getBoard().getDPWSClient().getDimmerPercentage(this);
	}

	/**
	 * @param dimPercentage the dimPercentage to set
	 */
	public final void setDimPercentage(int dimPercentage) {
		this.getBoard().getDPWSClient().changeDimmerPercentage(this);
	}

	/**
	 * @return the on
	 */
	public final boolean isOn() {
		return this.getBoard().getDPWSClient().isOn(this);
	}

	/**
	 * @param on the on to set
	 */
	public final void switchLight(boolean on) {
		this.getBoard().getDPWSClient().switchLight(this, on);
	}
}
