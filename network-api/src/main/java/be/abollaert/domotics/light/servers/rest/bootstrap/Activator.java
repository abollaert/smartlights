package be.abollaert.domotics.light.servers.rest.bootstrap;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import be.abollaert.domotics.light.api.Driver;

import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * Activator for the REST API.
 * 
 * @author alex
 */
public final class Activator implements BundleActivator {
	
	/** Logger definition. */
	private static final Logger logger = Logger.getLogger(Activator.class.getName());
	
	/** The bundle context. */
	private BundleContext context;
	
	/** The service tracker. */
	private ServiceTracker httpServiceTracker;
	
	/** The kernel tracker. */
	private ServiceTracker kernelServiceTracker;
	
	/** Indicates whether the http service is available. */
	private HttpService httpService;
	
	/** Indicates whether the kernel is available. */
	private Driver driver;
	
	/** Indicates whether we are registered. */
	private volatile boolean registered;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void start(final BundleContext context) throws Exception {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Starting REST API bundle.");
		}
		
		this.context = context;
		
		this.httpServiceTracker = new ServiceTracker(this.context, HttpService.class.getName(), null) {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public final Object addingService(final ServiceReference reference) {
				httpService = (HttpService)context.getService(reference);
				environmentUpdated();
				
				return super.addingService(reference);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public final void removedService(final ServiceReference reference, final Object service) {
				environmentUpdated();
				httpService = null;
				
				super.removedService(reference, service);
			}
		};
		
		this.httpServiceTracker.open();
		
		this.kernelServiceTracker = new ServiceTracker(this.context, Driver.class.getName(), null) {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public final Object addingService(final ServiceReference reference) {
				driver = (Driver)context.getService(reference);
				environmentUpdated();
				
				return super.addingService(reference);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public final void removedService(final ServiceReference reference, final Object service) {
				environmentUpdated();
				driver = null;
				
				super.removedService(reference, service);
			}
		};
		
		this.kernelServiceTracker.open();
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "REST API bundle started.");
		}
	}
	
	private final void environmentUpdated() {
		if (this.httpService != null && this.driver != null) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Starting REST API.");
			}

	        try {
	        	final RestAPI restApplication = new RestAPI(this.driver);
	        	httpService.registerServlet("/smartlights-api", new ServletContainer(restApplication), null, null);
	        	
	        	if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "REST API started.");
				}
	        	
	        	registered = true;
	        } catch (NamespaceException e) {
	        	if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not start REST API due to a namespace error : [" + e.getMessage() + "]", e);
				}
	        } catch (ServletException e) {
	        	if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not start REST API due to a servlet error : [" + e.getMessage() + "]", e);
				}
	        }
		} else {
			if (registered) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Stopping REST API.");
				}
				
				httpService.unregister("/smartlights-api");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void stop(final BundleContext context) throws Exception {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Stopping REST API bundle.");
		}
		
		this.httpServiceTracker.close();
		this.kernelServiceTracker.close();
		
		this.httpServiceTracker = null;
		this.kernelServiceTracker = null;
		
		if (this.registered) {
			httpService.unregister("/smartlights-api");
		}
		
		this.context = null;
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "REST API bundle stopped.");
		}
	}
}
