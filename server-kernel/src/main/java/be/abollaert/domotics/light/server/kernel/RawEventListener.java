package be.abollaert.domotics.light.server.kernel;

/**
 * Implemented by a raw event listener. This is used by code that uses passthrough (tunneling).
 * 
 * @author alex
 */
public interface RawEventListener {

	/**
	 * Called when an event has been received.
	 * 
	 * @param 	rawEvent		A raw event.
	 */
	void eventReceived(final String rawEvent);
}
