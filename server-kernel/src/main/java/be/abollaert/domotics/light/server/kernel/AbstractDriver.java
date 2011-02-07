package be.abollaert.domotics.light.server.kernel;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;

public abstract class AbstractDriver implements Driver {

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(AbstractDriver.class
			.getName());
	
	/** The digital modules. */
	private final Set<DigitalModule> digitalModules = new HashSet<DigitalModule>();
	
	/** The dimmer modules. */
	private final Set<DimmerModule> dimmerModules = new HashSet<DimmerModule>();
	
	/** The storage. */
	private final Storage storage;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Set<DigitalModule> getAllDigitalModules() {
		return this.digitalModules;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Set<DimmerModule> getAllDimmerModules() {
		return this.dimmerModules;
	}
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	storage		The storage.
	 */
	public AbstractDriver(final Storage storage) {
		this.storage = storage;
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
					this.digitalModules.add(module);
					
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Digital Module on [" + channel.getName() + "], ID [" + module.getId() + "]");
					}
				} else {
					final DimmerModule module = new ModuleImpl(channel, channelType, this.storage);
					this.dimmerModules.add(module);
					
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
	
	protected abstract List<Channel> searchChannels();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DigitalModule getDigitalModuleWithID(final int id) {
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
	 * Returns the storage.
	 * 
	 * @return	The storage.
	 */
	final Storage getStorage() {
		return this.storage;
	}
}
