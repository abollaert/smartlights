package be.abollaert.domotics.light.api.sensor;

public interface OccupancySensorListener {

	/**
	 * Called when the occupancy state of the sensor has changed.
	 * 
	 * @param 	newState		The new state.
	 */
	void occupancyChanged(final boolean newState);
}
