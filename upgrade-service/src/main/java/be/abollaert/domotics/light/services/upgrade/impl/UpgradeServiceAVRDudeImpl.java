package be.abollaert.domotics.light.services.upgrade.impl;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.services.upgrade.UpgradeService;

/**
 * Implementation of the upgrade service using AVR dude.
 * 
 * @author alex
 */
public final class UpgradeServiceAVRDudeImpl implements UpgradeService {

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(UpgradeServiceAVRDudeImpl.class.getName());
	
	/** This is the baud rate. */
	private static final int BAUDRATE = 19200;
	
	/** Command used to execute AVR dude. */
	private static final String AVRDUDE_COMMAND = "avrdude";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void upgradeNode(final String portName, final String hexFileLocation) throws IOException {
		if (portName == null) {
			throw new IllegalArgumentException("Port cannot be null here !");
		}
		
		if (hexFileLocation == null) {
			throw new IllegalArgumentException("Hex file location cannot be null here !");
		}
		
		final File hexFile = new File(hexFileLocation);
		
		if (!hexFile.exists() || !hexFile.isFile()) {
			throw new IllegalArgumentException("[" + hexFileLocation + "] is not a file or it is not a file !");
		}
		
		final SerialPort port = this.getSerialPort(portName);
		this.pulseDTR(port);
		port.close();
		
		this.runAVRDude(hexFileLocation, portName);
	}
	
	/**
	 * Gets the serial port on the given port name. Throws an {@link IOException} if it does not succeed in doing so.
	 * 
	 * @param 		portName		The port name.
	 * 
	 * @return		The port.
	 * 
	 * @throws 		IOException		If the port could not be opened.	
	 */
	private final SerialPort getSerialPort(final String portName) throws IOException {
		try {
			final CommPortIdentifier identifier = CommPortIdentifier.getPortIdentifier(portName);
			
			if (identifier.isCurrentlyOwned()) {
				throw new IOException("The port specified [" + portName + "] is already in use by [" + identifier.getCurrentOwner() + "] !");
			}
			
			final CommPort rawPort = identifier.open("DOM_SER_DRV", 2000);
			
			if (rawPort instanceof SerialPort) {
				final SerialPort port = (SerialPort)rawPort;
				port.setSerialPortParams(BAUDRATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				
				return port;
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
	
	/**
	 * Pulses the DTR on the given serial port.
	 * 
	 * @param 		serialPort		The port on which to pulse the DTR.
	 * 
	 * @throws 		IOException		If an IO error occurs during the pulse.
	 */
	private final void pulseDTR(final SerialPort serialPort) throws IOException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Pulsing the DTR on serial port [" + serialPort.getName() + "]");
		}
		
		serialPort.setDTR(true);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// Eat.
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Was interrupted while pulsing the DTR of port [" + serialPort.getName() + "]", e);
			}
		}
		
		serialPort.setDTR(false);
	}
	
	private final void runAVRDude(final String hexFileLocation, final String port) throws IOException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Running AVRDude, hex file [" + hexFileLocation + "], upload to port [" + port + "]");
		}
		
		// avrdude -p m168 -C /etc/avrdude.conf -c avrisp -P $port -b BAUDRATE -F -U flash:w:$hexFileLocation
		final List<String> commands = new ArrayList<String>();
		commands.add(AVRDUDE_COMMAND);
		commands.add("-p");
		commands.add("m168");
		commands.add("-C");
		commands.add("/etc/avrdude.conf");
		commands.add("-c");
		commands.add("avrisp");
		commands.add("-P");
		commands.add(port);
		commands.add("-b");
		commands.add(String.valueOf(BAUDRATE));
		commands.add("-F");
		commands.add("-U");
		commands.add(new StringBuilder("flash:w:").append(hexFileLocation).toString());
		
		final ProcessBuilder builder = new ProcessBuilder(commands);
		final Process process = builder.start();
		
		if (logger.isLoggable(Level.FINE)) {
			BufferedReader outputReader = null;
			
			try {
				outputReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				
				String line = outputReader.readLine();
				
				while (line != null) {
					if (logger.isLoggable(Level.FINE)) {
						logger.log(Level.FINE, line);
					}
					
					line = outputReader.readLine();
				}
			} finally {
				if (outputReader != null) {
					try {
						outputReader.close();
					} catch (IOException e) {
						if (logger.isLoggable(Level.WARNING)) {
							logger.log(Level.WARNING, "Could not close output reader for AVR dude command due to an IO error [" + e.getMessage() + "]", e);
						}
					}
				}
			}
			
			try {
				final int exitCode = process.waitFor();
				
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "AVRDude finished with exit code [" + exitCode + "]");
				}
				
				if (exitCode != 0) {
					throw new IOException("Error executing AVRDude to upgrade node at [" + port + "] with hex file [" + hexFileLocation + "], return code was [" + exitCode + "]");
				}
				
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "AVRDude finished successfully.");
				}
			} catch (InterruptedException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Interrupted while waiting for AVRDude to finish.", e);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			final UpgradeService service = new UpgradeServiceAVRDudeImpl();
			service.upgradeNode("/dev/ttyUSB0", "/home/alex/eclipse/workspaces/personal/smartlights2/smartlights/arduino_digital/Debug/arduino_digital.hex");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
