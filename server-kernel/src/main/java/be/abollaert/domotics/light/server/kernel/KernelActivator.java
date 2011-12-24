package be.abollaert.domotics.light.server.kernel;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;

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
						sensorManagers.add(new SensorManager(sensor, driver, 4, 40, 60));
					} else if (sensor.getName().equals("10-00-00-50-c2-36-63-3c")) {
						sensorManagers.add(new SensorManager(sensor, driver, 5, 2, 120));
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
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Registering serial driver.");
		}
		
		this.context = context;
		this.storage.start();
		
		if (this.driver == null) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Starting serial driver.");
			}
			
			this.driver = new SerialDriver(this.storage);
		}
		
		Dictionary<String, Object> properties = null;
		
		final ConfigurationAdmin configurationAdmin = this.getConfigurationAdmin();
		
		if (configurationAdmin != null) {
			final Configuration configuration = configurationAdmin.getConfiguration(SerialDriver.PID);
			
			if (configuration.getProperties() == null) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "No configuration for serial driver, creating new config.");
				}
				
				properties = new Hashtable<String, Object>();
				properties.put(Constants.SERVICE_PID, SerialDriver.PID);
				properties.put(SerialDriver.PROPNAME_DEVICE_PATHS, "/dev/ttyUSBftdi_A6004pOf,/dev/ttyUSBftdi_A6004cJi,/dev/ttyUSBftdi_A60048vp");

				configuration.update(properties);
			} else {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Using existing configuration.");
				}
				
				properties = configuration.getProperties();
			}
		} else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Could not get a hold of the configuration admin service !");
			}
		}
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Registering serial driver.");
		}
		
		this.registration = context.registerService(ManagedService.class.getName(), driver, properties);
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
	
	/**
	 * Gets the configuration admin service.
	 * 
	 * @return	The configuration admin service.
	 */
	private final ConfigurationAdmin getConfigurationAdmin() {
		final ServiceReference reference = this.context.getServiceReference(ConfigurationAdmin.class.getName());
		
		if (reference != null) {
			final ConfigurationAdmin configurationAdmin = (ConfigurationAdmin)this.context.getService(reference);
			
			return configurationAdmin;
		}
		
		return null;
	}

}
