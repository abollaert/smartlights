package be.techniquez.hometinkering.lightcontrol.model;

/**
 * A dimmer light.
 * 
 * @author alex
 */
public final class DimmerLight extends Light<DimmerBoard> {

	/**
	 * Creates a new dimmer light.
	 * 
	 * @param 	board				The board.
	 * @param 	channelNumber		The channel number.
	 */
	DimmerLight(final DimmerBoard board, final int channelNumber) {
		super(board, channelNumber);
	}
	
	/**
	 * Returns true if the light is on, false if not.
	 * 
	 * @return	True if the light is on, false if not.
	 */
	public final boolean isOn() {
		return this.getBoard().getPhysicalBoard().isLightOn(this.getChannelNumber());
	}
	
	/**
	 * Gets the dimmer percentage of this light.
	 * 
	 * @return	The dimmer percentage of this light.
	 */
	public final int getPercentageDimmed() {
		return this.getBoard().getPhysicalBoard().getLightPercentage(this.getChannelNumber());
	}

}
