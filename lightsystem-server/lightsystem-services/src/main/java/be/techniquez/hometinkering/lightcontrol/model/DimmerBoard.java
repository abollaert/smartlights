package be.techniquez.hometinkering.lightcontrol.model;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;

/**
 * Represents a dimmer board.
 * 
 * @author alex
 *
 */
public final class DimmerBoard extends Board<DimmerLight, DimmerLightControlBoard> {

	/**
	 * Creates a new instance.
	 * 
	 * @param 	physicalBoard		The physical board.
	 */
	public DimmerBoard(final DimmerLightControlBoard physicalBoard) {
		super(physicalBoard);
		// TODO Auto-generated constructor stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final DimmerLight createLight(final int channelNumber) {
		return new DimmerLight(this, channelNumber);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoardType getType() {
		return BoardType.DIMMER;
	}
}
