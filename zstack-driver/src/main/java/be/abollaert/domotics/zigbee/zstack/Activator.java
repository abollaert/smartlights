package be.abollaert.domotics.zigbee.zstack;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import be.abollaert.domotics.light.api.sensor.OccupancySensor;

public final class Activator implements BundleActivator, ZigbeeDeviceListener {

	/** Logger definition. */
	private static final Logger logger = Logger.getLogger(Activator.class
			.getName());
	
	private static final DeviceInfoTable TABLE = new DeviceInfoTable();
	
	static {
		TABLE.addSensor(new IEEEAddress(new int[] {
				0x10,
				0x00,
				0x00,
				0x50,
				0xc2,
				0x36,
				0x63,
				0x1b
			}), SensorType.OCCUPANCY);
		
		TABLE.addSensor(new IEEEAddress(new int[] {
				0x10,
				0x00,
				0x00,
				0x50,
				0xC2,
				0x36,
				0x63,
				0x3C
		}), SensorType.OCCUPANCY);
	}
	
	private ZStackModule module;
	
	private BundleContext context;
	
	private final Set<ServiceRegistration> registrations = new HashSet<ServiceRegistration>();
	
	public Activator() {
		try {
			this.module = new ZStackModuleImpl("/dev/ttyUSBftdi_A6003Cud", 57600, TABLE);
		} catch (IOException e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, "IO error while connecting to the ZStack driver : [" + e.getMessage() + "]", e);
			}
			
			this.module = null;
		}
	} 
	
	@Override
	public final void start(final BundleContext context) throws Exception {
		this.context = context;
		this.module.connect();
		this.module.addZigbeeDeviceListener(this);
	}

	@Override
	public final void stop(final BundleContext context) throws Exception {
		for (final ServiceRegistration registration : this.registrations) {
			registration.unregister();
		}
		
		this.module.removeZigbeeDeviceListener(this);
		this.module.disconnect();
		this.context = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void occupancySensorAdded(final OccupancySensor sensor) {
		if (this.context != null) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Registering occupancy sensor with the system.");
			}
			
			final Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("name", sensor.getName());
			
			this.registrations.add(this.context.registerService(OccupancySensor.class.getName(), sensor, properties));
		}
	}

}
