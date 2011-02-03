package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
final class TCPServerServiceTracker extends ServiceTracker {
	
	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(TCPServerServiceTracker.class.getName());
	
	/** The {@link GetModulesHandler}. */
	private final GetModulesHandler getModulesHandler = new GetModulesHandler();
	
	/** The {@link SwitchOutputHandler}. */
	private final SwitchOutputHandler switchOutputHandler = new SwitchOutputHandler();
	
	/** The handler for the fetch of the output channels. */
	private final GetDigitalOutputChannelsHandler getOutputChannelsHandler = new GetDigitalOutputChannelsHandler();
	
	/** The {@link GetDigitalInputChannelConfigHandler}. */
	private final GetDigitalInputChannelConfigHandler getDigitalInputChannelConfigHandler = new GetDigitalInputChannelConfigHandler();
	
	/** The handler responsible for configuring input channels. */
	private final SetDigitalInputChannelConfigurationHandler setDigitalInputConfigHandler = new SetDigitalInputChannelConfigurationHandler();
	
	/** The handler that sets the module configuration. */
	private final SetDigitalModuleConfigHandler setDigitalModuleConfigHandler = new SetDigitalModuleConfigHandler();
	
	/** The handler that saves the configuration of a digital module. */
	private final SaveModuleConfigHandler saveDigitalModuleConfigHandler = new SaveModuleConfigHandler();
	
	/** The handler to return the configuration of one digital module. */
	private final GetDigitalModuleConfigurationHandler getDigitalModuleConfigHandler = new GetDigitalModuleConfigurationHandler();
	
	/** The handler that allows you to set the configuration of a dimmer module. */
	private final SetDimmerModuleConfigHandler setDimmerModuleConfigurationHandler = new SetDimmerModuleConfigHandler();
	
	/** The handler that gets the dimmer input configuration. */
	private final GetDimmerInputChannelConfigHandler getDimmerInputChannelConfigHandler = new GetDimmerInputChannelConfigHandler();
	
	/** The handler for setting the configuration for a dimmer input. */
	private final SetDimmerInputConfigurationHandler setDimmerInputConfigurationHandler = new SetDimmerInputConfigurationHandler();
	
	/** The handler for getting the dimmer output state. */
	private final GetDimmerOutputChannelStateHandler getDimmerOutputChannelStateHandler = new GetDimmerOutputChannelStateHandler();
	
	/** The handler for getting the digital output state. */
	private final GetDigitalOutputChannelStateHandler getDigitalOutputChannelStateHandler = new GetDigitalOutputChannelStateHandler();
	
	private final SaveMoodHandler saveMoodHandler = new SaveMoodHandler();
	
	/** THe handler for dimming. */
	private final DimHandler dimHandler = new DimHandler();
	
	/** The handler for fetching switch events. */
	private final GetSwitchEventsHandler getSwitchEventsHandler = new GetSwitchEventsHandler();
	
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Object addingService(ServiceReference reference) {
		final Object svcObject = super.addingService(reference);
		
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
				
				this.getModulesHandler.register(this.httpService, context, this.driver);
				this.getDigitalInputChannelConfigHandler.register(this.httpService, context, this.driver);
				this.getOutputChannelsHandler.register(this.httpService, context, this.driver);
				this.switchOutputHandler.register(this.httpService, context, this.driver);
				this.setDigitalInputConfigHandler.register(this.httpService, context, this.driver);
				this.setDigitalModuleConfigHandler.register(this.httpService, context, this.driver);
				this.saveDigitalModuleConfigHandler.register(this.httpService, context, this.driver);
				this.getDigitalModuleConfigHandler.register(this.httpService, context, this.driver);
				this.setDimmerModuleConfigurationHandler.register(this.httpService, context, this.driver);
				this.getDimmerInputChannelConfigHandler.register(this.httpService, context, this.driver);
				this.dimHandler.register(this.httpService, context, this.driver);
				this.setDimmerInputConfigurationHandler.register(this.httpService, context, this.driver);
				this.getSwitchEventsHandler.register(this.httpService, context, this.driver);
				this.getDigitalOutputChannelStateHandler.register(this.httpService, context, this.driver);
				this.getDimmerOutputChannelStateHandler.register(this.httpService, context, this.driver);
				this.saveMoodHandler.register(this.httpService, context, this.driver);
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
		this.httpService.unregister(this.getModulesHandler.getURI());
		this.httpService.unregister(this.getDigitalInputChannelConfigHandler.getURI());
		this.httpService.unregister(this.getOutputChannelsHandler.getURI());
		this.httpService.unregister(this.switchOutputHandler.getURI());
		this.httpService.unregister(this.setDigitalInputConfigHandler.getURI());
		this.httpService.unregister(this.setDigitalModuleConfigHandler.getURI());
		this.httpService.unregister(this.saveDigitalModuleConfigHandler.getURI());
		this.httpService.unregister(this.getDigitalModuleConfigHandler.getURI());
		this.httpService.unregister(this.setDimmerModuleConfigurationHandler.getURI());
		this.httpService.unregister(this.getDimmerInputChannelConfigHandler.getURI());
		this.httpService.unregister(this.dimHandler.getURI());
		this.httpService.unregister(this.setDimmerInputConfigurationHandler.getURI());
		this.httpService.unregister(this.getSwitchEventsHandler.getURI());
		this.httpService.unregister(this.getDigitalOutputChannelStateHandler.getURI());
		this.httpService.unregister(this.getDimmerOutputChannelStateHandler.getURI());
		this.httpService.unregister(this.saveMoodHandler.getURI());
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
