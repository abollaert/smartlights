package be.abollaert.domotics.zigbee.zstack;

import be.abollaert.domotics.light.api.sensor.OccupancySensor;

public interface ZigbeeDeviceListener {

	/**
	 * Called when a sensor is added.
	 * 
	 * @param 	sensor		The sensor.
	 */
	void occupancySensorAdded(final OccupancySensor sensor);
}
