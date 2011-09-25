package be.abollaert.domotics.light.server.kernel.sensormanager;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.sensor.OccupancySensor;
import be.abollaert.domotics.light.api.sensor.OccupancySensorListener;

/**
 * Stub class.
 * 
 * @author alex
 */
public final class SensorManager implements OccupancySensorListener {
	
	private final OccupancySensor sensor;
	
	private final Driver driver;
	
	private final int channelNumber;
	
	private final int moduleId;
	
	public SensorManager(final OccupancySensor sensor, final Driver driver, final int channelNumber, final int moduleId) {
		this.sensor = sensor;
		this.driver = driver;
		this.channelNumber = channelNumber;
		this.moduleId = moduleId;
		
		this.sensor.addOccupancyListener(this);
	}

	@Override
	public final void occupancyChanged(final boolean newState) {
		final DigitalModule module = this.driver.getDigitalModuleWithID(this.moduleId);
		
		if (module != null) {
			try {
				module.switchOutputChannel(this.channelNumber, newState ? ChannelState.ON : ChannelState.OFF);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
