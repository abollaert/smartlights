package be.techniquez.hometinkering.lightcontrol.db;

import java.util.Set;

/**
 * DAO that is responsible for enumerating the installed drivers from the database.
 * 
 * @author alex
 */
public interface DriverDAO {

	/**
	 * Gets the installed drivers that are defined in the database.
	 * 
	 * @return	The installed drivers that are defined in the database.
	 */
	Set<String> getInstalledDrivers();
	
	/**
	 * Adds the given driver class to the installed drivers in the database.
	 * 
	 * @param 	className	The class name of the driver to install.
	 */
	void addInstalledDriver(final String className);

	/**
	 * Removes the installed driver from the database.
	 * 
	 * @param 	className	The classname of the driver to remove.
	 */
	void removeInstalledDriver(final String className);
}
