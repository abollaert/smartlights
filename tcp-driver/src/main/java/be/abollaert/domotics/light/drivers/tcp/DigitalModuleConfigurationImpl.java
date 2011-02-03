package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModuleConfiguration;

/**
 * Digital module configuration implementation running over TCP.
 * 
 * @author alex
 */
final class DigitalModuleConfigurationImpl implements DigitalModuleConfiguration {
	
	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(DigitalModuleConfigurationImpl.class.getName());

	/** The firmware version. */
	private final String firmwareVersion;
	
	/** The module ID. */
	private int moduleId;
	
	/** The number of channels. */
	private int numberOfChannels;
	
	/** The switch threshold. */
	private int switchThreshold;
	
	/** The TCP client. */
	private final TCPClient client;
	
	/** The input channel configurations. */
	private final DigitalInputChannelConfiguration[] inputChannelConfigurations;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 		moduleId			The ID of the module.
	 * @param 		numberOfChannels	The number of channels the module has.
	 * @param 		firmwareVersion		The firmware version of the module.
	 * @param 		switchThreshold		The switch threshold.
	 * @param		tcpClient			The TCP client.
	 */
	DigitalModuleConfigurationImpl(final int moduleId, final int numberOfChannels, final String firmwareVersion, final int switchThreshold, final TCPClient tcpClient) {
		this.moduleId = moduleId;
		this.numberOfChannels = numberOfChannels;
		this.firmwareVersion = firmwareVersion;
		this.switchThreshold = switchThreshold;
		this.client = tcpClient;
		
		this.inputChannelConfigurations = new DigitalInputChannelConfiguration[numberOfChannels];
		
		for (int i = 0; i < numberOfChannels; i++) {
			this.inputChannelConfigurations[i] = new DigitalModuleInputChannelConfigurationImpl(this.client, this, i, moduleId);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DigitalInputChannelConfiguration getDigitalChannelConfiguration(final int channelNumber) {
		return this.inputChannelConfigurations[channelNumber];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getFirmwareVersion() throws IOException {
		return this.firmwareVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getModuleId() throws IOException {
		return this.moduleId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getNumberOfChannels() throws IOException {
		return this.numberOfChannels;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getSwitchThreshold() throws IOException {
		return this.switchThreshold;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void save() throws IOException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "TCP Driver : Digital Module Configuration : Save");
		}
		
		for (int channelNumber = 0; channelNumber < this.inputChannelConfigurations.length; channelNumber++) {
			final DigitalInputChannelConfiguration configuration = this.inputChannelConfigurations[channelNumber];
			this.client.setDigitalInputChannelConfiguration(configuration);
		}
		
		this.client.setDigitalModuleConfiguration(this);
		this.client.saveModuleConfiguration(this.getModuleId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setModuleId(final int moduleId) throws IOException {
		this.moduleId = moduleId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setSwitchThreshold(final int switchThreshold) throws IOException {
		this.switchThreshold = switchThreshold;
	}

}
