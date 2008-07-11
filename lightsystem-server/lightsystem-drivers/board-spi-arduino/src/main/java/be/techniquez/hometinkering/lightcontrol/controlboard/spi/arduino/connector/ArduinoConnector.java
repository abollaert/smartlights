package be.techniquez.hometinkering.lightcontrol.controlboard.spi.arduino.connector;

import gnu.io.CommPort;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControllerStateListener;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControllerStateListener;

/**
 * Base class for the arduino boards, handles some common issues like sending commands,
 * getting board information and such.
 * 
 * @author alex
 */
public final class ArduinoConnector implements DimmerLightControlBoard, DigitalLightControlBoard {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(ArduinoConnector.class.getName());
	
	/** The command terminator. */
	private static final String COMMAND_TERMINATOR = "#";
	
	/** Number format used for dim value. */
	private static final NumberFormat DIMVALUE_NUMBER_FORMAT = new DecimalFormat("000");
	
	/**
	 * Enumerates the types of boards we have.
	 * 
	 * @author 	alex
	 */
	public enum BoardType {
		DIGITAL(1), DIMMER(2);
		
		/** The type identifier. */
		private final int typeID;
		
		/**
		 * Creates a new instance for the given type ID.
		 * 
		 * @param 	typeId	The type ID.
		 */
		private BoardType(final int typeId) {
			this.typeID = typeId;
		}
		
		/**
		 * Returns the type ID of this board type.
		 * 
		 * @return	The type ID for this board type.
		 */
		public final int getTypeID() {
			return this.typeID;
		}
		
		/**
		 * Returns the board type that corresponds to the given type ID.
		 * 
		 * @param 	typeId	The ID of the board this type corresponds to.
		 * 
		 * @return	The board type that corresponds to the given type ID.
		 */
		public static final BoardType byTypeID(final int typeId) {
			for (final BoardType boardType : BoardType.values()) {
				if (boardType.getTypeID() == typeId) {
					return boardType;
				}
			}
			
			return null;
		}
	}
	
	/** 
	 * Enumerates the opcodes used, these are the same for both types of boards, and 
	 * declared here. 
	 */
	private enum OpCodes {
		LIGHT_ON("1"), 
		LIGHT_OFF("0"), 
		GET_STATE("2"), 
		GET_VERSION("3"), 
		GET_TYPE("4"),
		GET_ID("5"),
		SET_DIM_VALUE("6"),
		GET_NUM_CHANNELS("7");
		
		/** The opcode for this particular instance. */
		private final String opCode;
		
		/** Constructs a new instance. */
		private OpCodes(final String code) {
			this.opCode = code;
		}
		
		/**
		 * Returns the opcode that should be sent to the board.
		 * 
		 * @return	The opcode that should be sent to the board.
		 */
		public final String getOpCode() {
			return this.opCode;
		}
	}
	
	/** The input stream through which we stream the commands. */
	private final DataInputStream inputStream;
	
	/** The output stream through which we read the board feedback. */
	private final DataOutputStream outputStream;
	
	/** The semaphore to lock around. */
	private final Object semaphore = new Object();
	
	/** The board ID. */
	private final int boardId;
	
	/** The number of channels. */
	private final int numberOfChannels;
	
	/** The type of board. */
	private final BoardType boardType;
	
	/** The states of the lights. */
	private final boolean[] lightStates;
	
	/** The dimmer values of the lights. */
	private final int[] dimmerValues;
	
	/** The board version. */
	private final int boardVersion;
	
	/** Contains the set of digital controller state listeners. */
	private final Set<DigitalLightControllerStateListener> digitalListeners = new HashSet<DigitalLightControllerStateListener>();
	
	/** Contains the set of dimmable controller state listeners. */
	private final Set<DimmerLightControllerStateListener> dimmerListeners = new HashSet<DimmerLightControllerStateListener>();
	
	/** Will be used to schedule feedback readings. */
	private final Timer feedbackTimer = new Timer();
	
	/** The comm port we use to communicate. */
	private final CommPort commPort;
	
	/**
	 * Creates a new board using the given comm port to communicate through.
	 * 
	 * @param 	commPort	The port through which we communicate with the board.
	 */
	public ArduinoConnector(final CommPort commPort) {
		// Setup the communication streams...
		try {
			this.commPort = commPort;
			this.inputStream = new DataInputStream(commPort.getInputStream());
			
			// Flush the input stream...
			while (this.inputStream.available() > 0) {
				logger.info("Skipping...");
				this.inputStream.read();
			}
			
			this.outputStream = new DataOutputStream(commPort.getOutputStream());
			
			this.boardType = this.loadBoardTypeIdentifier();
			this.boardId = this.loadBoardID();
			this.boardVersion = this.loadBoardVersion();
			this.numberOfChannels = this.loadNumberOfChannels();
			
			this.lightStates = new boolean[this.numberOfChannels];
			this.dimmerValues = new int[this.numberOfChannels];
			
			this.synchronizeState(this.loadState());
			
			this.feedbackTimer.schedule(new TimerTask() {
				public final void run() {
					final String feedback = readSpontaneousFeedbackFromBoard();

					if (feedback != null) {
						synchronizeState(feedback);
					}
				}
			}, 0, 200);
		} catch (IOException e) {
			throw new ConnectorInitializationException(e);
		}
	}
	
	/**
	 * Returns the board type.
	 * 
	 * @return	The board type.
	 */
	public final BoardType getBoardType() {
		return this.boardType;
	}
	
	/**
	 * Sends the given command on the serial line of the device line.
	 * 
	 * @param 	command		The command that should be sent to the device.
	 * 
	 * @throws 	IOException		If an IO error occurs during the interchange.
	 */
	private final String sendCommand(final String command) throws IOException {
		synchronized(this.semaphore) {
			try {
				final byte[] commandBytes = command.getBytes("US-ASCII");
				
				logger.info("Sending command [" + command + "]");
				
				this.outputStream.write(commandBytes);
				this.outputStream.flush();
				
				logger.info("Reading response from board...");
				
				// Read the output from the command.
				final StringBuilder response = new StringBuilder();
				
				byte currentByte = this.inputStream.readByte();
				
				while (currentByte != 35) {
					response.append((char)currentByte);
					currentByte = this.inputStream.readByte();
				}
				
				logger.info("Done, response is [" + response.toString() + "]");
				
				return response.toString();
			} catch (UnsupportedEncodingException e) {
				logger.severe("WTF, our system does not support US-ASCII !");
				return null;
			}
		}
	}
	
	/**
	 * Reads spontaneous feedback from board. This will usually happen when the board
	 * decides to send feedback when a switch has been pressed or a dimmer value has
	 * been adjusted.
	 * 
	 * @return	Spontaneous feedback, null if none.
	 */
	private final String readSpontaneousFeedbackFromBoard() {
		synchronized(semaphore) {
			try {
				return this.sendCommand("20000#");
			} catch (IOException e) {
				logger.log(Level.WARNING, "Could not read state from board...", e);
			}
		}
		
		return null;
	}
	
	/**
	 * Checks if the given light is on. Returns true if it is, false if it is not.
	 * 
	 * @param 	lightIndex	The index of the light.
	 */
	public final boolean isLightOn(final int lightIndex) {
		assertLightIndexValid(lightIndex);
		
		return this.lightStates[lightIndex];
	}
	
	/**
	 * Gets the ID of the board.
	 * 
	 * @return	The ID of the board.
	 */
	public final int getBoardId() {
		return this.boardId;
	}
	
	/**
	 * Loads the board ID.
	 * 
	 * @return	The board ID.
	 */
	private final int loadBoardID() throws IOException {
		final StringBuilder command = new StringBuilder(OpCodes.GET_ID.opCode);
		command.append("0000");
		command.append(COMMAND_TERMINATOR);
		
		return Integer.valueOf(this.sendCommand(command.toString()));
	}
	
	/**
	 * Loads the state string.
	 * 
	 * @return	The state string.
	 * 
	 * @throws 	IOException		If an IO error occurs during the load.
	 */
	private final String loadState() throws IOException {
		final StringBuilder command = new StringBuilder(OpCodes.GET_STATE.opCode);
		command.append("0000");
		command.append(COMMAND_TERMINATOR);
		
		return this.sendCommand(command.toString());
	}
	
	/**
	 * Loads the board version. 
	 * 
	 * @return	The board version.
	 */
	private final int loadBoardVersion() throws IOException {
		final StringBuilder command = new StringBuilder(OpCodes.GET_VERSION.opCode);
		command.append("0000");
		command.append(COMMAND_TERMINATOR);
		
		return Integer.valueOf(this.sendCommand(command.toString()));
	}
	/**
	 * Gets the number of channels on the board.
	 * 
	 * @return	The number of channels on the board.
	 */
	public final int getNumberOfChannels() {
		return this.numberOfChannels;
	}
	
	/**
	 * Loads the number of channels from the board.
	 * 
	 * @return	The number of channels as returned by the board.
	 * 
	 * @throws	IOException		If an error occurs during the load.
	 */
	private final int loadNumberOfChannels() throws IOException {
		final StringBuilder command = new StringBuilder(OpCodes.GET_NUM_CHANNELS.opCode);
		command.append("0000");
		command.append(COMMAND_TERMINATOR);
		
		return Integer.valueOf(this.sendCommand(command.toString()));
	}
	
	/**
	 * Returns the board type ID from the board.
	 * 
	 * @return	The board type ID.
	 */
	private final BoardType loadBoardTypeIdentifier() throws IOException {
		final StringBuilder command = new StringBuilder(OpCodes.GET_ID.opCode);
		command.append("0000");
		command.append(COMMAND_TERMINATOR);
		
		return BoardType.byTypeID(Integer.valueOf(this.sendCommand(command.toString())));
	}
	
	/**
	 * Calculates the dim value to be sent to the board out of a percentage.
	 * 
	 * @param 	percentage	The percentage to calculate the dim value from.
	 * 
	 * @return	The dim value to be sent to the board.
	 */
	private final int calculateDimValueOutOfPercentage(final int percentage) {
		int dimValue = (int)Math.round(percentage * 2.55);
		
		if (dimValue > 255) {
			dimValue = 255;
		} else if (dimValue < 0) {
			dimValue = 0;
		}
		
		return dimValue;
	}
	
	/**
	 * Calculates the percentage that needs to be set out of the dim value received from the
	 * board.
	 * 
	 * @param 	dimValue	The dim value received from the board.
	 * 
	 * @return	The percentage that corresponds to the dim value.
	 */
	private final int calculatePercentageOutOfDimValue(final int dimValue) {
		int percentage = (int)Math.round(dimValue / 2.55);
		
		if (percentage > 100) {
			percentage = 100;
		} else if (percentage < 0) {
			percentage = 0;
		}
		
		return percentage;
	}
	
	
	/**
	 * Asserts that the provided index is within valid bounds.
	 * 
	 * @param 	lightIndex	The index that should be checked.
	 */
	private final void assertLightIndexValid(final int lightIndex) {
		if (lightIndex < 0 || lightIndex > this.numberOfChannels - 1) {
			throw new IllegalArgumentException("Index has to be between 0 and " + (this.numberOfChannels - 1) + ", you supplied [" + lightIndex + "]");
		}
	}
	
	/**
	 * Synchronizes the internal state with the board using the state string returned
	 * by the board.
	 * 
	 * @param 	stateString		The state string.
	 */
	private final void synchronizeState(final String stateString) {
		for (int i = 0; i < this.numberOfChannels; i++) {
			final char stateCharacter = stateString.charAt(i * 4);
			
			if (stateCharacter == '0') {
				if (this.lightStates[i]) {
					this.lightStates[i] = false;
					
					this.notifyLightSwitchedOff(i);
				}
			} else {
				if (!this.lightStates[i]) {
					this.lightStates[i] = true;
					
					this.notifyLightSwitchedOn(i);
				}
			}
			
			final int dimValue = Integer.valueOf(stateString.substring((i * 4 + 1), (i * 4 + 1) + 3));
			
			this.dimmerValues[i] = calculatePercentageOutOfDimValue(dimValue);
			
			this.notifyDimmerValueChanged(i);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addStateChangeListener(final DimmerLightControllerStateListener listener) {
		this.dimmerListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getLightPercentage(int lightIndex) {
		assertLightIndexValid(lightIndex);
		
		return this.dimmerValues[lightIndex];
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeStateChangeListener(final DimmerLightControllerStateListener listener) {
		this.dimmerListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setLightPercentage(final int lightIndex, final int percentage) throws IOException {
		this.assertLightIndexValid(lightIndex);
		
		logger.info("Adjusting the dim value of light [" + lightIndex + "] to [" + percentage + "] %");
		
		if (this.boardType.equals(BoardType.DIGITAL)) {
			throw new IllegalStateException("Cannot set dimmer value on a digital board !");
		}
		
		final StringBuilder command = new StringBuilder(OpCodes.SET_DIM_VALUE.opCode);
		command.append(lightIndex);
		command.append(DIMVALUE_NUMBER_FORMAT.format(this.calculateDimValueOutOfPercentage(percentage)));
		command.append(COMMAND_TERMINATOR);
		
		this.synchronizeState(this.sendCommand(command.toString()));
		
		logger.info("Done, adjusted the dimmmer value of channel [" + lightIndex + "] to [" + percentage + "] %");
	}

	/**
	 * {@inheritDoc}
	 */
	public final void switchLight(final int lightIndex, final boolean on) throws IOException {
		assertLightIndexValid(lightIndex);
		
		logger.info("Switching on light on channel [" + lightIndex + "]");
		
		if (on) {
			if (this.isLightOn(lightIndex)) {
				logger.info("Light [" + lightIndex + "] is already on, ignoring request");
			} else {
				final StringBuilder command = new StringBuilder(OpCodes.LIGHT_ON.opCode);
				command.append(lightIndex);
				command.append(DIMVALUE_NUMBER_FORMAT.format(0));
				command.append(COMMAND_TERMINATOR);
				
				this.synchronizeState(this.sendCommand(command.toString()));
				
				logger.info("Done, switched light [" + lightIndex + "] to on");
			}
		} else {
			if (!this.isLightOn(lightIndex)) {
				logger.info("Light [" + lightIndex + "] is already off, ignoring request");
			} else {
				final StringBuilder command = new StringBuilder(OpCodes.LIGHT_OFF.opCode);
				command.append(lightIndex);
				command.append(DIMVALUE_NUMBER_FORMAT.format(0));
				command.append(COMMAND_TERMINATOR);
				
				this.synchronizeState(this.sendCommand(command.toString()));
				
				logger.info("Done, switched light [" + lightIndex + "] to off");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getBoardID() {
		return this.boardId;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getBoardVersion() {
		return this.boardVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addStateEventListener(final DigitalLightControllerStateListener listener) {
		this.digitalListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeStateEventListener(final DigitalLightControllerStateListener listener) {
		this.digitalListeners.remove(listener);
	}
	
	/**
	 * Notifies the listeners that the dimmer value for the given dimmer light has changed.
	 * 
	 * @param 	lightIndex	The index of the light for which the percentage has changed.
	 */
	private final void notifyDimmerValueChanged(final int lightIndex) {
		if (this.boardType.equals(BoardType.DIMMER)) {
			for (final DimmerLightControllerStateListener listener : this.dimmerListeners) {
				listener.percentageChanged(this.boardId, lightIndex, this.dimmerValues[lightIndex]);
			}
		}
	}
	
	/**
	 * Notifies that the light has been switched on.
	 * 
	 * @param 	lightIndex	The index of the light.
	 */
	private final void notifyLightSwitchedOn(final int lightIndex) {
		if (this.boardType.equals(BoardType.DIGITAL)) {
			for (final DigitalLightControllerStateListener listener : this.digitalListeners) {
				listener.lightSwitchedOn(this.boardId, lightIndex);
			} 
 		} else {
 			for (final DimmerLightControllerStateListener listener : this.dimmerListeners) {
 				listener.lightSwitchedOn(this.boardId, lightIndex);
 			}
 		}
	}
	
	/**
	 * Notifies that the given light has been switched off.
	 * 
	 * @param 	lightIndex	The index of the light.
	 */
	private final void notifyLightSwitchedOff(final int lightIndex) {
		if (this.boardType.equals(BoardType.DIGITAL)) {
			for (final DigitalLightControllerStateListener listener : this.digitalListeners) {
				listener.lightSwitchedOff(this.boardId, lightIndex);
			} 
 		} else {
 			for (final DimmerLightControllerStateListener listener : this.dimmerListeners) {
 				listener.lightSwitchedOff(this.boardId, lightIndex);
 			}
 		}
	}
	
	/**
	 * Destroys the connector.
	 */
	public final void destroy() {
		try {
			this.inputStream.close();
		} catch (IOException e) {	
		}
		
		try {
			this.outputStream.close();
		} catch (IOException e) {
		}
		
		this.commPort.close();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getDriverName() {
		return "Arduino";
	}
}
