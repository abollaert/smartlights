package be.techniquez.hometinkering.lightcontrol.db;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.model.DigitalBoard;
import be.techniquez.hometinkering.lightcontrol.model.DimmerBoard;

/**
 * DAO for the boards.
 * 
 * @author alex
 */
public interface BoardDAO {
	
	/**
	 * Fetches the digital board with the given ID from the database.
	 * 
	 * @param 	boardId		The ID of the board to fetch.
	 * 
	 * @return	The board, null if not found.
	 */
	DigitalBoard getDigitalBoard(final int boardId);
	
	/**
	 * Adds the digital board to the database.
	 * 
	 * @param 	digitalBoard	The board to add.
	 */
	void addDigitalBoard(final DigitalLightControlBoard digitalBoard);
	
	/**
	 * Removes the board with the given ID from the database.
	 * 
	 * @param 	boardId		The board ID.
	 */
	void removeDigitalBoard(final int boardId);
	
	/**
	 * Adds the given dimmer board to the database.
	 * 
	 * @param 	dimmerBoard		The dimmer board to add.
	 */
	void addDimmerBoard(final DimmerLightControlBoard dimmerBoard);
	
	/**
	 * Removes the dimmer board with the given ID from the database.
	 * 
	 * @param 	boardId		The ID of the board to remove.
	 */
	void removeDimmerBoard(final int boardId);
	
	/**
	 * Gets the given dimmer board from the database. Returns the board if found,
	 * null if not found.
	 * 
	 * @param 	boardId		The ID of the board we are looking for.
	 * 
	 * @return	The dimmer board with the given ID, null if not found.
	 */
	DimmerBoard getDimmerBoard(final int boardId);
}
