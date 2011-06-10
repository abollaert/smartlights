package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.protocolbuffers.Api;

/**
 * The TCP driver runs over the protocol buffers based protocol.
 * 
 * @author alex
 */
public final class TCPDriver implements Driver {

	/** The server address. */
	private static final String SERVER_ADDRESS = "192.168.100.1";
	//private static final String SERVER_ADDRESS = "127.0.0.1";
	
	/** The server port. */
	private static final int SERVER_PORT = 8080;
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(TCPDriver.class
			.getName());

	/** The digital modules. */
	private final Set<DigitalModule> digitalModules = new HashSet<DigitalModule>();
	
	/** The dimmer modules. */
	private final Set<DimmerModule> dimmerModules = new HashSet<DimmerModule>();
	
	/** The moods. */
	private final List<Mood> moods = new ArrayList<Mood>();
	
	/** The {@link TCPClient}. */
	private TCPClient tcpClient;
	
	/**
	 * Create a new instance.
	 */
	public TCPDriver() {
		this.tcpClient = new TCPClient();
		
		try {
			this.tcpClient.connect(SERVER_ADDRESS, SERVER_PORT, null);
		} catch (IOException e) {
			throw new IllegalStateException("Could not connect to server at [" + SERVER_ADDRESS + ":" + SERVER_PORT + "] due to an IO error [" + e.getMessage() + "]", e);
		}
	}
	
	@Override
	public final Set<DigitalModule> getAllDigitalModules() {
		return this.digitalModules;
	}

	@Override
	public final Set<DimmerModule> getAllDimmerModules() {
		return this.dimmerModules;
	}

	@Override
	public final void probe() throws IOException {
		this.digitalModules.clear();
		this.dimmerModules.clear();
		
		final Api.GetModulesResponse response = this.tcpClient.getModules();
			
		for (final Api.Module module : response.getModulesList()) {
			switch (module.getType()) {
				case DIGITAL: {
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Adding digital module, ID [" + module.getDigitalModule().getModuleId() + "], number of channels [" + module.getDigitalModule().getNumberOfChannels() + "]");
					}
					
					final Api.DigitalModule digitalModule = module.getDigitalModule();
					
					final int moduleId = digitalModule.getModuleId();
					final int numberOfChannels = digitalModule.getNumberOfChannels();
					final String firmwareVersion = digitalModule.getFirmwareVersion();
					final int switchThreshold = digitalModule.getConfiguration().getSwitchThresholdInMs();
					
					final DigitalModuleImpl digitalModuleImpl = new DigitalModuleImpl(moduleId, numberOfChannels, firmwareVersion, switchThreshold, this.tcpClient);
					this.digitalModules.add(digitalModuleImpl);
					this.tcpClient.getEventListener().addEventListener(digitalModuleImpl);
					
					break;
				}
				
				case DIMMER: {
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Adding dimmer module, ID [" + module.getDimmerModule().getModuleId() + "], number of channels [" + module.getDimmerModule().getNumberOfChannels() + "]");
					}
					
					final Api.DimmerModule dimmerModule = module.getDimmerModule();
					
					final int moduleId = dimmerModule.getModuleId();
					final int numberOfChannels = dimmerModule.getNumberOfChannels();
					final String firmwareVersion = dimmerModule.getFirmwareVersion();
					final int switchThreshold = dimmerModule.getConfiguration().getSwitchThresholdInMs();
					final int dimmerDelay = dimmerModule.getConfiguration().getDimmerDelay();
					final int dimmerThreshold = dimmerModule.getConfiguration().getDimmerThresholdInMs();
					
					final DimmerModuleImpl dimmerModuleImpl = new DimmerModuleImpl(this.tcpClient, moduleId, numberOfChannels, firmwareVersion, switchThreshold, dimmerDelay, dimmerThreshold);
					this.dimmerModules.add(dimmerModuleImpl);
					this.tcpClient.getEventListener().addEventListener(dimmerModuleImpl);
					
					break;
				}
			}
		}
		
		final Api.MoodList moods = this.tcpClient.getAllMoods();
		
		for (final Api.Mood mood : moods.getMoodsList()) {
			final Mood newMood = this.getNewMood(mood.getName());
			newMood.setId(mood.getMoodId());
			
			for (final Api.SwitchMoodElement element : mood.getSwitchElementsList()) {
				newMood.addSwitchElement(element.getModuleId(), element.getChannelNumber(), element.getRequestedState() ? ChannelState.ON : ChannelState.OFF);
			}
			
			for (final Api.DimmerMoodElement element : mood.getDimmerElementsList()) {
				newMood.addDimElement(element.getModuleId(), element.getChannelNumber(), element.getPercentage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DigitalModule getDigitalModuleWithID(int id) {
		for (final DigitalModule module : this.digitalModules) {
			if (module.getId() == id) {
				return module;
			}
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DimmerModule getDimmerModuleWithID(int id) {
		for (final DimmerModule module : this.dimmerModules) {
			if (module.getId() == id) {
				return module;
			}
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void unload() throws IOException {
	}

	@Override
	public final List<Mood> getAllMoods() throws IOException {
		return this.moods;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Mood getNewMood(final String name) {
		final MoodImpl mood = new MoodImpl(this.tcpClient);
		mood.setName(name);
		mood.setId(-1);
		
		this.moods.add(mood);
		
		return mood;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Mood getMoodWithID(int id) {
		for (final Mood mood : this.moods) {
			if (mood.getId() == id) {
				return mood;
			}
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeMood(int id) throws IOException {
		this.tcpClient.removeMood(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void allLightsOff() throws IOException {
		this.tcpClient.allLightsOff();
	}

	/**
	 * @return
	 */
	@Override
	public final String getVersion() {
		return this.getClass().getPackage().getImplementationVersion();
	}
}
