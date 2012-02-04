package be.abollaert.domotics.light.server.kernel;


import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.driver.base.RequestPDU;
import be.abollaert.domotics.light.driver.base.ResponsePDU;

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
		builder.append(requestPDU.getType().getTypeId());
		builder.append(",");
		builder.append(requestPDU.getArguments().length);
		
		if (requestPDU.getArguments().length > 0) {
			builder.append(",");
			
			int i = 0;
			
			for (i = 0; i < requestPDU.getArguments().length - 1; i++) {
				builder.append(requestPDU.getArguments()[i]).append(",");
			}
			
			builder.append(requestPDU.getArguments()[i]);
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
		
		return new ResponsePDU(type, arguments);
	}
}
