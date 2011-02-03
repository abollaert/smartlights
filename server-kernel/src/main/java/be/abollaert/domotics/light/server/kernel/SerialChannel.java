package be.abollaert.domotics.light.server.kernel;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The serial channel is the place where all the serial communication is done.
 * 
 * @author alex
 *
 */
final class SerialChannel extends AbstractChannel {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(SerialChannel.class.getName());

	/** This is the baud rate. */
	private static final int BAUDRATE = 19200;
	
	/** The port name. */
	private final String portName;
	
	/** The serial port. */
	private SerialPort port;
	
	/**
	 * Creates a new serial channel to talk to the given port.
	 * 
	 * @param 		portName		The name of the port.
	 */
	SerialChannel(final String portName) {
		this.portName = portName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final boolean isConnected() {
		return this.port != null;
	}

	@Override
	protected void connectInternal() throws IOException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Connecting to port [" + this.portName + "]");
		}
		
		try {
			final CommPortIdentifier identifier = CommPortIdentifier.getPortIdentifier(this.portName);
			
			if (identifier.isCurrentlyOwned()) {
				throw new IOException("The port specified [" + portName + "] is already in use by [" + identifier.getCurrentOwner() + "] !");
			}
			
			final CommPort rawPort = identifier.open("DOM_SER_DRV", 2000);
			
			if (rawPort instanceof SerialPort) {
				this.port = (SerialPort)rawPort;
				this.port.setSerialPortParams(BAUDRATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				
				// Set the receive time out to 2 seconds. This will enable some sort of polling. Otherwise an interrupt does not do anything and hangs
				// the driver.
				this.port.enableReceiveTimeout(2);
			} else {
				throw new IOException("Port [" + portName + "] is not a serial port !");
			}
		} catch (NoSuchPortException e) {
			throw new IOException("The port specified [" + portName + "] does not exist !", e);
		} catch (PortInUseException e) {
			throw new IOException("The port specified [" + portName + "] is already in use !");
		} catch (UnsupportedCommOperationException e) {
			throw new IOException("Unsupported comm operation when setting the serial parameters !", e);
		}
	}

	@Override
	protected final void disconnectInternal() {
		try {
			this.port.getInputStream().close();
			this.port.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.port.close();
		
		this.port = null;
	}

	@Override
	protected final InputStream getInputStream() throws IOException {
		return this.port.getInputStream();
	}

	@Override
	protected final OutputStream getOutputStream() throws IOException {
		return this.port.getOutputStream();
	}

	@Override
	public final String getName() {
		return this.portName;
	}

	@Override
	public final boolean supportsUpgrade() {
		return false;
	}

	@Override
	public final void upgrade(final int moduleId, final InputStream hexFileStream) throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Serial channel does not support upgrading (yet).");
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		final SerialChannel channel = new SerialChannel("/dev/ttyUSB0");
		channel.connectInternal();
		
		Thread.sleep(2000);
		
		channel.disconnectInternal();
	}
} 
