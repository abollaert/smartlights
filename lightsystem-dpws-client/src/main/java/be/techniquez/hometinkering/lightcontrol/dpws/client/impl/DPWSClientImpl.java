package be.techniquez.hometinkering.lightcontrol.dpws.client.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.ws4d.java.client.Client;
import org.ws4d.java.communication.DPWSException;
import org.ws4d.java.discovery.ISearchParameter;
import org.ws4d.java.discovery.ISearchResult;
import org.ws4d.java.discovery.SearchManager;
import org.ws4d.java.discovery.SearchParameter;
import org.ws4d.java.service.AbstractAction;
import org.ws4d.java.service.PVI;
import org.ws4d.java.service.Parameter;
import org.ws4d.java.service.remote.IRemoteService;
import org.ws4d.java.util.Properties;
import org.ws4d.java.xml.QualifiedName;

import be.techniquez.hometinkering.lightcontrol.dpws.client.DPWSClient;
import be.techniquez.hometinkering.lightcontrol.dpws.client.DPWSClientListener;
import be.techniquez.hometinkering.lightcontrol.dpws.client.model.DigitalBoard;

/**
 * The DPWS client class we use in this project to do the communication with the DPWS
 * based server system.
 * 
 * @author alex
 *
 */
public final class DPWSClientImpl extends Client implements DPWSClient {
	
	/** Logger instance used in this class. */
	private static final Logger logger = Logger.getLogger(DPWSClientImpl.class.getName());
	
	/** The namespace the device resides in. */
	private static final String DEVICE_NAMESPACE = "http://www.techniquez.be/hometinkering/lightcontrol/dpws";
	
	/** The name of the device. */
	private static final String DEVICE_NAME = "LightControlDevice";
	
	/** The name of the action that gets us the configuration. */
	private static final String GET_CONFIGURATION_ACTION_NAME = "GetCurrentConfiguration";
	
	/** The port type of the service that holds the configuration service. */
	private static final String CONFIGURATION_SERVICE_ID = "http://www.techniquez.be/hometinkering/lightcontrol/dpws/ConfigurationService";
	
	/** The service ID of the light control service. */
	private static final String CONTROL_SERVICE_ID = "http://www.techniquez.be/hometinkering/lightcontrol/dpws/LightControlService";
	
	/** The configuration service. */
	private IRemoteService configurationService;
	
	/** The control service. */
	private IRemoteService controlService;
	
	/** The digital boards. */
	private final List<DigitalBoard> digitalBoards = new ArrayList<DigitalBoard>();
	
	/** Set containing the DPWS client listeners. */
	private final Set<DPWSClientListener> listeners = new HashSet<DPWSClientListener>();
	
	public DPWSClientImpl() {
		logger.info("Starting DPWS discovery process to discover light systems on the network...");
		
		// Setup the logging
		Properties.getInstance().addProperty(Properties.PROP_LOG_LEVEL, 4);
	
		this.start();
		
		logger.info("Searching for services...");
		
		ISearchParameter searchServiceParameter = new SearchParameter(this);
		searchServiceParameter.addDeviceType(new QualifiedName(DEVICE_NAME, DEVICE_NAMESPACE));
		searchServiceParameter.addServiceType(new QualifiedName("ConfigurationService", "http://www.techniquez.be/hometinkering/lightcontrol/dpws/service/configuration"));
		
		SearchManager.getInstance().searchService(searchServiceParameter);
	
		searchServiceParameter = new SearchParameter(this);
		searchServiceParameter.addDeviceType(new QualifiedName(DEVICE_NAME, DEVICE_NAMESPACE));
		searchServiceParameter.addServiceType(new QualifiedName("LightControlService", "http://www.techniquez.be/hometinkering/lightcontrol/dpws/service/control"));
		
		SearchManager.getInstance().searchService(searchServiceParameter);
		
		logger.info("Done, client is initialized and ready to go...");
	}
	
	/**
	 * Adds the given listener to the set of listeners.
	 * 
	 * @param 	listener	The listener to add.
	 */
	public final void addListener(final DPWSClientListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Removes the given listener from the set of listeners.
	 * 
	 * @param 	listener	The listener to remove.
	 */
	public final void removeListener(final DPWSClientListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Loads the boards.
	 */
	private final void loadBoards() throws DPWSException {
		logger.info("Loading the boards...");
		
		final AbstractAction getConfigurationAction = this.configurationService.getAction(GET_CONFIGURATION_ACTION_NAME, "ConfigurationService");
		
		logger.info("Asking the device for it's configuration...");
		
		getConfigurationAction.invoke();
		
		logger.info("Done, configuration received, interpreting...");
		
		final Parameter response = getConfigurationAction.getOutputParameter("CurrentConfiguration");
		
		final PVI[] pathToNumberOfBoards = PVI.createPath("NumberOfBoards");
		pathToNumberOfBoards[0].setIndex(0);
		
		final PVI[] pathToBoardID = PVI.createPath("Board/BoardID");
		pathToBoardID[0].setIndex(0);
		pathToBoardID[1].setIndex(0);
		
		final PVI[] pathToBoardType = PVI.createPath("Board/BoardType");
		pathToBoardType[0].setIndex(0);
		pathToBoardType[1].setIndex(0);
		
		final PVI[] pathToBoardDriverName = PVI.createPath("Board/DriverName");
		pathToBoardDriverName[0].setIndex(0);
		pathToBoardDriverName[1].setIndex(0);
		
		final PVI[] pathToNumberOfChannels = PVI.createPath("Board/NumberOfChannels");
		pathToNumberOfChannels[0].setIndex(0);
		pathToNumberOfChannels[1].setIndex(0);
		
		final PVI[] pathToChannelNumber = PVI.createPath("Board/Channel/ChannelNumber");
		pathToChannelNumber[0].setIndex(0);
		pathToChannelNumber[1].setIndex(0);
		pathToChannelNumber[2].setIndex(0);
		
		final PVI[] pathToLightName = PVI.createPath("Board/Channel/LightName");
		pathToLightName[0].setIndex(0);
		pathToLightName[1].setIndex(0);
		pathToLightName[2].setIndex(0);
		
		// First we need to know the number of boards...
		final int numberOfBoards = Integer.valueOf(response.getValue(pathToNumberOfBoards).toString());
		
		for (int i = 0; i < numberOfBoards; i++) {
			// Process the current board...
			// Set the indices on board information PVIs...
			pathToBoardID[0].setIndex(i);
			pathToBoardDriverName[0].setIndex(i);
			pathToBoardType[0].setIndex(i);
			pathToNumberOfChannels[0].setIndex(i);
			
			// Extract the general values...
			final int boardId = Integer.valueOf(response.getValue(pathToBoardID).toString());
			final String boardDriverName = response.getValue(pathToBoardDriverName).toString();
			final String boardType = response.getValue(pathToBoardType).toString();
			final int numberOfChannels = Integer.valueOf(response.getValue(pathToNumberOfChannels).toString());
			
			System.out.println(boardType);
			if (boardType.equals("DIGITAL")) {
				final DigitalBoard board = new DigitalBoard(String.valueOf(boardId), null, boardDriverName);
			
				System.out.println(numberOfChannels);
				for (int j = 0; j < numberOfChannels; j++) {
					pathToChannelNumber[0].setIndex(i);
					pathToChannelNumber[1].setIndex(j);
					pathToChannelNumber[2].setIndex(0);
					
					pathToLightName[0].setIndex(i);
					pathToLightName[1].setIndex(j);
					pathToLightName[2].setIndex(0);
					
					final int channelNumber = Integer.valueOf(response.getValue(pathToChannelNumber).toString());
					final String lightName = response.getValue(pathToLightName).toString();
					
					if (lightName.equals("NONE")) {
						board.setChannelOccupation(channelNumber, null);
					} else {
						board.setChannelOccupation(channelNumber, lightName);
					}
				}
				
				this.digitalBoards.add(board);
			}
		}
		
		logger.info("Done, boards have been loaded...");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void reload() throws DPWSException {
		this.digitalBoards.clear();
		this.loadBoards();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final List<DigitalBoard> getDigitalBoards() {
		return this.digitalBoards;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onServiceFound(final ISearchResult result) {
		logger.info("Service found [" + result.getRemoteService().getServiceId() + "]");
		
		final IRemoteService service = result.getRemoteService();
		
		if (service.getServiceId().equals(CONFIGURATION_SERVICE_ID)) {
			this.configurationService = service;
		} else if (service.getServiceId().equals(CONTROL_SERVICE_ID)) {
			this.controlService = service;
		}
		
		if (this.configurationService != null && this.controlService != null) {
			for (final DPWSClientListener listener : this.listeners) {
				listener.clientInitialized();
			}
		}
	}
}
