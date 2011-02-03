package be.abollaert.domotics.light.server.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.server.kernel.ProtocolParser.RequestPDU;
import be.abollaert.domotics.light.server.kernel.ProtocolParser.ResponsePDU;

public abstract class AbstractChannel implements Channel {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(AbstractChannel.class
			.getName());
	
	/** Try 3 times before bailing out. */
	private static final int NUMBER_OF_TRIES = 3;
	
	/** The PDU start. */
	private static final char PDU_START = '{';
	
	/** The PDU end. */
	private static final char PDU_END = '}';
	
	/** The command lock. */
	private final Lock commandLock = new ReentrantLock();
	
	/** The response queue. */
	private final BlockingQueue<ResponsePDU> responseQueue = new ArrayBlockingQueue<ResponsePDU>(1);
	
	/** The responsereader. */
	private ResponseReader responseReader;
	
	private ExecutorService responseReaderService;
	
	/** The event listeners. */
	private final Set<CommunicationChannelEventListener> eventListeners = Collections.newSetFromMap(new WeakHashMap<CommunicationChannelEventListener, Boolean>());
	
	/**
	 * {@inheritDoc}
	 */
	public final void connect() throws IOException {
		this.connectInternal();
		
		this.responseReader = new ResponseReader(this.getInputStream());
		this.responseReaderService = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public final Thread newThread(final Runnable r) {
				return new Thread(r, "Serial Channel : Response Reader");
			}
		});
		
		this.responseReaderService.execute(this.responseReader);
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Connected.");
		}
	}
	
	protected abstract void connectInternal() throws IOException;
	
	protected abstract InputStream getInputStream() throws IOException;
	
	/**
	 * Runnable that is used for reading the responses.
	 * 
	 * @author alex
	 *
	 */
	private final class ResponseReader implements Runnable {
		
		/** The protocol state. */
		private static final int WAITING_FOR_RESPONSE = 0;
		private static final int READING_PAYLOAD = 2;
		
		/**
		 * The stream.
		 */
		private final InputStream stream;
		
		/** The protocol state. */
		private int state = WAITING_FOR_RESPONSE;
		
		/** The buffer. */
		private final StringBuilder buffer = new StringBuilder();
		
		/** Indicate a stop. */
		private volatile boolean stop = false;
		
		/** The event dispatch executor. */
		private final ExecutorService eventDispatchExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public final Thread newThread(final Runnable r) {
				return new Thread(r, "Serial driver event dispatch thread");
			}
		});
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	stream		The stream to use.
		 */
		private ResponseReader(final InputStream stream) {
			this.stream = stream;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public final void run() {
			final byte[] buffer = new byte[10];
			
			while (true && !stop && !Thread.interrupted()) {
				try {
					if (logger.isLoggable(Level.FINER)) {
						logger.log(Level.FINER, "Reading from stream.");
					}
					
					final int numberOfBytesRead = this.stream.read(buffer);
					
					for (int i = 0; i < numberOfBytesRead; i++) {
						final char currentCharacter = (char)buffer[i];
						
						if (logger.isLoggable(Level.FINEST)) {
							logger.finest("Character [" + currentCharacter + "] received.");
						}
						
						switch (this.state) {
						case WAITING_FOR_RESPONSE:
							if (currentCharacter == PDU_START) {
								this.state = READING_PAYLOAD;
								this.buffer.setLength(0);
							}
							
							break;
						case READING_PAYLOAD:
							if (currentCharacter == PDU_START) {
								if (logger.isLoggable(Level.WARNING)) {
									logger.log(Level.WARNING, "Unexpected start of PDU received when processing PDU, skipping.");
								}
								
								this.state = WAITING_FOR_RESPONSE;
							} else if (currentCharacter == PDU_END) {
								if (logger.isLoggable(Level.FINE)) {
									logger.log(Level.FINE, "Full PDU received, contents are [" + this.buffer.toString() + "]");
								}
								
								final ResponsePDU responsePDU = ProtocolParser.parse(this.buffer.toString());
								
								if (responsePDU != null) {
									if (responsePDU.getType() == ResponsePDU.Type.OK || responsePDU.getType() == ResponsePDU.Type.ERROR) {
										if (logger.isLoggable(Level.FINE)) {
											logger.log(Level.FINE, "Have a response for command of type [" + responsePDU.getType().name() + "]");
										}
										try {
											responseQueue.put(responsePDU);
										} catch (InterruptedException e) {
											if (logger.isLoggable(Level.WARNING)) {
												logger.log(Level.WARNING, "Interrupted while putting response [" + this.buffer.toString() + "] on the queue [" + e.getMessage() + "]", e);
											}
										}
									} else {
										if (logger.isLoggable(Level.FINE)) {
											logger.log(Level.FINE, "Dispatching event of type [" + responsePDU.getType().name() + "]");
										}
										
										for (final CommunicationChannelEventListener listener : eventListeners) {
											this.eventDispatchExecutor.execute(new Runnable() {
												public final void run() {
													try {
														listener.eventReceived(responsePDU);
													} catch (Throwable e) {
														if (logger
																.isLoggable(Level.WARNING)) {
															logger.log(Level.WARNING,
																	"Event listener [" + listener + "] caused an exception [" + e.getMessage() + "]", e);
														}
													}
												}
											});

										}
									}
								} else {
									if (logger.isLoggable(Level.WARNING)) {
										logger.log(Level.WARNING, "PDU could not be parsed, got null from the protocol parser.");
									}
								}
								
								this.state = WAITING_FOR_RESPONSE;
							} else {
								this.buffer.append(currentCharacter);
							}
							
							break;
						}
					}
				} catch (IOException e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "IO error while reading from stream [" + e.getMessage() + "]", e);
					}
				}
			}
			
			this.eventDispatchExecutor.shutdown();
			
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Response reader stopped.");
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public final void disconnect() {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Disconnecting channel.");
		}
		if (this.isConnected()) {
			this.responseReader.stop = true;
			this.responseReaderService.shutdownNow();
			
			
			try {
				this.responseReaderService.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Interrupted while waiting for response reader to shutdown.", e);
				}
			}
			
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
			}
			
			this.disconnectInternal();
		}
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Done, channel disconnected.");
		}
	}
	
	protected abstract void disconnectInternal();
	
	
	/**
	 * {@inheritDoc}
	 */
	public final void addEventListener(final CommunicationChannelEventListener eventListener) {
		this.eventListeners.add(eventListener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void removeEventListener(final CommunicationChannelEventListener listener) {
		this.eventListeners.remove(listener);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public final ResponsePDU sendCommand(final be.abollaert.domotics.light.server.kernel.ProtocolParser.RequestPDU command) throws IOException {
		this.commandLock.lock();
		this.responseQueue.clear();
		
		int currentTry = 1;
		
		final String commandString = ProtocolParser.serialize(command);
		
		try {
			while (currentTry <= NUMBER_OF_TRIES) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Sending command [" + commandString + "]");
				}
				
				final byte[] commandBytes = commandString.getBytes("US-ASCII");
				this.getOutputStream().write(commandBytes, 0, commandBytes.length);
				this.getOutputStream().flush();
				
				try {
					final ResponsePDU response = this.responseQueue.poll(2, TimeUnit.SECONDS);
					
					if (response == null) {
						currentTry++;
					} else {
						return response;
					}
				} catch (InterruptedException e) {
					currentTry++;
				}
			}
			
			return null;
		} finally {
			this.commandLock.unlock();
		}
	}
	
	protected abstract OutputStream getOutputStream() throws IOException;
	
	/**
	 * {@inheritDoc}
	 */
	public final ChannelType probeType() throws IOException {
		final ResponsePDU response = this.sendCommand(new RequestPDU(RequestPDU.Type.GET_TYPE, new int[0]));
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.OK) {
				if (response.getArguments().length == 1) {
					return (response.getArguments()[0] == 1 ? ChannelType.DIGITAL : ChannelType.DIMMER);
				}
			}
		}
		
		return null;
	}
}
