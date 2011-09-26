package be.abollaert.domotics.light.server.kernel.sensormanager;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
	
	/** The delay before switching the light off. */
	private final int delaySwitchOff;
	
	/** Possible running task to switch off. */
	private Future<Boolean> switchOffTask;
	
	private final ScheduledExecutorService delayedExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public final Thread newThread(final Runnable r) {
			return new Thread(r, "[SensorManager] Sensor [" + sensor.getName() + "]");
		}
	});
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	sensor			The sensor.
	 * @param 	driver			The driver.
	 * @param 	channelNumber	The channel number.
	 * @param 	moduleId		The module ID.
	 */
	public SensorManager(final OccupancySensor sensor, final Driver driver, final int channelNumber, final int moduleId, final int delaySwitchOff) {
		this.sensor = sensor;
		this.driver = driver;
		this.channelNumber = channelNumber;
		this.moduleId = moduleId;
		this.delaySwitchOff = delaySwitchOff;
		
		this.sensor.addOccupancyListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void occupancyChanged(final boolean newState) {
		final ChannelState state = newState? ChannelState.ON : ChannelState.OFF;
		
		if (state == ChannelState.OFF && this.delaySwitchOff > 0 && this.getChannelState() == ChannelState.ON) {
			if (this.switchOffTask == null) {
				this.switchOffTask = this.delayedExecutor.schedule(new Callable<Boolean>() {
					@Override
					public final Boolean call() throws Exception {
						switchOutputChannel(state);
						switchOffTask = null;
						
						return true;
					}
				}, this.delaySwitchOff, TimeUnit.SECONDS);
			}
		} else {
			if (state == ChannelState.ON && this.switchOffTask != null) {
				this.switchOffTask.cancel(false);
				this.switchOffTask = null;
			}
			
			if (!(state == ChannelState.OFF && this.getChannelState() == ChannelState.OFF)) {
				this.switchOutputChannel(state);
			}
		}
	}
	
	private final void switchOutputChannel(final ChannelState state) {
		final DigitalModule module = this.driver.getDigitalModuleWithID(this.moduleId);
		
		if (module != null) {
			try {
				module.switchOutputChannel(this.channelNumber, state);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private final ChannelState getChannelState() {
		final DigitalModule module = this.driver.getDigitalModuleWithID(this.moduleId);
		
		if (module != null) {
			try {
				return module.getOutputChannelState(this.channelNumber);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return ChannelState.OFF;
	}
}
