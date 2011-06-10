package be.abollaert.domotics.light.webinterface;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * 
 * @author alex
 *
 */
public final class WebInterfaceActivator implements BundleActivator {
	
	/** The symbolic name of the TCP server bundle. */
	private static final String TCP_SERVER_SYMBOLIC_NAME = "be.abollaert.domotics.lights.tcp-server";
	
	/** Logger definition. */
	private static final Logger logger = Logger
			.getLogger(WebInterfaceActivator.class.getName());
	
	private BundleContext context;
	
	private boolean tcpServerBundleStarted;
	
	private boolean httpServicePresent;
	
	private boolean deployed;
	
	/** The filter selects the http service as well as the driver. */
	private static final String SVC_FILTER = "(objectclass=" + HttpService.class.getName() + ")";
	
	private final BundleListener tcpServerListener = new BundleListener() {
		@Override
		public final void bundleChanged(final BundleEvent event) {
			final Bundle bundle = event.getBundle();
			
			System.out.println("Bundle changed.");
			if (bundle.getSymbolicName() != null && bundle.getSymbolicName().equals(TCP_SERVER_SYMBOLIC_NAME)) {
				switch (event.getType()) {
					case BundleEvent.STARTED: {
						tcpServerBundleStarted = true;
						
						updateState();
						
						break;
					}
					
					case BundleEvent.STOPPED: {
						tcpServerBundleStarted = false;
						
						updateState();
						
						break;
					}
				}
			}
		}
	};
	
	private final ServiceListener httpServiceListener = new ServiceListener() {
		@Override
		public final void serviceChanged(final ServiceEvent event) {
			switch (event.getType()) {
				case ServiceEvent.REGISTERED: {
					httpServicePresent = true;
					
					updateState();
					
					break;
				}
				
				case ServiceEvent.UNREGISTERING: {
					httpServicePresent = false;
					
					updateState();
					break;
				}
			}
		}
	};
	
	@Override
	public final void start(final BundleContext context) throws Exception {
		this.context = context;
		
		this.context.addBundleListener(this.tcpServerListener);
		this.context.addServiceListener(this.httpServiceListener, SVC_FILTER);
		
		if (this.getHttpService() != null) {
			this.httpServicePresent = true;
		}
		
		if (this.getTCPServerBundle() != null) {
			this.tcpServerBundleStarted = true;
		}
		
		this.updateState();
	}
	
	private final HttpService getHttpService() {
		if (this.context != null) {
			final ServiceReference serviceRef = this.context.getServiceReference(HttpService.class.getName());
			
			if (serviceRef != null) {
				return (HttpService)this.context.getService(serviceRef);
			}
		}
		
		return null;
	}
	
	private final Bundle getTCPServerBundle() {
		if (this.context != null) {
			final Bundle[] installedBundles = this.context.getBundles();
			
			for (final Bundle bundle : installedBundles) {
				if (bundle.getSymbolicName() != null && bundle.getSymbolicName().equals(TCP_SERVER_SYMBOLIC_NAME) && bundle.getState() == Bundle.ACTIVE) {
					return bundle;
				}
			}
		}
		
		return null;
	}
	
	private final void updateState() {
		if (this.deployed) {
			if (this.httpServicePresent == false || this.tcpServerBundleStarted == false) {
				this.unregister();
			}
		} else {
			if (this.httpServicePresent && this.tcpServerBundleStarted) {
				this.register();
			}
		}
	}
	
	private final void register() {
		final HttpService httpService = this.getHttpService();
		final HttpContext context = httpService.createDefaultHttpContext();
		
		try {
			httpService.registerResources("/admin", "/web", context);
			this.deployed = true;
		} catch (NamespaceException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Namespace error while trying to register admin : [" + e.getMessage() + "]", e);
			}
		}
	}
	
	private final void unregister() {
		final HttpService httpService = this.getHttpService();
		
		httpService.unregister("/admin/");
		this.deployed = false;
	}

	@Override
	public final void stop(final BundleContext context) throws Exception {
		if (this.deployed) {
			this.unregister();
		}
		
		this.context.removeBundleListener(this.tcpServerListener);
		this.context.removeServiceListener(this.httpServiceListener);
		
		this.context = null;
		this.tcpServerBundleStarted = false;
		this.httpServicePresent = false;
	}
}
