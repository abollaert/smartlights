package be.techniquez.hometinkering.lightcontrol.controlboard.spi.simulate;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.ControlBoardDriver;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.simulate.digital.SimulatedDigitalControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.simulate.dimmer.SimulatedDimmerBoard;

/**
 * Driver for the simulated boards.
 * 
 * @author alex
 */
public final class SimulatedBoardDriver implements ControlBoardDriver {

	/**
	 * {@inheritDoc}
	 */
	public final DigitalLightControlBoard[] loadConnectedDigitalBoards() {
		final SimulatedDigitalControlBoard board1 = new SimulatedDigitalControlBoard(100);
		final SimulatedDigitalControlBoard board2 = new SimulatedDigitalControlBoard(101);
		final SimulatedDigitalControlBoard board3 = new SimulatedDigitalControlBoard(102);
		
		return new DigitalLightControlBoard[] { board1, board2, board3 };
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getName() {
		return "Simulated";
	}

	public final DimmerLightControlBoard[] loadConnectedDimmerBoards() {
		final SimulatedDimmerBoard board1 = new SimulatedDimmerBoard(200);
		final SimulatedDimmerBoard board2 = new SimulatedDimmerBoard(201);
		
		return new DimmerLightControlBoard[] { board1, board2 };
	}

	public final void reload() {
		// Reload does not do anything in this case as everything is static in nature.
	}

}
