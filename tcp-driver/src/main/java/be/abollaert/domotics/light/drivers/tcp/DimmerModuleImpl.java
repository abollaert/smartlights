package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DimmerChannelStateChangeListener;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.DimmerModuleConfiguration;
import be.abollaert.domotics.light.api.DimmerModuleConfigurationChangedListener;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalInputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalModuleConfigurationChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalOutputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DimmerInputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DimmerModuleConfigurationChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DimmerOutputChannelStateChanged;

/**
 * Client side implementation of a dimmer module.
 * 
 * @author alex
 */
final class DimmerModuleImpl extends AbstractModule implements DimmerModule, MulticastEventListener {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(DimmerModuleImpl.class.getName());
	
	/** The configuration. */
	private final DimmerModuleConfiguration configuration;
	
	/** The channel states. */
	private final ChannelState[] inputChannelStates;
	
	/** The output channel states. */
	private final ChannelState[] outputChannelStates;
	
	/** The dimmer percentages. */
	private final int[] dimmerPercentages;
	
	/** Channel state listeners. */
	private final Set<DimmerChannelStateChangeListener> channelStateListeners = new HashSet<DimmerChannelStateChangeListener>();
	
	/** Listeners for configuration changes. */
	private final Set<DimmerModuleConfigurationChangedListener> configurationChangeListeners = new HashSet<DimmerModuleConfigurationChangedListener>();
	
	DimmerModuleImpl(final TCPClient tcpClient, final int moduleId, final int numberOfChannels, final String firmwareVersion, final int switchThreshold, final int dimmerDelay, final int dimmerThreshold) {
		super(moduleId, tcpClient);
		
		this.inputChannelStates = new ChannelState[numberOfChannels];
		this.outputChannelStates = new ChannelState[numberOfChannels];
		this.dimmerPercentages = new int[numberOfChannels];
		
		this.configuration = new DimmerModuleConfigurationImpl(tcpClient, this, moduleId, numberOfChannels, firmwareVersion);
		
		try {
			this.configuration.setSwitchThreshold(switchThreshold);
			this.configuration.setDimmerDelay(dimmerDelay);
			this.configuration.setDimmerThreshold(dimmerThreshold);
		} catch (IOException e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, "Could not set configuration for the dimmer configuration due to an IO error [" + e.getMessage() + "]", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addChannelStateListener(final DimmerChannelStateChangeListener listener) {
		this.channelStateListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addModuleConfigurationListener(final DimmerModuleConfigurationChangedListener listener) {
		this.configurationChangeListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void dim(final int channelNumber, final short desiredPercentage) throws IOException {
		this.getTCPClient().dim(this.getId(), channelNumber, desiredPercentage);
	}

	@Override
	public final DimmerModuleConfiguration getDimmerConfiguration() {
		return this.configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getDimmerPercentage(final int channelNumber) throws IOException {
		this.dimmerPercentages[channelNumber] = this.getTCPClient().getDimmerChannelOutputState(this.getId(), channelNumber).getPercentage();
		
		return this.dimmerPercentages[channelNumber];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getId() {
		try {
			return this.configuration.getModuleId();
		} catch (IOException e) {
			return -1;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ChannelState getInputChannelState(int channelNumber) throws IOException {
		return this.inputChannelStates[channelNumber];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChannelState getOutputChannelState(int channelNumber) throws IOException {
		this.outputChannelStates[channelNumber] = this.getTCPClient().getDimmerChannelOutputState(this.getId(), channelNumber).getState() ? ChannelState.ON : ChannelState.OFF;
		
		return this.outputChannelStates[channelNumber];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getPortName() {
		return "TCP";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeChannelStateListener(final DimmerChannelStateChangeListener listener) {
		this.channelStateListeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeModuleConfigurationListener(final DimmerModuleConfigurationChangedListener listener) {
		this.configurationChangeListeners.remove(listener);
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
	public final void upgrade(final InputStream hexFileStream) throws UnsupportedOperationException, IOException {
		
	}
	
	/**
	 * Set the input channel state of said channel.
	 * 
	 * @param 	channelNumber		The channel number.
	 * @param 	state				The new state.
	 */
	final void setInputChannelState(final int channelNumber, final ChannelState state) {
		this.inputChannelStates[channelNumber] = state;
	}
	
	/**
	 * Set the output channel state.
	 * 
	 * @param 	channelNumber		The channel number.
	 * @param 	state				The new state.
	 */
	final void setOutputChannelState(final int channelNumber, final ChannelState state) {
		this.outputChannelStates[channelNumber] = state;
	}
	
	/**
	 * Sets the dimmer percentage.
	 * 
	 * @param 	channelNumber		The channel number.
	 * @param 	percentage			The percentage.
	 */
	final void setDimmerPercentage(final int channelNumber, final int percentage) {
		this.dimmerPercentages[channelNumber] = percentage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void digitalInputChannelStateChanged(final DigitalInputChannelStateChanged event) {	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void digitalModuleConfigurationChanged(final DigitalModuleConfigurationChanged event) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void digitalOutputChannelStateChanged(final DigitalOutputChannelStateChanged event) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void dimmerModuleConfigurationChanged(final DimmerModuleConfigurationChanged event) {
		if (event.getModuleId() == this.getId()) {
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void dimmerInputStateChanged(final DimmerInputChannelStateChanged event) {
		if (event.getModuleId() == this.getId()) {
			this.inputChannelStates[event.getChannelNumber()] = event.getNewState()? ChannelState.ON : ChannelState.OFF;
			
			for (final DimmerChannelStateChangeListener listener : this.channelStateListeners) {
				listener.inputChannelStateChanged(event.getChannelNumber(), this.inputChannelStates[event.getChannelNumber()]);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void dimmerOutputStateChanged(final DimmerOutputChannelStateChanged event) {
		if (event.getModuleId() == this.getId()) {
			this.outputChannelStates[event.getChannelNumber()] = event.getOn()? ChannelState.ON : ChannelState.OFF;
			this.dimmerPercentages[event.getChannelNumber()] = event.getDimmerPercentage();
			
			for (final DimmerChannelStateChangeListener listener : this.channelStateListeners) {
				listener.outputChannelStateChanged(event.getChannelNumber(), this.outputChannelStates[event.getChannelNumber()], this.dimmerPercentages[event.getChannelNumber()]);
			}
		}
	}
}
