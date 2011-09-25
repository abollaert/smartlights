package be.abollaert.domotics.zigbee.zstack;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ZStackModuleImpl} is the main entry point to the Zigbee dongle.
 * 
 * @author alex
 */
final class ZStackModuleImpl implements ZStackModule {
	
	/** Wait for 5 seconds max for a synchronous response. */
	private static final int SECONDS_SYNC_TIMEOUT = 5;
	
	/** Start of frame. */
	static final int SOF = 0xFE;
	
	/** Logger definition. */
	private static final Logger logger = Logger.getLogger(ZStackModuleImpl.class.getName());

	/** The serial port name. */
	private final String serialPortName;
	
	/** The baud rate. */
	private final int baudRate;
	
	/** The serial port. */
	private SerialPort serialPort;
	
	/** The response reader thread. */
	private Thread responseReaderThread;
	
	/** The response reader. */
	private ResponseReader responseReader;
	
	/** The information table. */
	private final DeviceInfoTable infoTable;
	
	/** Queue for the responses. */
	private final BlockingQueue<ZStackFrame> responseQueue = new ArrayBlockingQueue<ZStackFrame>(1);
	
	/** The command lock for thread safety. */
	private final Lock commandLock = new ReentrantLock();
	
	/** For the reset, which is an asynchonous operation, we use a cyclic barrier. */
	private final CyclicBarrier resetBarrier = new CyclicBarrier(2);
	
	/** The device discovery service. */
	private ExecutorService deviceDiscoveryService;
	
	/** The zigbee nodes. */
	private final Map<Integer, ZigbeeNode> nodes = new HashMap<Integer, ZigbeeNode>();
	
	private final Set<ZigbeeDeviceListener> listeners = Collections.newSetFromMap(new WeakHashMap<ZigbeeDeviceListener, Boolean>());
	
	/**
	 * Create a new instance.
	 */
	ZStackModuleImpl(final String serialPort, final int baudRate, final DeviceInfoTable infoTable) throws IOException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "ZStack module : using serial port [" + serialPort + "], baud rate [" + baudRate + "]");
		}
		
		final File portFile = new File(serialPort);
		
		this.serialPortName = portFile.getCanonicalPath();
		this.baudRate = baudRate;
		this.infoTable = infoTable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void connect() throws ZStackException {
		final Enumeration<CommPortIdentifier> comPorts = CommPortIdentifier.getPortIdentifiers();
		
		boolean connected = false;
		
		while (comPorts.hasMoreElements() && !connected) {
			final CommPortIdentifier current = comPorts.nextElement();
			
			if (current.getPortType() == CommPortIdentifier.PORT_SERIAL && current.getName().equals(this.serialPortName)) {
				try {
					this.serialPort = (SerialPort)current.open("ZStack Driver", 1000);
					this.serialPort.setSerialPortParams(this.baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					
					this.responseReader = new ResponseReader(this.serialPort.getInputStream());
					this.responseReaderThread = new Thread(this.responseReader, "ZStack : Response reader");
					this.responseReaderThread.start();
					
					this.deviceDiscoveryService = Executors.newSingleThreadExecutor(new ThreadFactory() {
						@Override
						public final Thread newThread(final Runnable r) {
							return new Thread(r, "ZStack : Device discovery service");
						}
					});
					
					this.reset();
				} catch (PortInUseException e) {
					this.serialPort = null;
					
					throw new ZStackException("Failed to open serial port connected to the dongle [" + this.serialPortName + ", port was already in use.", e);
				} catch (UnsupportedCommOperationException e) {
					this.serialPort = null;
					
					throw new ZStackException("Failed to open serial port connected to the dongle [" + this.serialPortName + ", could not set serial port parameters.", e);
				} catch (IOException e) {
					this.serialPort = null;
					
					throw new ZStackException("Failed to open serial port connected to the dongle [" + this.serialPortName + ", error getting input stream.", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	final ZStackFrame sendSynchonousRequest(final ZStackFrame requestFrame) throws ZStackException {
		if (this.serialPort != null) {
			try {
				this.commandLock.lock();
				
				// Clear previous responses that were not picked up.
				this.responseQueue.clear();
				
				final int[] frame = requestFrame.toWireFrame();
				
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Sending frame [" + ZStackUtils.asString(frame) + "]");
				}
				
				for (int i = 0; i < frame.length; i++) {
					this.serialPort.getOutputStream().write(frame[i] & 0xFF);
				}
				
				this.serialPort.getOutputStream().flush();
				
				ZStackFrame response = null;
				
				try {
					response = this.responseQueue.poll(SECONDS_SYNC_TIMEOUT, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Interrupted while waiting for response : [" + e.getMessage() + "]", e);
					}
				}
				
				return response;
			} catch (IOException e) {
				throw new ZStackException("IO error sending frame : [" + requestFrame + "]", e);
			} finally {
				this.commandLock.unlock();
			}
		} else {
			throw new IllegalStateException("Serial port [" + this.serialPortName + "] is not open !");
		}
	}
	
	/**
	 * Calculate the checksum.
	 * 
	 * @param 		frameData		The frame data covered by the checksum.
	 * 
	 * @return		The calculated checksum.
	 */
	static final int calculateFCS(final int[] frameData, final int startIndex, final int length) {
		int fcs = 0;
		
		for (int i = startIndex; i < startIndex + length; i++) {
			fcs = (fcs ^ (frameData[i] & 0xFF));
		}
		
		return fcs & 0xFF;
	}
	
	/**
	 * Calculate the checksum.
	 * 
	 * @param 		frameData		The frame data covered by the checksum.
	 * 
	 * @return		The calculated checksum.
	 */
	private static boolean validateFCS(final int fcs, final int[] frameData) {
 		return calculateFCS(frameData, 0, frameData.length) == fcs;
	}
	
	/**
	 * Response reader thread.
	 * 
	 * @author alex
	 */
	private final class ResponseReader implements Runnable {
		
		/** Should be <code>true</code> while we are running. */
		private volatile boolean active = true;
		
		/** The protocol state. */
		private ProtocolState protocolState = ProtocolState.WAITING_FOR_SOF;
		
		private final InputStream inputStream;
		
		/** The current frame length. */
		private int currentFrameLength;
		
		/** The current command ID. */
		private int currentCommandId;
		
		/** The frame data. */
		private int[] frameData;
		
		/** The data offset. */
		private int dataOffset;
		
		private ResponseReader(final InputStream stream) {
			this.inputStream = stream;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public final void run() {
			while (this.active) {
				try {
					final int byteRead = this.inputStream.read();
					
					if (byteRead != -1) {
						switch (this.protocolState) {
							case WAITING_FOR_SOF: {
								if (byteRead == SOF) {
									this.protocolState = ProtocolState.WAITING_FOR_FRAME_LENGTH;
									this.currentFrameLength = 0;
								}
								
								break;
							}
							
							case WAITING_FOR_FRAME_LENGTH: {
								this.currentFrameLength = (byteRead & 0xFF);
								
								this.protocolState = ProtocolState.WAITING_FOR_COMMAND;
								this.currentCommandId = 0;
								
								break;
							}
							
							case WAITING_FOR_COMMAND: {
								this.currentCommandId += (byteRead) << 8;
								this.protocolState = ProtocolState.READING_COMMAND;
								
								break;
							}
							
							case READING_COMMAND: {
								this.currentCommandId += (byteRead);
								this.protocolState = ProtocolState.READING_DATA;
								
								if (this.currentFrameLength == 0) {
									this.protocolState = ProtocolState.WAITING_FOR_CHECKSUM;
								} else {
									this.protocolState = ProtocolState.READING_DATA;
									this.frameData = new int[this.currentFrameLength];
									this.dataOffset = 0;
								}
								
								break;
							}
							
							case READING_DATA: {
								this.frameData[this.dataOffset] = byteRead;
								dataOffset++;
								
								if (dataOffset == this.currentFrameLength) {
									this.protocolState = ProtocolState.WAITING_FOR_CHECKSUM;
								}
								
								break;
							}
							
							case WAITING_FOR_CHECKSUM: {
								// The length also needs to be included in the checksum.
								final int[] frameDataCoveredByFCS = new int[3 + this.frameData.length];
								frameDataCoveredByFCS[0] = this.currentFrameLength;
								frameDataCoveredByFCS[1] = (this.currentCommandId & 0xFF00) >> 8;
								frameDataCoveredByFCS[2] = this.currentCommandId & 0xFF;
								
								for (int i = 0; i < this.frameData.length; i++) {
									frameDataCoveredByFCS[3 + i] = this.frameData[i];
								}
								
								if (validateFCS(byteRead, frameDataCoveredByFCS)) {
									if (logger.isLoggable(Level.INFO)) {
										logger.log(Level.INFO, "Full frame received : API ID : [" + ZStackUtils.asHex(this.currentCommandId) + "], frame data : [" + ZStackUtils.asString(this.frameData) + "], FCS [" + ZStackUtils.asHex(byteRead) + "]");
									}
									
									this.processFrame();
								} else {
									if (logger.isLoggable(Level.WARNING)) {
										logger.log(Level.WARNING, "Received frame but FCS was not valid.");
									}
								}
								
								this.protocolState = ProtocolState.WAITING_FOR_SOF;
								break;
							}
						}
					}
				} catch (IOException e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "IO error while reading from input stream : [" + e.getMessage() + "]", e);
					}
				}
			}
			
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Response reader has stopped.");
			}
		}
		
		private final void processFrame() {
			final MessageType messageType = MessageType.fromCommandID(this.currentCommandId);
			
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "The message received is a [" + messageType + "]");
			}
			
			switch (messageType) {
				case SYNCHRONOUS_RESPONSE: {
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Received a synchronous response.");
					}
					
					final ZStackFrame response = new ZStackFrame(this.currentCommandId, this.frameData);
					
					try {
						responseQueue.put(response);
					} catch (InterruptedException e) {
						if (logger.isLoggable(Level.WARNING)) {
							logger.log(Level.WARNING, "Interrupted while putting response on the queue : [" + e.getMessage() + "]", e);
						}
					}
					
					break;
				}
				
				case ASYNCHRONOUS_REQUEST: {
					final ZStackFrame request = new ZStackFrame(this.currentCommandId, this.frameData);
					
					if (request.getCommandID() == CommandID.SYSTEM_RESET_IND) {
						if (logger.isLoggable(Level.INFO)) {
							logger.log(Level.INFO, "Reset is complete, notifying barrier.");
						}
						
						try {
							resetBarrier.await();
						} catch (InterruptedException e) {
							if (logger.isLoggable(Level.WARNING)) {
								logger.log(Level.WARNING, "Interrupted while notifying reset completion !", e);
							}
						} catch (BrokenBarrierException e) {
							if (logger.isLoggable(Level.WARNING)) {
								logger.log(Level.WARNING, "Broken barrier while waiting for reset to complete !", e);
							}
						}
					} else if (request.getCommandID() == CommandID.AF_INCOMING_MESSAGE) {
						final IncomingMessage message = new IncomingMessage(request.getPayload());
						
						if (logger.isLoggable(Level.INFO)) {
							logger.log(Level.INFO, "Received an unsollicited message [" + message + "]");
						}
						
						if (message.getClusterId() == ClusterID.OCCUPANCY_SENSING) {
							final ZCLFrame zclFrame = new ZCLFrame(message.getPayload());
							
							if (logger.isLoggable(Level.INFO)) {
								logger.log(Level.INFO, "Occupancy sensing frame : [" + zclFrame + "]");
							}
							
							final ZigbeeNode node = nodes.get(message.getSourceAddress());
							
							if (node != null) {
								node.reportAttributes(zclFrame.getPayload());
							}
						}
					} else if (request.getCommandID() == CommandID.ZDO_END_DEVICE_ANNCE_IND) {
						int i = 0;
						
						final int sourceAddress = (request.getPayload()[i++] & 0xFF) + (request.getPayload()[i++] << 8);
						final int shortAddress = (request.getPayload()[i++] & 0xFF) + (request.getPayload()[i++] << 8);
						
						final int[] ieeeAddress = new int[8];
						
						for (int j = 0; j < 8; j++) {
							ieeeAddress[j] = request.getPayload()[i + 7 - j];
						}
						
						//System.arraycopy(request.getPayload(), i, ieeeAddress, 0, 8);
						i+=8;
						
						int capabilities = request.getPayload()[i];
						
						if (logger.isLoggable(Level.INFO)) {
							logger.log(Level.INFO, "New device has announced itself : short address [" + ZStackUtils.asHex(shortAddress) + "], IEEE address [" + ZStackUtils.asString(ieeeAddress) + "]");
						}
						
						final IEEEAddress address = new IEEEAddress(ieeeAddress);
						final SensorType type = infoTable.getSensorType(address);
						
						if (type != null) {
							if (logger.isLoggable(Level.INFO)) {
								logger.log(Level.INFO, "The device is of type [" + type + "]");
							}
							
							switch (type) {
								case OCCUPANCY: {
									final ZigbeeOccupancySensor sensor = new ZigbeeOccupancySensor(shortAddress, address, 0x01, ZStackModuleImpl.this);
									nodes.put(shortAddress, sensor);
									
									for (final ZigbeeDeviceListener listener : listeners) {
										listener.occupancySensorAdded(sensor);
									}
									break;
								}
							}
						}
					}
					
					break;
				}
			}
		}
	}
	
	/** The protocol state. */
	private static enum ProtocolState {
		WAITING_FOR_SOF,
		WAITING_FOR_FRAME_LENGTH,
		WAITING_FOR_COMMAND,
		READING_COMMAND,
		READING_DATA,
		WAITING_FOR_CHECKSUM;
	};
	
	private static enum MessageType {
		POLL(0x00),
		SYNCHRONOUS_REQUEST(0x02),
		SYNCHRONOUS_RESPONSE(0x06),
		ASYNCHRONOUS_REQUEST(0x04);
		
		private final int id;
		
		private MessageType(final int id) {
			this.id = id;
		}
		
		private static final MessageType fromCommandID(final int commandID) {
			final int typeInCommandID = (commandID & 0x7000) >> 12;
							
			for (final MessageType type : MessageType.values()) {
				if (type.id == typeInCommandID) {
					return type;
				}
			}
			
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getFirmwareVersion() throws ZStackException {
		final ZStackFrame frame = new ZStackFrame(CommandID.SYS_VERSION, new int[0]);
		final ZStackFrame response = this.sendSynchonousRequest(frame);
		
		if (response.getCommandID() == CommandID.SYS_VERSION_RESPONSE) {
			final StringBuilder firmwareVersionBuilder = new StringBuilder();
			firmwareVersionBuilder.append(response.getPayload()[2]).append(".").append(response.getPayload()[3]);
			
			return firmwareVersionBuilder.toString();
		}
		
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		final DeviceInfoTable infoTable = new DeviceInfoTable();
		infoTable.addSensor(new IEEEAddress(new int[] {
			0x10,
			0x00,
			0x00,
			0x50,
			0xc2,
			0x36,
			0x63,
			0x1b
		}), SensorType.OCCUPANCY);
		
		final ZStackModule module = new ZStackModuleImpl("/dev/ttyUSB0", 57600, infoTable);
		module.connect();
		//module.disconnect();
		/*String firmwareVersion = module.getFirmwareVersion();
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Module firmware version is [" + firmwareVersion + "]");
		}
		
		final long ieeeAddress = module.getChannel();*/
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void disconnect() throws ZStackException {
		this.deviceDiscoveryService.shutdownNow();
		
		this.responseReader.active = false;
		
		try {
			this.serialPort.getInputStream().close();
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "IO error while closing the serial port input stream : [" + e.getMessage() + "]", e);
			}
		}
		
		this.responseReaderThread.interrupt();
		
		this.serialPort.close();
		this.serialPort = null;
		this.responseReaderThread = null;
		this.responseReader = null;
	}
	
	final void sendAsynchronousRequest(final ZStackFrame requestFrame) throws ZStackException {
		if (this.serialPort != null) {
			try {
				this.commandLock.lock();
				
				final int[] frame = requestFrame.toWireFrame();
				
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Sending frame [" + ZStackUtils.asString(frame) + "]");
				}
				
				for (int i = 0; i < frame.length; i++) {
					this.serialPort.getOutputStream().write(frame[i] & 0xFF);
				}
				
				this.serialPort.getOutputStream().flush();
			} catch (IOException e) {
				throw new ZStackException("IO error sending frame : [" + requestFrame + "]", e);
			} finally {
				this.commandLock.unlock();
			}
		} else {
			throw new IllegalStateException("Serial port [" + this.serialPortName + "] is not open !");
		}
	}
	
	private final void reset() throws ZStackException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Resetting dongle.");
		}
		
		this.resetBarrier.reset();
		
		try {
			this.commandLock.lock();
			
			this.sendAsynchronousRequest(new ZStackFrame(CommandID.SYSTEM_RESET, new int[] { 0 }));

			this.resetBarrier.await();
			
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "System reset complete.");
			}
		} catch (BrokenBarrierException e) {
			throw new ZStackException("Reset barrier was broken while waiting for reset to complete !", e);
		} catch (InterruptedException e) {
			throw new ZStackException("Interrupted while waiting for barrier !", e);
		} finally {
			this.commandLock.unlock();
		}
	}
	
	final void sendData(final int[] destinationAddress, final int destinationEndPoint, final int sourceEndPoint, final int cluster, final int txSequenceNumber, final int[] payload) throws ZStackException {
		final int[] frameData = new int[10 + payload.length];
		int i = 0;
		
		frameData[i++] = destinationEndPoint & 0xFF;
		frameData[i++] = (destinationEndPoint & 0xFF00) >> 8;
		frameData[i++] = destinationEndPoint;
		frameData[i++] = sourceEndPoint;
		frameData[i++] = cluster & 0xFF;
		frameData[i++] = (cluster & 0xFF00) >> 8;
		frameData[i++] = txSequenceNumber;
		frameData[i++] = 0x00;
		frameData[i++] = 0x07;
		frameData[i++] = payload.length;
		
		System.arraycopy(payload, 0, frameData, i, payload.length);
		
		final ZStackFrame request = new ZStackFrame(CommandID.AF_DATA_REQUEST, frameData);
		final ZStackFrame response = this.sendSynchonousRequest(request);
	}

	@Override
	public final void addZigbeeDeviceListener(ZigbeeDeviceListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public final void removeZigbeeDeviceListener(ZigbeeDeviceListener listener) {
		this.listeners.remove(listener);
	}
}
