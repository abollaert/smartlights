package be.techniquez.hometinkering.lightcontrol.dpws.server;

import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.ws4d.java.eventing.EventManager;
import org.ws4d.java.service.HostedService;
import org.ws4d.java.util.Properties;

/**
 * The DPWS server.
 * 
 * @author alex
 */
public final class Server {
	
	/** Logger instance that gets used in the server. */
	private static final Logger logger = Logger.getLogger(Server.class.getName());
	
	/** Location of the spring configuration file we use to bootstrap the server. */
	private static final String SPRING_CONFIGURATION_FILE_LOCATION = "/be/techniquez/hometinkering/lightcontrol/dpws/server/lightcontrol-dpws-device.xml";
	
	/** The application context for the sever. */
	private final ApplicationContext applicationContext;
	
	/**
	 * Initializes the server. This entails bootstrapping spring and the repository.
	 */
	public Server() {
		logger.info("Initializing DPWS server, bootstrapping Spring...");
		
		this.applicationContext = new ClassPathXmlApplicationContext(SPRING_CONFIGURATION_FILE_LOCATION);
		
		logger.info("Done, application context has been initialized, starting device...");
		
		final LightControlDevice device = (LightControlDevice)this.applicationContext.getBean("lightControlDPWSDevice");
		
		logger.info("Setting up eventing...");
		
		EventManager.initEventing();
		
		for (final Object service : device.getHostedServices().values()) {
			EventManager.getInstance().addServiceEvents((HostedService)service);
		}
		
		logger.info("Starting device...");
		
		device.start();
		
		logger.info("Done, server has been bootstrapped and started...");
		
		// Setup the logging
		Properties.getInstance().addProperty(Properties.PROP_LOG_LEVEL, 4);
	}

	/**
	 * Main, starts up the application.
	 * 
	 * @param 	args	The arguments.
	 */
	public static void main(String[] args) {
		new Server();
	}
}