package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalInputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalOutputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DimmerInputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DimmerOutputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.Event;

/**
 * The event listener listens for events on the multicast group.
 * 
 * @author alex
 *
 */
public final class EventListener {

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(EventListener.class.getName());
	
	/** We use a buffer of 4K for the messages. */
	private static final int BUFFER_SIZE = 4096;

	/** The port used to send the events through. */
	private static final int MULTICAST_PORT = 5894;
	
	/** The address we will be sending messages to. This is the group. */
	private static final String MULTICAST_ADDRESS = "224.0.0.100";
	
	/** The event listener thread. */
	private Thread eventListenerThread;
	
	/** The socket used. */
	private MulticastSocket socket;
	
	/** The group address. */
	private InetAddress groupAddress;
	
	/** The {@link ExecutorService} that's used to dispatch the messages. */
	private ExecutorService messageDispatcherService;
	
	/** The map of event listeners. */
	private final Set<MulticastEventListener> eventListeners = new HashSet<MulticastEventListener>();
	
	/**
	 * Starts the event listener.
	 * 
	 * @throws 	IOException		If an IO error occurs when starting.
	 */
	void start(final InetAddress address) throws IOException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Starting event listener.");
		}
		
		this.groupAddress = InetAddress.getByName(MULTICAST_ADDRESS);
		this.socket = new MulticastSocket(MULTICAST_PORT);
		this.socket.joinGroup(this.groupAddress);
		
		this.eventListenerThread = new Thread(new EventListenerThread(this.socket), "[TCP Driver] Multicast event listener");
		this.eventListenerThread.start();
		
		this.messageDispatcherService = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public final Thread newThread(final Runnable r) {
				return new Thread(r, "[TCP Driver] Message dispatcher service");
			}
		});
	
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Event listener started.");
		}
	}
	
	/**
	 * Stop the event listener.
	 * 
	 * @throws 	IOException		If an IO error occurs during the stop.
	 */
	void stop() throws IOException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Stopping event listener.");
		}
		
		this.eventListenerThread.interrupt();
		this.socket.close();
		
		this.groupAddress = null;
		this.socket = null;
		this.eventListenerThread = null;
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Event listener stopped.");
		}
	}
	
	/**
	 * Event listener thread.
	 * 
	 * @author alex
	 */
	private final class EventListenerThread implements Runnable {
		
		/** The buffer used for the datagrams. */
		private final byte[] buffer = new byte[BUFFER_SIZE];
		
		/** The socket. */
		private final DatagramSocket socket;
		
		/**
		 * Create a new instance specifying the socket.
		 * 
		 * @param 	socket		The socket to use.
		 */
		private EventListenerThread(final DatagramSocket socket) {
			this.socket = socket;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public final void run() {
			while (true && !Thread.interrupted()) {
				final DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length, groupAddress, MULTICAST_PORT);
				
				try {
					this.socket.receive(packet);
					
					messageDispatcherService.execute(new Runnable() {
						public final void run() {
							try {
								handleMessage(packet);
							} catch (IOException e) {
								if (logger.isLoggable(Level.WARNING)) {
									logger.log(Level.WARNING, "IO error while handling message, message is lost. [" + e.getMessage() + "]", e);
								}
							}
						}
					});
				} catch (IOException e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Could not receive datagram due to an IO error [" + e.getMessage() + "]");
					}
				}
			}
			
			// Cleanup.
		}
	}
	
	/**
	 * Handles the message in the {@link DatagramPacket}.
	 * 
	 * @param 		packet			The packet to handle.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	private final void handleMessage(final DatagramPacket packet) throws IOException {
		final byte[] message = new byte[ packet.getLength() ];
		System.arraycopy(packet.getData(), 0, message, 0, packet.getLength());
		
		final Event event = Event.parseFrom(message);
		
		switch (event.getType()) {
			case DIGITAL_INPUT_CHANNEL_STATE_CHANGED: {
				final DigitalInputChannelStateChanged stateChange = event.getDigitalInputChannelStateChangedEvent();
				
				if (stateChange != null) {
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Received a digital input channel state change : module [" + stateChange.getModuleId() + "], channel [" + stateChange.getChannelNumber() + "], state [" + stateChange.getNewState() + "]");
					}
					
					for (final MulticastEventListener listener : this.eventListeners) {
						listener.digitalInputChannelStateChanged(event.getDigitalInputChannelStateChangedEvent());
					}
				} else {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Got a digital state change event, but the event in the message was null !");
					}
				}
				
				break;
			}
			
			case DIGITAL_OUTPUT_CHANNEL_STATE_CHANGED: {
				final DigitalOutputChannelStateChanged stateChange = event.getDigitalOutputChannelStateChangedEvent();
				
				if (stateChange != null) {
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Received a digital output channel state change : module [" + stateChange.getModuleId() + "], channel [" + stateChange.getChannelNumber() + "], state [" + stateChange.getNewState() + "]");
					}
					
					for (final MulticastEventListener listener : this.eventListeners) {
						listener.digitalOutputChannelStateChanged(event.getDigitalOutputChannelStateChangedEvent());
					}
					
				} else {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Got a digital state change event, but the event in the message was null !");
					}
				}
				
				break;
			}
			
			case DIGITAL_MODULE_CONFIG_CHANGE: {
				for (final MulticastEventListener listener : this.eventListeners) {
					listener.digitalModuleConfigurationChanged(event.getDigitalModuleConfigChanged());
				}
				
				break;
			}
			
			case DIMMER_MODULE_INPUT_CHANNEL_STATE_CHANGED: {
				final DimmerInputChannelStateChanged stateChange = event.getDimmerInputStateChanged();

				if (stateChange != null) {
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Received a dimmer input channel state change : module [" + stateChange.getModuleId() + "], channel [" + stateChange.getChannelNumber() + "], state [" + stateChange.getNewState() + "]");
					}
					
					for (final MulticastEventListener listener : this.eventListeners) {
						listener.dimmerInputStateChanged(stateChange);
					}
					
				} else {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Got a dimmer state change event, but the event in the message was null !");
					}
				}
				
				break;
			}
			
			case DIMMER_MODULE_OUTPUT_CHANNEL_STATE_CHANGED: {
				final DimmerOutputChannelStateChanged stateChange = event.getDimmerOutputStateChanged();

				if (stateChange != null) {
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Received a dimmer output channel state change : module [" + stateChange.getModuleId() + "], channel [" + stateChange.getChannelNumber() + "], state [" + stateChange.getOn() + "]");
					}
					
					for (final MulticastEventListener listener : this.eventListeners) {
						listener.dimmerOutputStateChanged(stateChange);
					}
					
				} else {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Got a dimmer state change event, but the event in the message was null !");
					}
				}
				
				break;
			}
			
			case DIMMER_MODULE_CONFIG_CHANGE: {
				for (final MulticastEventListener listener : this.eventListeners) {
					listener.dimmerModuleConfigurationChanged(event.getDimmerModuleConfigChanged());
				}
				
				break;
			}
		}
	}
	
	/**
	 * Adds an event listener.
	 * 
	 * @param 	listener		The listener to add.
	 */
	public final void addEventListener(final MulticastEventListener listener) {
		this.eventListeners.add(listener);
	}
	
	/**
	 * Removes the given event listener.
	 * 
	 * @param 	listener		The listener to remove.
	 */
	public final void removeEventListener(final MulticastEventListener listener) {
		this.eventListeners.remove(listener);
	}
}
