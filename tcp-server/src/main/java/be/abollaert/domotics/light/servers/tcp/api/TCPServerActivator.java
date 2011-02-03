package be.abollaert.domotics.light.servers.tcp.api;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import be.abollaert.domotics.light.api.Driver;

/**
 * Budnle activator for the TCP server.
 * 
 * @author alex
 *
 */
public final class TCPServerActivator implements BundleActivator {
	
	/** The filter selects the http service as well as the driver. */
	private static final String SVCS_FILTER = "(|(objectclass=" + Driver.class.getName() + ")(objectclass=" + HttpService.class.getName() + "))";
	
	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(TCPServerActivator.class.getName());
	
	private ServiceTracker tracker;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void start(final BundleContext context) throws Exception {
		final Filter filter = context.createFilter(SVCS_FILTER);
		this.tracker = new TCPServerServiceTracker(context, filter);
		this.tracker.open();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void stop(final BundleContext context) throws Exception {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Stopping TCP server.");
		}
		
		this.tracker.close();
		this.tracker = null;
	}
}
