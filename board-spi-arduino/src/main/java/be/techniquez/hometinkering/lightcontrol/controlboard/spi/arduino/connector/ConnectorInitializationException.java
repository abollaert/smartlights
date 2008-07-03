package be.techniquez.hometinkering.lightcontrol.controlboard.spi.arduino.connector;

/**
 * Gets thrown when we cannot initialize the connector properly.
 * 
 * @author alex
 */
public final class ConnectorInitializationException extends RuntimeException {

	/**
	 * Creates a new instance, indicating the cause of the error.
	 * 
	 * @param 	cause	The cause of the error.
	 */
	ConnectorInitializationException(final Throwable cause) {
		super("Cannot initialize the connector properly due to [" + cause.getClass().getSimpleName() + "], [" + cause.getMessage() + "]", cause);
	}
}
