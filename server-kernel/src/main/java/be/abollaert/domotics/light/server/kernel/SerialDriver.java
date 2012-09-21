package be.abollaert.domotics.light.server.kernel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.driver.base.AbstractDriver;
import be.abollaert.domotics.light.driver.base.Channel;
import be.abollaert.domotics.light.driver.base.ChannelType;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;
import be.abollaert.domotics.light.server.kernel.persistence.StoredDimMoodElement;
import be.abollaert.domotics.light.server.kernel.persistence.StoredMoodInfo;
import be.abollaert.domotics.light.server.kernel.persistence.StoredSwitchMoodElement;

/**
 * Main entry point.
 * 
 * @author alex
 *
 */
public final class SerialDriver extends AbstractDriver implements ManagedService {
	
	/** The property containing the device paths. */
	public static final String PROPNAME_DEVICE_PATHS = "devicePaths";
	
	/** The PID. */
	public static final String PID = "smartlights.serialdriver";

	/** For the moment : hardcoded module IDs. FIXME : Add config option for this. */
	private static final String[] MODULE_PORTS = new String[] {
		"/dev/ttyUSBftdi_A60048vp",
		"/dev/ttyUSBftdi_A6004cJi",
		"/dev/ttyUSBftdi_A6004pOf"
	};
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(SerialDriver.class
			.getName());
	
	/** The current channels for this driver. */
	private List<Channel> channels = new ArrayList<Channel>();
	
	/** The moods. */
	private final List<Mood> moods = new ArrayList<Mood>();
	
	private String[] modulePorts;
	
	private final Storage storage;
	
	public SerialDriver(final Storage storage) {
		super();
		
		this.storage = storage;
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Loading moods.");
		}
		
		final List<StoredMoodInfo> moods = this.storage.getStoredMoods();
		
		for (final StoredMoodInfo moodInfo : moods) {
			this.moods.add(new MoodImpl(moodInfo.getId(), moodInfo.getName(), this, this.storage));
		}
		
		for (final Mood mood : this.moods) {
			final List<StoredSwitchMoodElement> switchElements = this.storage.getSwitchElementsForMood(mood.getId());
			
			for (final StoredSwitchMoodElement storedSwitchElement : switchElements) {
				mood.addSwitchElement(storedSwitchElement.getModuleId(), storedSwitchElement.getChannelNumber(), storedSwitchElement.getRequestedState());
			}
			
			final List<StoredDimMoodElement> dimElements = this.storage.getDimElementsForMood(mood.getId());
			
			for (final StoredDimMoodElement storedMoodElement : dimElements) {
				mood.addDimElement(storedMoodElement.getModuleId(), storedMoodElement.getChannelNumber(), storedMoodElement.getTargetPercentage());
			}
		}
	}
	
	private final List<Channel> searchChannels() throws IOException {
		final List<Channel> channels = new ArrayList<Channel>();
	
		for (final String port : this.modulePorts) {
			final File portFile = new File(port);
			
			final Channel channel = new SerialChannel(portFile.getCanonicalPath());
			channels.add(channel);
		}
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Serial driver found [" + channels.size() + "] channels.");
		}
		
		this.channels = channels;
		
		return channels;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void unload() throws IOException {
		for (final Channel channel : this.channels) {
			if (channel.isConnected()) {
				channel.disconnect();
			}
		}
		
		this.channels.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<Mood> getAllMoods() throws IOException {
		return this.moods;
	}
	
	public final int saveMood(final Mood mood) {
		final StoredMoodInfo moodInfo = this.storage.saveMoodInformation(mood.getId(), mood.getName());
		
		if (mood.getId() == -1) {
			mood.setId(moodInfo.getId());
			this.moods.add(mood);
		}
		
		return moodInfo.getId();
	}

	@Override
	public Mood getMoodWithID(int id) {
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
	public final void removeMood(final int id) throws IOException {
		final Mood matchingMood = this.getMoodWithID(id);
		
		if (matchingMood != null) {
			this.storage.removeMood(id);
			this.moods.remove(matchingMood);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final Mood getNewMood(final String name) {
		final Mood newMood = new MoodImpl(-1, name, this, this.storage);
		this.moods.add(newMood);
		return newMood;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void allLightsOff() throws IOException {
		for (final DigitalModule digitalModule : this.getAllDigitalModules()) {
			for (int channel = 0; channel < digitalModule.getDigitalConfiguration().getNumberOfChannels(); channel++) {
				digitalModule.switchOutputChannel(channel, ChannelState.OFF);
			}
		}
		
		for (final DimmerModule dimmerModule : this.getAllDimmerModules()) {
			for (int channel = 0; channel < dimmerModule.getDimmerConfiguration().getNumberOfChannels(); channel++) {
				dimmerModule.switchOutputChannel(channel, ChannelState.OFF);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getVersion() {
		return this.getClass().getPackage().getImplementationVersion();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void updated(final Dictionary properties) throws ConfigurationException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Configuration updated.");
		}
		
		if (properties != null) {
			final String devicePathsValue = (String)properties.get(PROPNAME_DEVICE_PATHS);
			
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Received device paths value : [" + devicePathsValue + "]");
			}
			
			if (devicePathsValue != null) {
				final String[] devicePaths = devicePathsValue.split(",");
				
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Using device paths [" + Arrays.toString(devicePaths) + "]");
				}
				
				this.modulePorts = devicePaths;
				
				try {
					this.unload();
					this.probe();
				} catch (IOException e) {
					if (logger.isLoggable(Level.SEVERE)) {
						logger.log(Level.SEVERE, "Could not restart the serial driver due to an IO error probing : [" + e.getMessage() + "]", e);
					}
				}
			}
		} else {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Properties were null, configuration has not changed.");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void probe() throws IOException {
		for (final Channel channel : this.searchChannels()) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Connecting to channel [" + channel.getName() + "]");
			}
			
			channel.connect();
			
			final ChannelType channelType = channel.probeType();
			
			if (channelType != null) {
				if (channelType == ChannelType.DIGITAL) {
					final DigitalModule module = new ModuleImpl(channel, channelType, this.storage);
					this.addDigitalModule(module);
					
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Digital Module on [" + channel.getName() + "], ID [" + module.getId() + "]");
					}
				} else {
					final DimmerModule module = new ModuleImpl(channel, channelType, this.storage);
					this.addDimmerModule(module);
					
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Dimmer Module on [" + channel.getName() + "], ID [" + module.getId() + "]");
					}
				}
			} else {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Could not detect module on port [" + channel.getName() + "]");
				}
				
				channel.disconnect();
			}
		}
	}
}
