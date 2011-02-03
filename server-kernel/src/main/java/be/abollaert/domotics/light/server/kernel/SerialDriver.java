package be.abollaert.domotics.light.server.kernel;

import gnu.io.CommPortIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;
import be.abollaert.domotics.light.server.kernel.persistence.StoredMoodInfo;

/**
 * Main entry point.
 * 
 * @author alex
 *
 */
public final class SerialDriver extends AbstractDriver {

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
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<Channel> searchChannels() {
		final List<Channel> channels = new ArrayList<Channel>();
	
		final Enumeration<CommPortIdentifier> identifiers = CommPortIdentifier.getPortIdentifiers();
		
		while (identifiers.hasMoreElements()) {
			final CommPortIdentifier identifier = identifiers.nextElement();
			
			if (identifier.getName().contains("ttyUSB")) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Probing on [" + identifier.getName() + "]");
				}
				
				final Channel channel = new SerialChannel(identifier.getName());
				channels.add(channel);
			}
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
}
