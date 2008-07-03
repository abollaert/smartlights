package be.techniquez.hometinkering.lightcontrol.controlboard.spi;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;

/**
 * Interface specifying the behaviour of a control board spi driver.
 * Needs to be implemented by drivers that have an implementation of the specification.
 * 
 * @author alex
 */
public interface ControlBoardDriver {

	/**
	 * Loads the connected digital boards. The driver is also responsible for initializing them.
	 * 
	 * @return	The connected digital boards.
	 */
	DigitalLightControlBoard[] loadConnectedDigitalBoards();
	
	/**
	 * Loads the connected dimmer boards. This driver also need to initialize the connected 
	 * dimmer boards.
	 * 
	 * @return	The connected dimmer boards.
	 */
	DimmerLightControlBoard[] loadConnectedDimmerBoards();
	
	/**
	 * Returns the name of the driver.
	 * 
	 * @return	The name of the driver.
	 */
	String getName();
	
	/**
	 * Reloads the driver. This means reloading all the boards that are there.
	 */
	void reload();
}
