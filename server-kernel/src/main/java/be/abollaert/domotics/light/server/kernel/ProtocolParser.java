package be.abollaert.domotics.light.server.kernel;


import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser for the protocol.
 * 
 * @author alex
 */
final class ProtocolParser {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(ProtocolParser.class.getName());
	
	/** The start of a PDU. */
	static final char PDU_START = '{';
	
	/** The end of a PDU. */
	static final char PDU_END = '}';
	
	/**
	 * PDU class. We parse the data into this class.
	 * 
	 * @author alex
	 */
	static final class RequestPDU {
		
		/** Enumerates the types. */
		enum Type {
			GET_TYPE(1), 
			GET_ID(2),
			GET_FW_VERSION(3),
			GET_NR_CHANNELS(4),
			GET_FEATURES(5),
			GET_SW_THRESHOLD(20),
			GET_SW_TIMER(21),
			GET_CHANNEL_MAPPING(22),
			GET_DEFAULT_STATE(23),
			GET_DIMMER_THRESHOLD(24),
			GET_DEFAULT_PERCENTAGE(25),
			GET_DIMMER_DELAY(26),
			GET_DEFAULT_DIMMER_DIRECTION(27),
			SET_SW_THRESHOLD(50),
			SET_SW_TIMER(51),
			SET_CHANNEL_MAPPING(52),
			SET_DEFAULT_STATE(53),
			SET_DIMMER_THRESHOLD(54),
			SET_DEFAULT_PERCENTAGE(55),
			SET_DIMMER_DELAY(56),
			SET_DEFAULT_DIMMER_DIRECTION(57),
			SET_MODULE_ID(58),
			SAVE_CONFIGURATION(70),
			RELOAD_CONFIGURATION(71),
			GET_OUTPUT_STATE(80),
			GET_INPUT_STATE(81),
			SWITCH_OUTPUT(90),
			DIM(91);
			
			private final int typeId;
			
			/**
			 * Create a new instance.
			 * 
			 * @param typeId
			 */
			private Type(final int typeId) {
				this.typeId = typeId;
			}
			
			/**
			 * Returns the {@link Type} that corresponds to the given type ID.
			 * 
			 * @param 		typeId		The type ID.
			 * 
			 * @return		The corresponding type.
			 */
			static final Type byTypeId(final int typeId) {
				for (final Type type : Type.values()) {
					if (type.typeId == typeId) {
						return type;
					}
				}
				
				return null;
			}
		}
		
		/** The type. */
		private final Type type;
		
		/** The arguments. */
		private final int[] arguments;
		
		/**
		 * Create a new {@link RequestPDU}.
		 * 
		 * @param 	type	The type of the PDU.
		 */
		RequestPDU(final Type type, int... arguments) {
			this.type = type;
			this.arguments = arguments;
		}
	}
	
	/**
	 * Response PDU.
	 * 
	 * @author alex
	 */
	static final class ResponsePDU {
		
		/** Enumerates the types. */
		enum Type {
			OK(0), 
			ERROR(1), 
			OUTPUT_CHANNEL_STATE_CHANGE(5), 
			INPUT_CHANNEL_STATE_CHANGE(6), 
			CONFIGURATION_CHANGED(7);
			
			private final int typeId;
			
			/**
			 * Create a new instance.
			 * 
			 * @param typeId
			 */
			private Type(final int typeId) {
				this.typeId = typeId;
			}
			
			/**
			 * Returns the {@link Type} that corresponds to the given type ID.
			 * 
			 * @param 		typeId		The type ID.
			 * 
			 * @return		The corresponding type.
			 */
			static final Type byTypeId(final int typeId) {
				for (final Type type : Type.values()) {
					if (type.typeId == typeId) {
						return type;
					}
				}
				
				return null;
			}
		}
		
		/** The type. */
		private final Type type;
		
		/** The arguments. */
		private final int[] arguments;
		
		/** The raw response. */
		private final String rawResponse;
		
		private ResponsePDU(final String rawResponse, final Type type, final int... arguments) {
			this.type = type;
			this.arguments = arguments;
			this.rawResponse = rawResponse;
		}
		
		final Type getType() {
			return this.type;
		}
		
		final int[] getArguments() {
			return this.arguments;
		}
		
		final String getRawResponse() {
			return this.rawResponse;
		}
	}
	
	/** Going to do this statically as we are basically stateless. */
	private ProtocolParser() {
	}
	
	/**
	 * Serialize the PDU to a protocol command.
	 * 
	 * @param 		requestPDU		The request PDU.
	 * 
	 * @return		The protocol command.
	 */
	static final String serialize(final RequestPDU requestPDU) {
		final StringBuilder builder = new StringBuilder();
		builder.append(PDU_START);
		builder.append(requestPDU.type.typeId);
		builder.append(",");
		builder.append(requestPDU.arguments.length);
		
		if (requestPDU.arguments.length > 0) {
			builder.append(",");
			
			int i = 0;
			
			for (i = 0; i < requestPDU.arguments.length - 1; i++) {
				builder.append(requestPDU.arguments[i]).append(",");
			}
			
			builder.append(requestPDU.arguments[i]);
		}
		
		builder.append(PDU_END);
		
		return builder.toString();
	}
	
	/**
	 * Parses the protocol response into a PDU.
	 * 
	 * @param 		responsePDU		The response PDU.
	 * 
	 * @return		The parsed response.
	 */
	static final ResponsePDU parse(final String protocolResponse) {
		// Does not contain the start and end of a PDU.
		if (protocolResponse == null) {
			throw new NullPointerException("protocolResponse parameter cannot be null here !");
		}
		
		final String[] stringParts = protocolResponse.split(",");
		final int[] intParts = new int[stringParts.length];
		
		for (int i = 0; i < stringParts.length; i++) {
			try {
				intParts[i] = Integer.parseInt(stringParts[i]);
			} catch (NumberFormatException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Cannot parse protocol part [" + stringParts[i] + "] into a number [" + e.getMessage() + "]", e);
				}
				
				return null;
			}
		}
		
		if (intParts.length < 2) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Cannot parse protocol response [" + protocolResponse + "], parsed response has only [" + intParts.length + "] parts");
			}
			
			return null;
		}
		
		final int responseTypeId = intParts[0];
		final int numberOfArguments = intParts[1];
		final int[] arguments = new int[numberOfArguments];
		
		if (numberOfArguments != intParts.length - 2) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Number of arguments in PDU [" + numberOfArguments + "] mismatches with the real number of arguments [" + (intParts.length - 2) + "]");
			}
			
			return null;
		} else {
			for (int i = 0; i < numberOfArguments; i++) {
				arguments[i] = intParts[i + 2];
			}
		}
		
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "Response type ID [" + responseTypeId + "], number of arguments [" + numberOfArguments + "], arguments are [" + Arrays.toString(arguments) + "]");
		}
		
		final ResponsePDU.Type type = ResponsePDU.Type.byTypeId(responseTypeId);
		
		if (type == null) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Could not parse protocol response [" + protocolResponse + "], could not map response type [" + responseTypeId + "]");
			}
			
			return null;
		}
		
		return new ResponsePDU("{" + protocolResponse + "}", type, arguments);
	}
}
