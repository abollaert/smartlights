package be.abollaert.domotics.light.api;



/**
 * Implemented by classes that want to be notified by channel state changes.
 * 
 * @author alex
 */
public interface DigitalChannelStateChangeListener {
	
	/**
	 * Notifies interested parties that a channel has changed state. This interface can be used for input events as well as output events.
	 * 
	 * @param 	channelNumber		The channel number.
	 * @param 	newState			The new state.
	 */
	void outputChannelStateChanged(final int channelNumber, final ChannelState newState);
	
	/**
	 * Notifies interested parties that a channel has changed state. This interface can be used for input events as well as output events.
	 * 
	 * @param 	channelNumber		The channel number.
	 * @param 	newState			The new state.
	 */
	void inputChannelStateChanged(final int channelNumber, final ChannelState newState);
}
