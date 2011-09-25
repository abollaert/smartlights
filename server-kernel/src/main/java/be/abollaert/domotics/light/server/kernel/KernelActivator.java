package be.abollaert.domotics.light.server.kernel;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;

import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.sensor.OccupancySensor;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;
import be.abollaert.domotics.light.server.kernel.persistence.sqlite.SQLiteStorage;
import be.abollaert.domotics.light.server.kernel.sensormanager.SensorManager;

/**
 * Activator for the serial driver. It provides the modules on the server side.
 * 
 * @author alex
 */
public final class KernelActivator implements BundleActivator {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(KernelActivator.class.getName());
	
	/** The service registration received when registering the service. */
	private ServiceRegistration registration;
	
	/** The serial driver. */
	private SerialDriver driver;
	
	/** The storage. */
	private Storage storage;
	
	/** The bundle context. */
	private BundleContext context;
	
	private final Set<SensorManager> sensorManagers = new HashSet<SensorManager>();
	
	/** The filter selects the http service as well as the driver. */
	private static final String SVC_FILTER = "(objectclass=" + OccupancySensor.class.getName() + ")";
	
	private final ServiceListener listener = new ServiceListener() {
		@Override
		public final void serviceChanged(final ServiceEvent event) {
			if (context != null) {
				if (event.getType() == ServiceEvent.REGISTERED) {
					final OccupancySensor sensor = (OccupancySensor)context.getService(event.getServiceReference());
					
					if (sensor.getName().equals("10-00-00-50-c2-36-63-1b")) {
						sensorManagers.add(new SensorManager(sensor, driver, 4, 40));
					} else if (sensor.getName().equals("10-00-00-50-c2-36-63-3c")) {
						sensorManagers.add(new SensorManager(sensor, driver, 5, 2));
					}
				}
			}
		}
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void start(final BundleContext context) throws Exception {
		if (this.storage == null) {
			this.storage = new SQLiteStorage();
		}
		
		this.context = context;
		this.storage.start();
		
		if (this.driver == null) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Starting serial driver.");
			}
			
			this.driver = new SerialDriver(this.storage);
		}
		
		this.driver.probe();
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Registering serial driver.");
		}
		
		this.registration = context.registerService(Driver.class.getName(), driver, null);
		this.context.addServiceListener(this.listener, SVC_FILTER);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void stop(final BundleContext context) throws Exception {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Unregistering serial driver.");
		}
		
		this.driver.unload();
		this.registration.unregister();
		this.storage.stop();
		this.sensorManagers.clear();
		this.context = null;
	}

}
