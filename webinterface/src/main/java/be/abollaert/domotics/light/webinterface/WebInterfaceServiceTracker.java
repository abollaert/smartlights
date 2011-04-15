package be.abollaert.domotics.light.webinterface;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.felix.http.api.ExtHttpService;
import org.apache.wicket.protocol.http.WicketFilter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import be.abollaert.domotics.light.api.Driver;

/**
 * Tracks the {@link HttpService} and loads the servlets if it becomes available. It removes them when the service is not available.
 * 
 * @author alex
 */
final class WebInterfaceServiceTracker extends ServiceTracker {
	
	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(WebInterfaceServiceTracker.class.getName());
	
	/** The driver. */
	private Driver driver;
	
	/** The {@link HttpService}. */
	private ExtHttpService httpService;
	
	/** The wicket servlet. */
	private javax.servlet.Filter wicketFilter;

	/**
	 * Create a new instance.
	 * 
	 * @param	context		The bundle context.
	 */
	WebInterfaceServiceTracker(final BundleContext context, final Filter filter) {
		super(context, filter, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Object addingService(ServiceReference reference) {
		System.out.println("Adding service");
		final Object svcObject = super.addingService(reference);
		
		if (svcObject != null) {
			boolean changed = false;
			
			System.out.println(svcObject);
			if (svcObject instanceof HttpService) {
				if (this.httpService == null) {
					changed = true;
				}
				
				this.httpService = (ExtHttpService)svcObject;
			} else if (svcObject instanceof Driver) {
				if (this.driver == null) {
					changed = true;
				}
				
				this.driver = (Driver)svcObject;
			}
			
			if (changed && this.httpService != null && this.driver != null) {
				this.register();
			}
		}
		
		return svcObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removedService(final ServiceReference reference, final Object service) {
		if (service instanceof Driver) {
			if (this.httpService != null) {
				this.unregister();
			}
			
			this.driver = null;
		} else if (service instanceof HttpService) {
			if (this.driver != null) {
				this.unregister();
			}
			
			this.httpService = null;
		}
		
	}
	
	
	private final void register() {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Registering wicket.");
		}
		
		final Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("applicationClassName", WebInterface.class.getName());
		props.put("filterMappingUrlPattern", "/admin/*");
		
		final WebInterfaceWicketFilter filter = new WebInterfaceWicketFilter();
		
		try {
			this.httpService.registerFilter(filter, "/admin/.*", props, 1, null);
		} catch (ServletException e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, "Servlet error while registering filter : [" + e.getMessage() + "]", e);
			}
		}
	}
	
	private final class WebInterfaceWicketFilter extends WicketFilter {
		@Override
	    protected final ClassLoader getClassLoader() {
	        return this.getClass().getClassLoader();
	    }
	}
	
	final void unregister() {
		if (this.wicketFilter != null) {
			this.httpService.unregisterFilter(this.wicketFilter);
			this.wicketFilter = null;
		}
	}
}
