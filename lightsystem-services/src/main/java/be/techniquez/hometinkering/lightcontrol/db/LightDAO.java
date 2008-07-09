package be.techniquez.hometinkering.lightcontrol.db;

import be.techniquez.hometinkering.lightcontrol.model.Light;

/**
 * DAO definition for the lights.
 * 
 * @author alex
 */
public interface LightDAO {
	
	/**
	 * Adds the information from the database to the light, if applicable.
	 * 
	 * @param 	light		The light to add information to.
	 * 
	 * @return	The light with added information from the database. The same light if nothing has been recorded in the database.
	 */
	@SuppressWarnings("unchecked")
	Light addDatabaseInformation(final Light light);
	
	/**
	 * Update the given channel with the given light info.
	 * 
	 * @param 	boardId				The ID of the board.
	 * @param 	channelNumber		The channel number.
	 * @param 	lightName			The name of the light.
	 * @param 	lightDescription	The description of the light.
	 */
	@SuppressWarnings("unchecked")
	void updateLight(final Light light);
}
