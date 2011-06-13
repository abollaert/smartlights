package be.abollaert.domotics.zigbee.zstack;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.sensor.OccupancySensor;
import be.abollaert.domotics.light.api.sensor.OccupancySensorListener;

final class ZigbeeOccupancySensor extends ZigbeeNode implements OccupancySensor {
	
	/** Logger definition. */
	private static final Logger logger = Logger.getLogger(ZigbeeOccupancySensor.class
			.getName());
	
	/** Indicates whether the occupation is on. */
	private boolean isOccupied = false;
	
	private final Set<OccupancySensorListener> listeners = Collections.newSetFromMap(new WeakHashMap<OccupancySensorListener, Boolean>());

	ZigbeeOccupancySensor(final int shortAddress, final IEEEAddress ieeeAddress, final int endPoint, final ZStackModuleImpl module) {
		super(shortAddress, ieeeAddress, endPoint, module);
	}

	@Override
	final void reportAttributes(final int[] report) {
		int i = 0;
		
		while (i < report.length) {
			final int attributeId = (report[i++] << 8) + (report[i++] & 0xFF);
			
			// Already know the type of data we expect.
			i++;
			
			switch (attributeId) {
				case 0x00: {
					this.isOccupied = (report[i++]) != 0;
					
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Occupancy report : [" + this.isOccupied + "]");
					}
					
					for (final OccupancySensorListener listener : this.listeners) {
						listener.occupancyChanged(this.isOccupied);
					}
					
					break;
				}
			}
		}
	}
	
	public final boolean isOccupied() {
		return this.isOccupied;
	}

	@Override
	public final void addOccupancyListener(OccupancySensorListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public final void removeOccupancyListener(OccupancySensorListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public final boolean getOccupancy() {
		return this.isOccupied;
	}
	
	public final String getName() {
		return this.getIeeeAddress().toString();
	}
}
