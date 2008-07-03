package be.techniquez.hometinkering.lightcontrol.controlboard.spi;

/**
 * Light control board, implements the common features.
 * 
 * @author alex
 */
public interface LightControlBoard {
	
	/**
	 * Returns the number of channels that are supplied on this digital board.
	 * 
	 * @return	The number of channels currently supplied from this board.
	 */
	int getNumberOfChannels();

	/**
	 * Returns the ID associated to this board.
	 * 
	 * @return	The ID associated to this board.
	 */
	int getBoardID();

	/**
	 * Returns the software version running on this board.
	 * 
	 * @return	The software version running on this board.
	 */
	int getBoardVersion();
	
	/**
	 * Returns the name of the driver that is responsible for this board.
	 * 
	 * @return	The name of the driver that is responsible for this board.
	 */
	String getDriverName();
}
