package be.abollaert.domotics.light.server.kernel;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;
import be.abollaert.domotics.light.server.kernel.persistence.sqlite.SQLiteStorage;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void start(final BundleContext context) throws Exception {
		if (this.storage == null) {
			this.storage = new SQLiteStorage();
		}
		
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
	}

}
