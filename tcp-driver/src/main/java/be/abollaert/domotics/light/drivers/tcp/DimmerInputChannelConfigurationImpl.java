package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DimmerDirection;
import be.abollaert.domotics.light.api.DimmerInputChannelConfiguration;
import be.abollaert.domotics.light.protocolbuffers.Api;

final class DimmerInputChannelConfigurationImpl implements DimmerInputChannelConfiguration {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(DimmerInputChannelConfigurationImpl.class.getName());
	
	/** The TCP client. */
	private final TCPClient tcpClient;
	
	/** The module configuration. */
	private final DimmerModuleImpl module;
	
	/** The module ID. */
	private final int moduleId;
	
	/** The channel number. */
	private final int channelNumber;
	
	/** The timer. */
	private int timer;
	
	/** The default state. */
	private ChannelState defaultState;
	
	/** The default direction. */
	private DimmerDirection defaultDirection;
	
	/** The mapped output channel. */
	private int mappedOutputChannel;
	
	/** The default percentage. */
	private int defaultPercentage;
	
	/** The name. */
	private String name;
	
	/** Indicates if logging is enabled. */
	private boolean loggingEnabled;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	tcpClient
	 * @param 	moduleId
	 * @param 	channelNumber
	 */
	DimmerInputChannelConfigurationImpl(final TCPClient tcpClient, final DimmerModuleImpl module, final int moduleId, final int channelNumber) {
		this.tcpClient = tcpClient;
		this.moduleId = moduleId;
		this.channelNumber = channelNumber;
		this.module = module;
		
		try {
			this.load();
		} catch (IOException e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, "Could not load configuration for channel [" + this.channelNumber + "] on dimmer module [" + this.moduleId + "] due to an IO error.", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DimmerDirection getDefaultDirection() throws IOException {
		return this.defaultDirection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getDefaultPercentage() throws IOException {
		return this.defaultPercentage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChannelState getDefaultState() throws IOException {
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
	public final void setDefaultDirection(final DimmerDirection direction) throws IOException {
		this.defaultDirection = direction;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaultPercentage(final int percentage) throws IOException {
		this.defaultPercentage = percentage;
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
	public final void setTimerInSeconds(int seconds) throws IOException {
		this.timer = seconds;
	}
	
	/**
	 * Loads the configuration. 
	 * 
	 * @throws IOException
	 */
	private final void load() throws IOException {
		final Api.DimmerInputChannelConfig configuration = this.tcpClient.getDimmerInputChannelConfiguration(this.moduleId, this.channelNumber);
		
		this.setTimerInSeconds(configuration.getTimerInSec());
		this.setDefaultPercentage(configuration.getDefaultPercentage());
		this.setDefaultState(configuration.getDefaultState() ? ChannelState.ON : ChannelState.OFF);
		this.setDefaultDirection(configuration.getDefaultDirection()? DimmerDirection.UP : DimmerDirection.DOWN);
		this.setMappedOutputChannel(configuration.getMappedOutputChannel());
		
		this.module.setOutputChannelState(this.getMappedOutputChannel(), configuration.getCurrentOutputState()? ChannelState.ON : ChannelState.OFF);
		this.module.setInputChannelState(this.channelNumber, configuration.getCurrentSwitchState()? ChannelState.ON : ChannelState.OFF);
		this.module.setDimmerPercentage(this.channelNumber, configuration.getCurrentDimmerPercentage());
		
		if (configuration.hasName()) {
			this.setName(configuration.getName());
		}
		
		if (configuration.hasEnableLogging()) {
			this.setLoggingEnabled(configuration.getEnableLogging());
		}
	}

	/**
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
	 * 
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
