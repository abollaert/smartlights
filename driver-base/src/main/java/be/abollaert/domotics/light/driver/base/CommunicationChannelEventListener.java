package be.abollaert.domotics.light.driver.base;

/**
 * Attached to the communication channel. Notifies all events.
 * 
 * @author alex
 *
 */
public interface CommunicationChannelEventListener {

	/**
	 * Called when an event has been received.
	 * 
	 * @param 	eventString		The event string.
	 */
	void eventReceived(final ResponsePDU responsePDU);
}
