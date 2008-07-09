package be.techniquez.hometinkering.lightcontrol.model;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;

/**
 * Digital board class, adds more metadata to the board information.
 * 
 * @author alex
 */
public final class DigitalBoard extends Board<DigitalLight, DigitalLightControlBoard> {

	/**
	 * Creates a new instance with the given physical board backing it.
	 * 
	 * @param 	physicalBoard		The physical board.
	 */
	public DigitalBoard(final DigitalLightControlBoard physicalBoard) {
		super(physicalBoard);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final DigitalLight createLight(final int channelNumber) {
		final DigitalLight light = new DigitalLight(this, channelNumber);
		
		return light;
	}
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public final BoardType getType() {
		// TODO Auto-generated method stub
		return BoardType.DIGITAL;
	}
}
