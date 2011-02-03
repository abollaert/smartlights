package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalChannelStateChangeListener;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DigitalModuleConfiguration;
import be.abollaert.domotics.light.api.DigitalModuleConfigurationChangedListener;
import be.abollaert.domotics.light.protocolbuffers.Api;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalInputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalModuleConfigurationChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalOutputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DimmerInputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DimmerModuleConfigurationChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DimmerOutputChannelStateChanged;

/**
 * Digital module implementation.
 * 
 * @author alex
 */
final class DigitalModuleImpl extends AbstractModule implements DigitalModule, MulticastEventListener {

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(DigitalModuleImpl.class.getName());
	
	/** The configuration. */
	private final DigitalModuleConfiguration configuration;
	
	/** THe channel state listeners. */
	private final Set<DigitalChannelStateChangeListener> listeners = new HashSet<DigitalChannelStateChangeListener>();
	
	/** The digital config change listeners. */
	private final Set<DigitalModuleConfigurationChangedListener> digitalModuleConfigChangeListeners = new HashSet<DigitalModuleConfigurationChangedListener>();
	
	/**
	 * Create a new instance. 
	 * 
	 * @param 	id						The ID.
	 * @param 	numberOfChannels		The number of channels.
	 * @param	firmwareVersion			The firmware version.
	 * @param	switchThreshold			The switch threshold.
	 * @param	tcpClient				The TCP client.
	 */
	DigitalModuleImpl(final int id, final int numberOfChannels, final String firmwareVersion, final int switchThreshold, final TCPClient tcpClient) {
		super(id, tcpClient);
		
		this.configuration = new DigitalModuleConfigurationImpl(id, numberOfChannels, firmwareVersion, switchThreshold, tcpClient);
	}
	
	@Override
	public final void addChannelStateListener(final DigitalChannelStateChangeListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DigitalModuleConfiguration getDigitalConfiguration() {
		return this.configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getId() {
		try {
			return this.configuration.getModuleId();
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "IO error while getting the ID of this module [" + e.getMessage() + "]", e);
			}
			
			return -1;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ChannelState getInputChannelState(final int channelNumber) throws IOException {
		return ChannelState.OFF;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ChannelState getOutputChannelState(final int channelNumber) throws IOException {
		return this.getTCPClient().getDigitalChannelOutputState(this.getId(), channelNumber).getState() ? ChannelState.ON : ChannelState.OFF;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getPortName() {
		return "/dev/blabla";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeChannelStateListener(final DigitalChannelStateChangeListener listener) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void switchOutputChannel(final int channelNumber, final ChannelState desiredState) throws IOException {
		this.getTCPClient().switchOutput(this.getId(), channelNumber, desiredState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void digitalInputChannelStateChanged(final DigitalInputChannelStateChanged event) {
		if (event.getModuleId() == this.getId()) {
			for (final DigitalChannelStateChangeListener listener : this.listeners) {
				listener.inputChannelStateChanged(event.getChannelNumber(), event.getNewState() ? ChannelState.ON : ChannelState.OFF);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void digitalOutputChannelStateChanged(final DigitalOutputChannelStateChanged event) {
		if (event.getModuleId() == this.getId()) {
			for (final DigitalChannelStateChangeListener listener : this.listeners) {
				listener.outputChannelStateChanged(event.getChannelNumber(), event.getNewState() ? ChannelState.ON : ChannelState.OFF);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addModuleConfigurationListener(final DigitalModuleConfigurationChangedListener listener) {
		this.digitalModuleConfigChangeListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeModuleConfigurationListener(final DigitalModuleConfigurationChangedListener listener) {
		this.digitalModuleConfigChangeListeners.remove(listener);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void digitalModuleConfigurationChanged(final DigitalModuleConfigurationChanged event) {
		if (event.getModuleId() == this.getId()) {
			try {
				final Api.DigitalModuleConfig newConfig = this.getTCPClient().getDigitalModuleConfig(this.getId());
				
				this.getDigitalConfiguration().setSwitchThreshold(newConfig.getSwitchThresholdInMs());
				
				for (final DigitalModuleConfigurationChangedListener listener : this.digitalModuleConfigChangeListeners) {
					listener.digitalModuleConfigurationChanged(this.getId());
				}
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not process config change event. [" + e.getMessage() + "]", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void dimmerModuleConfigurationChanged(final DimmerModuleConfigurationChanged event) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void dimmerInputStateChanged(final DimmerInputChannelStateChanged event) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dimmerOutputStateChanged(DimmerOutputChannelStateChanged event) {
	}
}
