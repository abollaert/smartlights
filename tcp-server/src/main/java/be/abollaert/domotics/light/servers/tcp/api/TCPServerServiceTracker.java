package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import be.abollaert.domotics.light.api.Driver;

/**
 * Tracks the {@link HttpService} and loads the servlets if it becomes available. It removes them when the service is not available.
 * 
 * @author alex
 */
final class TCPServerServiceTracker extends ServiceTracker {
	
	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(TCPServerServiceTracker.class.getName());
	
	private final List<AbstractHandler> handlers = new ArrayList<AbstractHandler>();
	
	/** The driver. */
	private Driver driver;
	
	/** The {@link HttpService}. */
	private HttpService httpService;
	
	/** The event dispatcher. */
	private final EventDispatcher eventDispatcher;

	/**
	 * Create a new instance.
	 * 
	 * @param	context		The bundle context.
	 */
	TCPServerServiceTracker(final BundleContext context, final Filter filter) {
		super(context, filter, null);
		
		this.eventDispatcher = new EventDispatcher();
		
		this.handlers.add(new GetModulesHandler());
		this.handlers.add(new SwitchOutputHandler());
		this.handlers.add(new GetDigitalOutputChannelsHandler());
		this.handlers.add(new GetDigitalInputChannelConfigHandler());
		this.handlers.add(new SetDigitalInputChannelConfigurationHandler());
		this.handlers.add(new SetDigitalModuleConfigHandler());
		this.handlers.add(new SaveModuleConfigHandler());
		this.handlers.add(new GetDigitalModuleConfigurationHandler());
		this.handlers.add(new SetDimmerModuleConfigHandler());
		this.handlers.add(new GetDimmerInputChannelConfigHandler());
		this.handlers.add(new SetDimmerInputConfigurationHandler());
		this.handlers.add(new GetDimmerOutputChannelStateHandler());
		this.handlers.add(new GetDigitalOutputChannelStateHandler());
		this.handlers.add(new SaveMoodHandler());
		this.handlers.add(new DimHandler());
		this.handlers.add(new GetSwitchEventsHandler());
		this.handlers.add(new GetAllMoodsHandler());
		this.handlers.add(new ActivateMoodHandler());
		this.handlers.add(new RemoveMoodHandler());
		this.handlers.add(new AllLightsOffHandler());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Object addingService(ServiceReference reference) {
		final Object svcObject = super.addingService(reference);
		
		System.out.println("TCP server [" + svcObject + "]");
		if (svcObject != null) {
			boolean changed = false;
			
			if (svcObject instanceof HttpService) {
				if (this.httpService == null) {
					changed = true;
				}
				
				this.httpService = (HttpService)svcObject;
			} else if (svcObject instanceof Driver) {
				if (this.driver == null) {
					changed = true;
				}
				
				this.driver = (Driver)svcObject;
			}
			
			if (changed && this.httpService != null && this.driver != null) {
				final HttpContext context = this.httpService.createDefaultHttpContext();
				
				try {
					this.eventDispatcher.start(this.driver);
				} catch (IOException e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Could not start event dispatcher due to an IO error [" + e.getMessage() + "]", e);
					}
				}
				
				for (final AbstractHandler handler : this.handlers) {
					handler.register(this.httpService, context, this.driver);
				}
				
				try {
					this.httpService.registerResources("/mobile", "/mobile", context);
				} catch (NamespaceException e) {
					e.printStackTrace();
				}
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
				this.unregisterHandlers();
				this.stopEventDispatcher();
			}
			
			this.driver = null;
		} else if (service instanceof HttpService) {
			if (this.driver != null) {
				this.unregisterHandlers();
				this.stopEventDispatcher();
			}
			
			this.httpService = null;
		}
		
	}
	
	/**
	 * Unregisters the handlers.
	 */
	private final void unregisterHandlers() {
		for (final AbstractHandler handler : this.handlers) {
			this.httpService.unregister(handler.getURI());
		}
	}
	
	/**
	 * In it's own method for the sole sake of an {@link IOException}.
	 */
	private final void stopEventDispatcher() {
		try {
			this.eventDispatcher.stop();
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Could not stop event dispatcher due to an IO error [" + e.getMessage() + "]", e);
			}
		}
	}
}
