package be.abollaert.domotics.light.server.kernel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Mood;
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
public final class SerialDriver extends AbstractDriver {

	private static final String[] MODULE_PORTS = new String[] {
		
	};
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(SerialDriver.class
			.getName());
	
	/** The current channels for this driver. */
	private List<Channel> channels;
	
	/** The moods. */
	private final List<Mood> moods = new ArrayList<Mood>();
	
	public SerialDriver(final Storage storage) {
		super(storage);
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Loading moods.");
		}
		
		final List<StoredMoodInfo> moods = this.getStorage().getStoredMoods();
		
		for (final StoredMoodInfo moodInfo : moods) {
			this.moods.add(new MoodImpl(moodInfo.getId(), moodInfo.getName(), this, this.getStorage()));
		}
		
		for (final Mood mood : this.moods) {
			final List<StoredSwitchMoodElement> switchElements = this.getStorage().getSwitchElementsForMood(mood.getId());
			
			for (final StoredSwitchMoodElement storedSwitchElement : switchElements) {
				mood.addSwitchElement(storedSwitchElement.getModuleId(), storedSwitchElement.getChannelNumber(), storedSwitchElement.getRequestedState());
			}
			
			final List<StoredDimMoodElement> dimElements = this.getStorage().getDimElementsForMood(mood.getId());
			
			for (final StoredDimMoodElement storedMoodElement : dimElements) {
				mood.addDimElement(storedMoodElement.getModuleId(), storedMoodElement.getChannelNumber(), storedMoodElement.getTargetPercentage());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<Channel> searchChannels() {
		final List<Channel> channels = new ArrayList<Channel>();
	
		for (final String port : MODULE_PORTS)  {
			final Channel channel = new SerialChannel(port);
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
		final StoredMoodInfo moodInfo = this.getStorage().saveMoodInformation(mood.getId(), mood.getName());
		
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
			this.getStorage().removeMood(id);
			this.moods.remove(matchingMood);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final Mood getNewMood(final String name) {
		final Mood newMood = new MoodImpl(-1, name, this, this.getStorage());
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
}
