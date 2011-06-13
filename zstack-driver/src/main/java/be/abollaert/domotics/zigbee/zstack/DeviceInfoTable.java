package be.abollaert.domotics.zigbee.zstack;

import java.util.HashMap;
import java.util.Map;

public final class DeviceInfoTable {

	private final Map<IEEEAddress, SensorType> types = new HashMap<IEEEAddress, SensorType>();
	
	public final void addSensor(final IEEEAddress address, final SensorType type) {
		this.types.put(address, type);
	}
	
	public final SensorType getSensorType(final IEEEAddress address) {
		return this.types.get(address);
	}
}
