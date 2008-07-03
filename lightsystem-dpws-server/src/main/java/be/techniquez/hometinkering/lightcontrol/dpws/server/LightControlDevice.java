package be.techniquez.hometinkering.lightcontrol.dpws.server;

import java.util.Set;
import java.util.logging.Logger;

import org.ws4d.java.service.HostingService;

import be.techniquez.hometinkering.lightcontrol.dpws.server.services.AbstractDPWSService;

/**
 * The hosting service, which is in fact the device itself.
 * 
 * @author alex
 */
public final class LightControlDevice extends HostingService {
	
	/** Logger instance as used in this class. */
	private static final Logger logger = Logger.getLogger(LightControlDevice.class.getName());
	
	/** The locale. */
	private static final String LOCALE_BE = "nl-BE";
	
	/**
	 * Creation of the device.
	 */
	public LightControlDevice(final String namespace, final String porttype, final String manufacturer, final String friendlyName, final String modelName, final String firmwareVersion, final String serialNumber, final Set<AbstractDPWSService> hostedServices) {
		super(porttype, namespace);
		
		logger.info("Setting device parameters...");
		
		this.setManufacturerName(LOCALE_BE, manufacturer);
		this.setFriendlyName(LOCALE_BE, friendlyName);
		this.setModelName(LOCALE_BE, modelName);
		this.setFirmwareVersion(firmwareVersion);
		this.setSerialNumber(serialNumber);
		
		logger.info("Adding services...");
		
		for (final AbstractDPWSService hostedService : hostedServices) {
			logger.info("Adding hosted service [" + hostedService.getServiceId() + "]");
			
			this.addHostedService(hostedService);
		}
		
		logger.info("Done, device has been initialized and is ready for use...");
	}
}
