package be.techniquez.hometinkering.lightcontrol.controlboard.spi.arduino;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.ControlBoardDriver;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.arduino.connector.ArduinoConnector;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.arduino.connector.ArduinoConnector.BoardType;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;

/**
 * Driver class for the arduino light control boards.
 * 
 * @author alex
 */
public final class ArduinoControlBoardDriver implements ControlBoardDriver {
	
	/** Logger instance used in this class. */
	private static final Logger logger = Logger.getLogger("controlboard");
	
	/** The serial port format. */
	private static final MessageFormat SERIAL_PORT_FORMAT = new MessageFormat("/dev/ttyUSB{0}");
	
	/** The digital boards. */
	private DigitalLightControlBoard[] digitalBoards = new DigitalLightControlBoard[0];
	
	/** The dimmable boards. */
	private DimmerLightControlBoard[] dimmerBoards = new DimmerLightControlBoard[0];
	
	/**
	 * Creates a driver. Driver will scan for possible serial devices here.
	 */
	public ArduinoControlBoardDriver() {
		this.reload();
	}

	/**
	 * {@inheritDoc}
	 */
	public final DigitalLightControlBoard[] loadConnectedDigitalBoards() {
		return this.digitalBoards;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getName() {
		return "Arduino";
	}

	/**
	 * FIXME: Not implemented as of yet.
	 * 
	 * {@inheritDoc}
	 */
	public final DimmerLightControlBoard[] loadConnectedDimmerBoards() {
		return this.dimmerBoards;
	}

	/**
	 * Reloads the board.
	 */
	public void reload() {
		for (final DigitalLightControlBoard digitalBoard : this.digitalBoards) {
			((ArduinoConnector)digitalBoard).destroy();
		}
		
		for (final DimmerLightControlBoard dimmerBoard : this.dimmerBoards) {
			((ArduinoConnector)dimmerBoard).destroy();
		}
		
		final Set<DigitalLightControlBoard> digitalBoards = new HashSet<DigitalLightControlBoard>();
		final Set<DimmerLightControlBoard> dimmerBoards = new HashSet<DimmerLightControlBoard>();
		
		for (int i = 0; i < 10; i++) {
			final String currentDevicePath = SERIAL_PORT_FORMAT.format(new Object[] { i });
			
			logger.info("Probing on device path [" + currentDevicePath + "]");
			
			try {
				final CommPortIdentifier commportID = CommPortIdentifier.getPortIdentifier(currentDevicePath);
			
				logger.info("Serial device found at [" + currentDevicePath + "], trying to initialize...");
				
				final ArduinoConnector connector = new ArduinoConnector(commportID.open("LCJAVA", 2000));
				
				if (connector.getBoardType().equals(BoardType.DIGITAL)) {
					digitalBoards.add(connector);
				} else {
					dimmerBoards.add(connector);
				}
			} catch (NoSuchPortException e) {
				logger.info("No serial device found at [" + currentDevicePath + "]");
			} catch (PortInUseException e) {
				logger.info("We have a serial device on [" + currentDevicePath + "], but it is in use by [" + e.currentOwner + "]");
			}
			
			this.digitalBoards = digitalBoards.toArray(new DigitalLightControlBoard[ digitalBoards.size() ]);
			this.dimmerBoards = dimmerBoards.toArray(new DimmerLightControlBoard[ dimmerBoards.size() ]);
		}
	}
}
