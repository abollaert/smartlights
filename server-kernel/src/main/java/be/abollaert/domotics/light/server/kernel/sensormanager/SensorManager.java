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
	
	private static final int MODULE_ID = 40;
	
	private static final int CHANNEL_NUMBER = 4;
	
	private final OccupancySensor sensor;
	
	private final Driver driver;
	
	public SensorManager(final OccupancySensor sensor, final Driver driver) {
		this.sensor = sensor;
		this.driver = driver;
		
		this.sensor.addOccupancyListener(this);
	}

	@Override
	public final void occupancyChanged(final boolean newState) {
		final DigitalModule module = this.driver.getDigitalModuleWithID(MODULE_ID);
		
		if (module != null) {
			try {
				module.switchOutputChannel(CHANNEL_NUMBER, newState ? ChannelState.ON : ChannelState.OFF);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
