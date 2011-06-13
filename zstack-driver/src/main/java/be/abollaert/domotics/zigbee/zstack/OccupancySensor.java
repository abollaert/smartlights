package be.abollaert.domotics.zigbee.zstack;

import java.util.logging.Level;
import java.util.logging.Logger;

final class OccupancySensor extends ZigbeeNode {
	
	/** Logger definition. */
	private static final Logger logger = Logger.getLogger(OccupancySensor.class
			.getName());
	
	/** Indicates whether the occupation is on. */
	private boolean isOccupied = false;

	OccupancySensor(final int shortAddress, final IEEEAddress ieeeAddress, final int endPoint, final ZStackModuleImpl module) {
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
					
					break;
				}
			}
		}
	}
	
	public final boolean isOccupied() {
		return this.isOccupied;
	}
}
