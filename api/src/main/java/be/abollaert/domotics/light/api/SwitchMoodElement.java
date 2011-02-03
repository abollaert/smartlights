package be.abollaert.domotics.light.api;

public interface SwitchMoodElement extends MoodElement {

	/**
	 * Returns the requested state.
	 * 
	 * @return	The requested state.
	 */
	ChannelState getRequestedState();

}