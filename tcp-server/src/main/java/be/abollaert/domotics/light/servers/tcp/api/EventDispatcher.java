package be.abollaert.domotics.light.servers.tcp.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalChannelStateChangeListener;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DigitalModuleConfigurationChangedListener;
import be.abollaert.domotics.light.api.DimmerChannelStateChangeListener;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.DimmerModuleConfigurationChangedListener;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.protocolbuffers.Eventing;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalInputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalOutputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalInputChannelStateChanged.Builder;

/**
 * Dispatches events received from the modules. It sends them to a multicast socket at port {@link #MULTICAST_PORT}.
 * 
 * @author alex
 */
final class EventDispatcher {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(EventDispatcher.class.getName());

	/** The port used to send the events through. */
	private static final int MULTICAST_PORT = 5894;
	
	/** The address we will be sending messages to. This is the group. */
	private static final String MULTICAST_ADDRESS = "224.0.0.100";
	
	/** The socket. */
	private MulticastSocket socket;
	
	/** The group address. */
	private InetAddress groupAddress;
	
	/** The {@link Set} of {@link DigitalModule}s we are listening to. */
	private final Set<DigitalModuleStateListener> digitalModuleStateListeners = new HashSet<DigitalModuleStateListener>();
	
	/** The {@link Set} of {@link DimmerModule}s we are listening to. */
	private final Set<DimmerModuleStateListener>  dimmerModuleStateListeners = new HashSet<DimmerModuleStateListener>();
	
	/** 
	 * Start the dispatcher. 
	 *
	 * @throws	IOException		If an IO error occurs during the start.
	 */
	void start(final Driver driver) throws IOException {
		// Open the socket first, then add ourselves as a listener.
		this.openMulticastSocket();
		
		for (final DigitalModule digitalModule : driver.getAllDigitalModules()) {
			final DigitalModuleStateListener stateListener = new DigitalModuleStateListener(digitalModule);
			this.digitalModuleStateListeners.add(stateListener);
		}
		
		for (final DimmerModule dimmerModule : driver.getAllDimmerModules()) {
			final DimmerModuleStateListener listener = new DimmerModuleStateListener(dimmerModule);
			this.dimmerModuleStateListeners.add(listener);
		}
	}
	
	/**
	 * Stop the dispatcher.
	 * 
	 * @throws 	IOException		If an IO error occurs during the start.
	 */
	void stop() throws IOException {
		if (this.socket == null) {
			throw new IllegalStateException("Do not have a multicast socket to send messages to. Have you started the dispatcher?");
		}
		
		for (final DigitalModuleStateListener digitalModuleStateListener : this.digitalModuleStateListeners) {
			digitalModuleStateListener.stop();
		}
		
		for (final DimmerModuleStateListener listener : this.dimmerModuleStateListeners) {
			listener.stop();
		}
		
		this.digitalModuleStateListeners.clear();
		this.dimmerModuleStateListeners.clear();
		
		this.socket.leaveGroup(this.groupAddress);
		this.socket.close();
		
		this.socket = null;
		this.groupAddress = null;
	}
	/**
	 * Opens the multicast socket.
	 * 
	 * @throws	IOException		If an IO error occurs during the open.
	 */
	private final void openMulticastSocket() throws IOException {
		this.groupAddress = InetAddress.getByName(MULTICAST_ADDRESS);
		this.socket = new MulticastSocket(MULTICAST_PORT);
		this.socket.joinGroup(this.groupAddress);
		this.socket.setBroadcast(true);
	}
	
	/**
	 * Sends the given message.
	 * 
	 * @param 	message		The message to send.
	 * 
	 * @throws	IOException	If an IO error occurs while sending the message.
	 */
	private final void sendMessage(final byte[] message) throws IOException {
		if (this.socket == null) {
			throw new IllegalStateException("Do not have a multicast socket to send messages to. Have you started the dispatcher?");
		}
		
		final DatagramPacket packet = new DatagramPacket(message, message.length, this.groupAddress, MULTICAST_PORT);
		this.socket.send(packet);
	}
	
	/**
	 * State listener for a {@link DigitalModule}.
	 * 
	 * @author alex
	 */
	private final class DigitalModuleStateListener implements DigitalChannelStateChangeListener, DigitalModuleConfigurationChangedListener {
		
		/** The {@link DigitalModule} we are listening to. */
		private final DigitalModule module;
		
		/**
		 * Create a new instance specifying the module.
		 * 
		 * @param 	module		The module.
		 */
		private DigitalModuleStateListener(final DigitalModule module) {
			this.module = module;
			this.module.addChannelStateListener(this);
			this.module.addModuleConfigurationListener(this);
		}
		
		/**
		 * Stop listening.
		 */
		private final void stop() {
			this.module.removeChannelStateListener(this);
			this.module.removeModuleConfigurationListener(this);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void inputChannelStateChanged(final int channelNumber, final ChannelState newState) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Sending input state change event for module [" + this.module.getId() + "], channel [" + channelNumber + "], state [" + newState + "]");
			}
			
			final Builder stateChangebuilder = DigitalInputChannelStateChanged.newBuilder();
			ByteArrayOutputStream outputStream = null;
			
			try {
				stateChangebuilder.setModuleId(this.module.getId());
				stateChangebuilder.setChannelNumber(channelNumber);
				stateChangebuilder.setNewState(newState == ChannelState.ON ? true : false);
				
				final be.abollaert.domotics.light.protocolbuffers.Eventing.Event.Builder eventBuilder = Eventing.Event.newBuilder();
				eventBuilder.setDigitalInputChannelStateChangedEvent(stateChangebuilder.build());
				eventBuilder.setType(Eventing.Event.Type.DIGITAL_INPUT_CHANNEL_STATE_CHANGED);
				
				outputStream = new ByteArrayOutputStream();
				eventBuilder.build().writeTo(outputStream);
				
				sendMessage(outputStream.toByteArray());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "IO error while generating digital input channel state event : [" + e.getMessage() + "]", e);
				}
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						if (logger.isLoggable(Level.WARNING)) {
							logger.log(Level.WARNING, "IO error while closing output stream : [" + e.getMessage() + "]", e);
						}
					}
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void outputChannelStateChanged(final int channelNumber, final ChannelState newState) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Sending output state change event for module [" + this.module.getId() + "], channel [" + channelNumber + "], state [" + newState + "]");
			}
			
			final be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalOutputChannelStateChanged.Builder stateChangebuilder = DigitalOutputChannelStateChanged.newBuilder();
			ByteArrayOutputStream outputStream = null;
			
			try {
				stateChangebuilder.setModuleId(this.module.getId());
				stateChangebuilder.setChannelNumber(channelNumber);
				stateChangebuilder.setNewState(newState == ChannelState.ON ? true : false);
			
				final be.abollaert.domotics.light.protocolbuffers.Eventing.Event.Builder eventBuilder = Eventing.Event.newBuilder();
				eventBuilder.setDigitalOutputChannelStateChangedEvent(stateChangebuilder);
				
				eventBuilder.setType(Eventing.Event.Type.DIGITAL_OUTPUT_CHANNEL_STATE_CHANGED);
				
				outputStream = new ByteArrayOutputStream();
				eventBuilder.build().writeTo(outputStream);
				
				sendMessage(outputStream.toByteArray());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "IO error while generating digital output channel state event : [" + e.getMessage() + "]", e);
				}
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						if (logger.isLoggable(Level.WARNING)) {
							logger.log(Level.WARNING, "IO error while closing output stream : [" + e.getMessage() + "]", e);
						}
					}
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void digitalModuleConfigurationChanged(int moduleId) {
			final Eventing.DigitalModuleConfigurationChanged.Builder eventBuilder = Eventing.DigitalModuleConfigurationChanged.newBuilder();
			eventBuilder.setModuleId(moduleId);
			
			try {
				final Eventing.Event.Builder builder = Eventing.Event.newBuilder();
				builder.setType(Eventing.Event.Type.DIGITAL_MODULE_CONFIG_CHANGE);
				builder.setDigitalModuleConfigChanged(eventBuilder);
				
				sendMessage(builder.build().toByteArray());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "IO error while sending event (digital configuration change) : [" + e.getMessage() + "]", e);
				}
			}
		}
	}
	
	/**
	 * Listener that listens for dimmer state changes.
	 * 
	 * @author alex
	 */
	private final class DimmerModuleStateListener implements DimmerChannelStateChangeListener, DimmerModuleConfigurationChangedListener {

		/** The module we are listening on. */
		private final DimmerModule module;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	module		The mpdule.
		 */
		private DimmerModuleStateListener(final DimmerModule module) {
			this.module = module;
			this.module.addChannelStateListener(this);
			this.module.addModuleConfigurationListener(this);
		}
		
		/**
		 * Stop the state listener.
		 */
		final void stop() {
			this.module.removeChannelStateListener(this);
			this.module.removeModuleConfigurationListener(this);
		}
		
		@Override
		public final void inputChannelStateChanged(final int channelNumber, final ChannelState newState) {
			final Eventing.DimmerInputChannelStateChanged.Builder eventBuilder = Eventing.DimmerInputChannelStateChanged.newBuilder();
			eventBuilder.setModuleId(this.module.getId());
			eventBuilder.setNewState(newState == ChannelState.ON? true : false);
			eventBuilder.setChannelNumber(channelNumber);
			
			final Eventing.Event.Builder messageBuilder = Eventing.Event.newBuilder();
			messageBuilder.setType(Eventing.Event.Type.DIMMER_MODULE_INPUT_CHANNEL_STATE_CHANGED);
			messageBuilder.setDimmerInputStateChanged(eventBuilder);
			
			try {
				sendMessage(messageBuilder.build().toByteArray());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not send dimmer input channel state change event due to an IO error [" + e.getMessage() + "]", e);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void outputChannelStateChanged(final int channelNumber, final ChannelState newState, final int percentage) {
			final Eventing.DimmerOutputChannelStateChanged.Builder eventBuilder = Eventing.DimmerOutputChannelStateChanged.newBuilder();
			eventBuilder.setModuleId(this.module.getId());
			eventBuilder.setChannelNumber(channelNumber);
			eventBuilder.setDimmerPercentage(percentage);
			eventBuilder.setOn(newState == ChannelState.ON? true : false);
			
			final Eventing.Event.Builder messageBuilder = Eventing.Event.newBuilder();
			messageBuilder.setType(Eventing.Event.Type.DIMMER_MODULE_OUTPUT_CHANNEL_STATE_CHANGED);
			messageBuilder.setDimmerOutputStateChanged(eventBuilder);
			
			try {
				sendMessage(messageBuilder.build().toByteArray());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not send dimmer output channel state change event due to an IO error [" + e.getMessage() + "]", e);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void dimmerModuleConfigurationChanged(final int moduleId) {
			final Eventing.DimmerModuleConfigurationChanged.Builder eventBuilder = Eventing.DimmerModuleConfigurationChanged.newBuilder();
			eventBuilder.setModuleId(this.module.getId());
			
			final Eventing.Event.Builder messageBuilder = Eventing.Event.newBuilder();
			messageBuilder.setType(Eventing.Event.Type.DIMMER_MODULE_CONFIG_CHANGE);
			messageBuilder.setDimmerModuleConfigChanged(eventBuilder);
			
			try {
				sendMessage(messageBuilder.build().toByteArray());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not send dimmer configuration change event due to an IO error [" + e.getMessage() + "]", e);
				}
			}
		}
		
	}
}
