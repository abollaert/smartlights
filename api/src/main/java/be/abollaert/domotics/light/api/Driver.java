package be.abollaert.domotics.light.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface Driver {

	/**
	 * Returns all the digital modules known on the system.
	 * 
	 * @return	All the digital modules known on the system.
	 */
	Set<DigitalModule> getAllDigitalModules();
	
	/**
	 * Returns all the known dimmer modules on the system.
	 * 	
	 * @return	All the dimmer modules known on the system.
	 */
	Set<DimmerModule> getAllDimmerModules();
	
	/**
	 * Returns the digital module with the given ID.
	 * 
	 * @param 		id		The module ID.
	 * 
	 * @return		The {@link DigitalModule} with the given ID, null if none found.
	 */
	DigitalModule getDigitalModuleWithID(final int id);
	
	/**
	 * Returns the dimmer module with the given ID.
	 * 
	 * @param 		id		The module ID.
	 * 
	 * @return		The {@link DimmerModule} with the given ID, null if none found.
	 */
	DimmerModule getDimmerModuleWithID(final int id);
	
	/**
	 * Probes for devices.
	 * 
	 * @throws IOException
	 */
	void probe() throws IOException;
	
	/**
	 * Unload the driver.
	 * 
	 * @throws IOException
	 */
	void unload() throws IOException;
	
	/**
	 * Returns all the {@link Mood}s registered on the system.
	 * 
	 * @return	The {@link Mood} registered on the system.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	List<Mood> getAllMoods() throws IOException;

	/**
	 * This method returns a new mood that has not been saved yet, with the given name.
	 * 
	 * @param	name		The name of the mood to create.
	 * 
	 * @return	A mood that can be created afterwards.
	 */
	Mood getNewMood(final String name);
	
	/**
	 * Returns the mood with the given ID.
	 * 
	 * @param 		id		The ID of the mood.
	 * 
	 * @return		The given mood.
	 */
	Mood getMoodWithID(final int id);
	
	/**
	 * Removes the mood with the given ID.
	 * 
	 * @param 	id		The ID of the mood to remove.
	 */
	void removeMood(final int id) throws IOException;
	
	/**
	 * Switches all the lights off.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	void allLightsOff() throws IOException;
}
