package be.abollaert.domotics.light.server.kernel;


import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalChannelStateChangeListener;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DigitalModuleConfiguration;
import be.abollaert.domotics.light.api.DigitalModuleConfigurationChangedListener;
import be.abollaert.domotics.light.api.DimmerChannelStateChangeListener;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.DimmerModuleConfiguration;
import be.abollaert.domotics.light.api.DimmerModuleConfigurationChangedListener;
import be.abollaert.domotics.light.api.SwitchEvent;
import be.abollaert.domotics.light.driver.base.Channel;
import be.abollaert.domotics.light.driver.base.ChannelType;
import be.abollaert.domotics.light.driver.base.CommunicationChannelEventListener;
import be.abollaert.domotics.light.driver.base.RequestPDU;
import be.abollaert.domotics.light.driver.base.ResponsePDU;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;


/**
 * Digital module implementation.
 * 
 * @author alex
 *
 */
final class ModuleImpl implements DigitalModule, DimmerModule, CommunicationChannelEventListener {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(ModuleImpl.class.getName());
	
	/** The serial channel. */
	private final Channel channel;
	
	/** The configuration. */
	private final ModuleConfigurationImpl configuration;
	
	/** {@link DigitalChannelStateChangeListener}s who are attached to this module. */
	private final Set<DigitalChannelStateChangeListener> digitalStateListeners = Collections.newSetFromMap(new WeakHashMap<DigitalChannelStateChangeListener, Boolean>());
	
	/** {@link DimmerChannelStateChangeListener}s who are attached to this module. */
	private final Set<DimmerChannelStateChangeListener> dimmerStateListeners = Collections.newSetFromMap(new WeakHashMap<DimmerChannelStateChangeListener, Boolean>());
		
	/** Digital module configuration listeners. */
	private final Set<DigitalModuleConfigurationChangedListener> digitalConfigurationChangedListeners = Collections.newSetFromMap(new WeakHashMap<DigitalModuleConfigurationChangedListener, Boolean>());
	
	/** Dimmer module configuration listeners. */
	private final Set<DimmerModuleConfigurationChangedListener> dimmerConfigurationChangedListeners = Collections.newSetFromMap(new WeakHashMap<DimmerModuleConfigurationChangedListener, Boolean>());
	
	/** The channel type. */
	private final ChannelType channelType;
	
	/** The module ID. */
	private int moduleId = -1;
	
	/** The storage. */
	private final Storage storage;
	
	/** The output channel states. */
	private final ChannelState[] outputChannelStates;
	
	/**
	 * Create a new module using the given serial channel.
	 * 
	 * @param 	serialChannel	The serial channel.
	 */
	public ModuleImpl(final Channel serialChannel, final ChannelType channelType, final Storage storage) throws IOException {
		this.channel = serialChannel;
		this.channel.addEventListener(this);
		this.configuration = new ModuleConfigurationImpl(serialChannel, storage);
		this.channelType = channelType;
		this.storage = storage;
		
		this.outputChannelStates = new ChannelState[ this.configuration.getNumberOfChannels() ];
		
		for (int channelNumber = 0; channelNumber < this.outputChannelStates.length; channelNumber++) {
			this.outputChannelStates[channelNumber] = this.getOutputChannelState(channelNumber);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DigitalModuleConfiguration getDigitalConfiguration() {
		return this.configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getId() {
		if (this.moduleId == -1) {
			try {
				this.moduleId = this.configuration.getModuleId();
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not get module ID from configuration : [" + e.getMessage() + "]", e);
				}
			}
		}
		
		return this.moduleId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ChannelState getOutputChannelState(int channelNumber) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.GET_OUTPUT_STATE, new int[] { channelNumber });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.OK) {
				if (responsePDU.getArguments().length >= 1) {
					if (responsePDU.getArguments()[0] == 0) {
						return ChannelState.OFF;
					} else if (responsePDU.getArguments()[0] == 1) {
						return ChannelState.ON;
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void switchOutputChannel(final int channelNumber, final ChannelState desiredState) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.SWITCH_OUTPUT, new int[] { channelNumber, desiredState.getState() });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.ERROR) {
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addChannelStateListener(final DigitalChannelStateChangeListener listener) {
		this.digitalStateListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeChannelStateListener(final DigitalChannelStateChangeListener listener) {
		this.digitalStateListeners.remove(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void eventReceived(final ResponsePDU responsePDU) {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Event received of type [" + responsePDU.getType().name() + "]");
		}
		
		switch (responsePDU.getType()) {
			case INPUT_CHANNEL_STATE_CHANGE: {
				if (responsePDU.getArguments().length == 2) {
					final int channelNumber = responsePDU.getArguments()[0];
					final int newState = responsePDU.getArguments()[1];
					
					if (this.channelType == ChannelType.DIGITAL) {
						for (final DigitalChannelStateChangeListener listener : this.digitalStateListeners) {
							listener.inputChannelStateChanged(channelNumber, (newState == 0 ? ChannelState.OFF : ChannelState.ON));
						}
					} else if (this.channelType == ChannelType.DIMMER) {
						for (final DimmerChannelStateChangeListener listener : this.dimmerStateListeners) {
							listener.inputChannelStateChanged(channelNumber, (newState == 0 ? ChannelState.OFF : ChannelState.ON));
						}
					}
				}
				
				break;
			}
			
			case OUTPUT_CHANNEL_STATE_CHANGE: {
				if (responsePDU.getArguments().length >= 2) {
					final int channelNumber = responsePDU.getArguments()[0];
					final ChannelState newState = responsePDU.getArguments()[1] == 1 ? ChannelState.ON : ChannelState.OFF;
					
					if (responsePDU.getArguments().length == 3) {
						if (this.channelType == ChannelType.DIMMER) {
							final int percentage = responsePDU.getArguments()[2];
							
							for (final DimmerChannelStateChangeListener listener : this.dimmerStateListeners) {
								listener.outputChannelStateChanged(channelNumber, newState, percentage);
							}
							
							if (this.getDimmerConfiguration().getDimmerChannelConfiguration(channelNumber).isLoggingEnabled()) {
								if (newState != this.outputChannelStates[channelNumber]) {
									this.storage.logOnOffEvent(this.getId(), channelNumber, newState == ChannelState.ON);
									this.outputChannelStates[channelNumber] = newState;
								} else {
									this.storage.logDimEvent(this.getId(), channelNumber, percentage);
								}
							}
						}
					} else if (this.channelType == ChannelType.DIGITAL) {
						for (final DigitalChannelStateChangeListener listener : this.digitalStateListeners) {
							listener.outputChannelStateChanged(channelNumber, newState);
						}
						
						if (this.getDimmerConfiguration().getDimmerChannelConfiguration(channelNumber).isLoggingEnabled()) {
							this.storage.logOnOffEvent(this.getId(), channelNumber, newState == ChannelState.ON);
						}
					}
				}
				
				break;
			}
			
			case CONFIGURATION_CHANGED: {
				if (this.channelType == ChannelType.DIGITAL) {
					for (final DigitalModuleConfigurationChangedListener listener : this.digitalConfigurationChangedListeners) {
						listener.digitalModuleConfigurationChanged(this.moduleId);
					}
				 } else if (this.channelType == ChannelType.DIMMER){
						for (final DimmerModuleConfigurationChangedListener listener : this.dimmerConfigurationChangedListeners) {
							listener.dimmerModuleConfigurationChanged(this.moduleId);
						}
				 }
				
				break;
			}
		}
	}

	@Override
	public final ChannelState getInputChannelState(final int channelNumber) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.GET_INPUT_STATE, new int[] { channelNumber });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.OK) {
				if (responsePDU.getArguments().length == 1) {
					if (responsePDU.getArguments()[0] == 0) {
						return ChannelState.OFF;
					} else if (responsePDU.getArguments()[0] == 1) {
						return ChannelState.ON;
					}
				}
			}
		}
		
		return null;
	}

	@Override
	public final void addChannelStateListener(final DimmerChannelStateChangeListener listener) {
		this.dimmerStateListeners.add(listener);
	}

	@Override
	public final void dim(final int channelNumber, final short desiredPercentage) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.DIM, new int[] { channelNumber, desiredPercentage });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.ERROR) {
			}
		}
	}

	@Override
	public final void removeChannelStateListener(final DimmerChannelStateChangeListener listener) {
		this.dimmerStateListeners.remove(listener);
	}

	@Override
	public final DimmerModuleConfiguration getDimmerConfiguration() {
		return this.configuration;
	}

	@Override
	public final int getDimmerPercentage(int channelNumber) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.GET_OUTPUT_STATE, new int[] { channelNumber });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.OK) {
				if (responsePDU.getArguments().length == 2) {
					return responsePDU.getArguments()[1];
				}
			}
		}
		
		return -1;
	}

	@Override
	public final String getPortName() {
		return this.channel.getName();
	}

	@Override
	public final void upgrade(final InputStream hexFileStream) throws UnsupportedOperationException, IOException {
		if (this.channel.supportsUpgrade()) {
			this.channel.upgrade(this.getId(), hexFileStream);
		} else {
			throw new UnsupportedOperationException("Underlying channel does not support upgrading.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addModuleConfigurationListener(final DigitalModuleConfigurationChangedListener listener) {
		this.digitalConfigurationChangedListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeModuleConfigurationListener(final DigitalModuleConfigurationChangedListener listener) {
		this.digitalConfigurationChangedListeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addModuleConfigurationListener(final DimmerModuleConfigurationChangedListener listener) {
		this.dimmerConfigurationChangedListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeModuleConfigurationListener(final DimmerModuleConfigurationChangedListener listener) {
		this.dimmerConfigurationChangedListeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<SwitchEvent> getSwitchEvents(final int channelNumber, final Date startDate, final Date endDate) {
		return this.storage.getSwitchEventsForPeriod(this.getId(), channelNumber, startDate, endDate);
	}
}
