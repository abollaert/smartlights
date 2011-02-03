package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;

import be.abollaert.domotics.light.api.DimmerInputChannelConfiguration;
import be.abollaert.domotics.light.api.DimmerModuleConfiguration;

/**
 * Dimmer module configuration for the client side.
 * 
 * @author alex
 */
final class DimmerModuleConfigurationImpl implements DimmerModuleConfiguration {

	/** The ID of the module. */
	private final int moduleId;
	
	/** The number of channels in the module. */
	private final int numberOfChannels;
	
	/** The firmware version. */
	private final String firmwareVersion;
	
	/** The switch threshold. */
	private int switchThreshold;
	
	/** The delay before the dimmer steps. */
	private int dimmerDelay;
	
	/** The threshold after which the dimmer function is applied. */
	private int dimmerThreshold;
	
	/** Input channel configuration. */
	private final DimmerInputChannelConfiguration[] inputChannelConfigurations;
	
	/** The TCP client. */
	private final TCPClient tcpClient;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	moduleId				The module ID.
	 * @param 	numberOfChannels		The number of channels.
	 * @param 	firmwareVersion			The firmware version.
	 */
	DimmerModuleConfigurationImpl(final TCPClient tcpClient, final DimmerModuleImpl dimmerModule, final int moduleId, final int numberOfChannels, final String firmwareVersion) {
		this.moduleId = moduleId;
		this.numberOfChannels = numberOfChannels;
		this.firmwareVersion = firmwareVersion;
		this.inputChannelConfigurations = new DimmerInputChannelConfiguration[this.numberOfChannels];
		this.tcpClient = tcpClient;
		
		for (int i = 0; i < this.numberOfChannels; i++) {
			this.inputChannelConfigurations[i] = new DimmerInputChannelConfigurationImpl(tcpClient, dimmerModule, this.moduleId, i);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DimmerInputChannelConfiguration getDimmerChannelConfiguration(final int channelNumber) {
		return this.inputChannelConfigurations[channelNumber];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getDimmerDelay() throws IOException {
		return this.dimmerDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getDimmerThreshold() throws IOException {
		return this.dimmerThreshold;
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
		this.tcpClient.setDimmerModuleConfiguration(this);
		
		for (int i = 0; i < this.numberOfChannels; i++) {
			this.tcpClient.setDimmerInputConfiguration(this.getModuleId(), i, this.inputChannelConfigurations[i]);
		}
		
		this.tcpClient.saveModuleConfiguration(this.moduleId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setDimmerDelay(final int dimmerThreshold) throws IOException {
		this.dimmerDelay = dimmerThreshold;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setDimmerThreshold(final int dimmerThreshold) throws IOException {
		this.dimmerThreshold = dimmerThreshold;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setSwitchThreshold(final int switchThreshold) throws IOException {
		this.switchThreshold = switchThreshold;
	}
}
