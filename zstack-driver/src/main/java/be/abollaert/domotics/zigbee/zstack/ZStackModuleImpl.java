package be.abollaert.domotics.zigbee.zstack;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ZStackModuleImpl} is the main entry point to the Zigbee dongle.
 * 
 * @author alex
 */
final class ZStackModuleImpl implements ZStackModule {
	
	/** Start of frame. */
	private static final int SOF = 0xFE;
	
	/** Logger definition. */
	private static final Logger logger = Logger
			.getLogger(ZStackModuleImpl.class.getName());

	/** The serial port name. */
	private final String serialPortName;
	
	/** The baud rate. */
	private final int baudRate;
	
	/** The serial port. */
	private SerialPort serialPort;
	
	/**
	 * Create a new instance.
	 */
	ZStackModuleImpl(final String serialPort, final int baudRate) {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "ZStack module : using serial port [" + serialPort + "], baud rate [" + baudRate + "]");
		}
		
		this.serialPortName = serialPort;
		this.baudRate = baudRate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect() throws ZStackException {
		final Enumeration<CommPortIdentifier> comPorts = CommPortIdentifier.getPortIdentifiers();
		
		boolean connected = false;
		
		while (comPorts.hasMoreElements() && !connected) {
			final CommPortIdentifier current = comPorts.nextElement();
			
			if (current.getPortType() == CommPortIdentifier.PORT_SERIAL && current.getName().equals(this.serialPortName)) {
				try {
					this.serialPort = (SerialPort)current.open("ZStack Driver", 2000);
					this.serialPort.setSerialPortParams(this.baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				} catch (PortInUseException e) {
					this.serialPort = null;
					
					throw new ZStackException("Failed to open serial port connected to the dongle [" + this.serialPortName + ", port was already in use.", e);
				} catch (UnsupportedCommOperationException e) {
					this.serialPort = null;
					
					throw new ZStackException("Failed to open serial port connected to the dongle [" + this.serialPortName + ", could not set serial port parameters.", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final <REQ extends ZStackRequest, RESP extends ZStackResponse> void sendRequest(final REQ request, final RESP response) throws ZStackException {
		if (this.serialPort != null) {
			try {
				final byte[] frame = getFrame(request);
				
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Sending frame [" + toString(frame) + "]");
				}
				
				this.serialPort.getOutputStream().write(frame);
				
				if (response != null) {
					// Wait for response.
				}
			} catch (IOException e) {
				throw new ZStackException("IO error sending frame : [" + request + "]", e);
			}
		} else {
			throw new IllegalStateException("Serial port [" + this.serialPortName + "] is not open !");
		}
	}
	
	/**
	 * Gets a frame representing the given command.
	 * 
	 * @param 	command		The command.
	 * 
	 * @return	The resulting frame.
	 */
	private static final byte[] getFrame(final ZStackRequest command) {
		final byte[] payload = command.getPayload();
		
		// SOF - LEN - CMD ID - PAYLOAD - FCS.
		final int frameSize = 1 + 1 + 2 + payload.length + 1;
		
		final byte[] frame = new byte[frameSize];
		frame[0] = (byte)(SOF & 0xFF);
		frame[1] = (byte)(payload.length & 0xFF);
		
		final int commandID = command.getCommandID();
		frame[2] = (byte)((commandID >> 8) & 0xFF);
		frame[3] = (byte)(commandID & 0xFF);
		
		for (int i = 0; i < payload.length; i++) {
			frame[4 + i] = payload[i];
		}
		
		final int fcs = calculateFCS(frame);
		frame[frameSize - 1] = (byte)(fcs & 0xFF);
		
		return frame;
	}
	
	/**
	 * Calculate the checksum.
	 * 
	 * @param 		frameData		The frame data covered by the checksum.
	 * 
	 * @return		The calculated checksum.
	 */
	private static final int calculateFCS(final byte[] frameData) {
		int fcs = 0;
		
		for (int i = 2; i < frameData.length - 1; i++) {
			fcs = fcs ^ (frameData[i] & 0xFF);
		}
		
		return fcs;
	}
	
	private static final String toString(final byte[] frame) {
		final StringBuilder builder = new StringBuilder();
		
		int i = 0;
		
		while (i < frame.length) {
			builder.append("0x");
			
			final String currentByteString = Integer.toHexString(frame[i] & 0xFF);
			
			if (currentByteString.length() == 1) {
				builder.append("0");
			}
			
			builder.append(currentByteString);
			
			if (i < frame.length - 1) {
				builder.append(" ");
			}
			
			i++;
		}
		
		return builder.toString();
	}
	
	public static void main(String[] args) throws Exception {
		final ZStackModule module = new ZStackModuleImpl("/dev/ttyUSB0", 57600);
		
		module.connect();
		
		SysVersionResponse response = null;
		module.sendRequest(new SysVersionRequest(), response);
	}
}
