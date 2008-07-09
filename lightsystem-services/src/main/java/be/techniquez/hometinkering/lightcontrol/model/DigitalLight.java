package be.techniquez.hometinkering.lightcontrol.model;


/**
 * A digital light.
 * 
 * @author alex
 */
public final class DigitalLight extends Light<DigitalBoard> {

	/**
	 * Creates a new instance. 
	 * 
	 * @param 	board			The board.
	 * @param 	channelNumber	The channel number.
	 */
	DigitalLight(final DigitalBoard board, final int channelNumber) {
		super(board, channelNumber);
	}
	
	/**
	 * Returns true if this light is on, false if not.
	 * 
	 * @return	True if this light is on, false if not.
	 */
	public final boolean isOn() {
		return this.getBoard().getPhysicalBoard().isLightOn(this.getChannelNumber());
	}
}
