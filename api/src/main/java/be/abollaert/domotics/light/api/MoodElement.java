package be.abollaert.domotics.light.api;

public interface MoodElement {

	/**
	 * Returns the module ID.
	 * 
	 * @return	The module ID.
	 */
	int getModuleId();

	/**
	 * Returns the channel number.
	 * 
	 * @return	The channel number.
	 */
	int getChannelNumber();
}