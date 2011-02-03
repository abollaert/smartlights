package be.abollaert.domotics.light.services.upgrade.osgi;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

import be.abollaert.domotics.light.services.upgrade.UpgradeService;
import be.abollaert.domotics.light.services.upgrade.impl.UpgradeServiceAVRDudeImpl;
import be.abollaert.domotics.light.services.upgrade.impl.UpgradeServlet;

/**
 * Bundle activator for the upgrade service. This will provide the upgrade service.
 * 
 * @author alex
 */
public final class UpgradeServiceActivator implements BundleActivator {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(UpgradeServiceActivator.class.getName());
	
	/** The upgrade servlet alias. */
	private static final String UPGRADE_SERVLET_ALIAS = "/upgrade-firmware";
	
	/** Reference to the registered service. */
	private ServiceRegistration registration;
	
	/** The upgrade service. */
	private UpgradeService upgradeService;
	
	/** The upgrade servlet. */
	private UpgradeServlet upgradeServlet;
	
	/** The HTTP service. */
	private HttpService httpService;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void start(final BundleContext context) throws Exception {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Starting Upgrade Service bundle.");
		}
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Registering upgrade service.");
		}
		
		this.upgradeService = new UpgradeServiceAVRDudeImpl();
		this.registration = context.registerService(UpgradeService.class.getName(), this.upgradeService, new Hashtable<String, Object>());
		
		final ServiceReference httpServiceReference = context.getServiceReference(HttpService.class.getName());
		
		if (httpServiceReference != null) {
			this.httpService = (HttpService)context.getService(httpServiceReference);
			
			if (this.httpService != null) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Registering upgrade servlet.");
				}
				
				this.upgradeServlet = new UpgradeServlet(this.upgradeService);
				
				this.httpService.registerServlet(UPGRADE_SERVLET_ALIAS, this.upgradeServlet, null, null);
			} else {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not obtain the http service from the reference.");
				}
			}
		} else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Could not obtain http service reference.");
			}
		}
	
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Upgrade Service bundle started.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void stop(final BundleContext context) throws Exception {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Stopping Upgrade Service bundle.");
		}
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Unregistering the upgrade service.");
		}
		
		this.registration.unregister();
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Unregistering the servlet.");
		}
		
		if (this.httpService != null) {
			this.httpService.unregister(UPGRADE_SERVLET_ALIAS);
		}
		
		this.registration = null;
		this.upgradeService = null;
		this.upgradeServlet = null;
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Upgrade Service bundle stopped.");
		}
	}

}
