package be.abollaert.domotics.light.webinterface;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.service.http.HttpService;

import be.abollaert.domotics.light.api.Driver;

/**
 * 
 * @author alex
 *
 */
public final class WebInterfaceActivator implements BundleActivator {
	
	/** Logger definition. */
	private static final Logger logger = Logger
			.getLogger(WebInterfaceActivator.class.getName());
	
	
	/** The filter selects the http service as well as the driver. */
	private static final String SVCS_FILTER = "(|(objectclass=" + Driver.class.getName() + ")(objectclass=" + HttpService.class.getName() + "))";

	private WebInterfaceServiceTracker tracker;
	
	@Override
	public final void start(final BundleContext context) throws Exception {
		final Filter filter = context.createFilter(SVCS_FILTER);
		this.tracker = new WebInterfaceServiceTracker(context, filter);
		this.tracker.open();
	}

	@Override
	public final void stop(final BundleContext context) throws Exception {
		if (this.tracker != null) {
			this.tracker.unregister();
			this.tracker.close();
			this.tracker = null;
		}
	}

}
