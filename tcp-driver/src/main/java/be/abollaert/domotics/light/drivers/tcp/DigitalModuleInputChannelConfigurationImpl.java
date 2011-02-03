package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModuleConfiguration;
import be.abollaert.domotics.light.protocolbuffers.Api;

/**
 * Input channel implementation.
 * 
 * @author alex
 */
final class DigitalModuleInputChannelConfigurationImpl implements DigitalInputChannelConfiguration {
	
	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(DigitalModuleInputChannelConfigurationImpl.class
					.getName());
	
	/** The TCP client. */
	private final TCPClient tcpClient;
	
	/** The module configuration. */
	private final DigitalModuleConfiguration parent;
	
	/** The channel number. */
	private final int channelNumber;
	
	/** The mapped output channel. */
	private int mappedOutputChannel;
	
	/** The default state. */
	private ChannelState defaultState;
	
	/** The timer. */
	private int timer;
	
	/** The ID of the module. */
	private final int moduleId;
	
	/** The name of the channel. */
	private String name;
	
	/** <code>true</code> if logging is enabled, <code>false</code> otherwise. */
	private boolean loggingEnabled;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	tcpClient		The tcp client.
	 * @param	parent			The parent configuration.
	 * @param	channelNumber	The channel number.
	 */
	DigitalModuleInputChannelConfigurationImpl(final TCPClient tcpClient, final DigitalModuleConfiguration parent, final int channelNumber, final int moduleId) {
		this.tcpClient = tcpClient;
		this.parent = parent;
		this.channelNumber = channelNumber;
		this.moduleId = moduleId;
		
		try {
			this.loadConfig();
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "IO error when loading channel configuration for channel : [" + e.getMessage() + "]", e);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ChannelState getDefaultState() throws IOException {
		return this.defaultState;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getMappedOutputChannel() throws IOException {
		return this.mappedOutputChannel;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getTimerInSeconds() throws IOException {
		return this.timer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setDefaultState(final ChannelState state) throws IOException {
		this.defaultState = state;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setMappedOutputChannel(final int outputChannel) throws IOException {
		this.mappedOutputChannel = outputChannel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setTimerInSeconds(final int seconds) throws IOException {
		this.timer = seconds;
	}
	
	/**
	 * Loads the configuration.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	private final void loadConfig() throws IOException {
		final Api.DigitalInputChannelConfig configuration = this.tcpClient.getDigitalChannelConfiguration(this.parent.getModuleId(), this.channelNumber).getConfig();
		
		this.mappedOutputChannel = configuration.getMappedOutputChannel();
		this.defaultState = configuration.getDefaultState() ? ChannelState.ON : ChannelState.OFF;
		this.timer = configuration.getTimerInSec();
		this.name = configuration.getName();
		this.loggingEnabled = configuration.getEnableLogging();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getChannelNumber() {
		return this.channelNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getModuleId() {
		return this.moduleId;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public final String getName() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isLoggingEnabled() {
		return this.loggingEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setLoggingEnabled(final boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setName(final String name) {
		this.name = name;
	}

}
