package be.abollaert.domotics.light.server.kernel;


import be.abollaert.domotics.light.server.kernel.ProtocolParser.ResponsePDU;

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
