package be.abollaert.domotics.light.api.sensor;

public interface OccupancySensor {

	void addOccupancyListener(final OccupancySensorListener listener);
	
	void removeOccupancyListener(final OccupancySensorListener listener);
	
	boolean getOccupancy();
	
	/**
	 * Returns the name.
	 * 
	 * @return	The name.
	 */
	String getName();
}
