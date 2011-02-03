package be.abollaert.domotics.light.api;


import java.io.IOException;

/**
 * Digital module configuration.
 * 
 * @author alex
 */
public interface DimmerModuleConfiguration {

	/**
	 * Returns the module ID.
	 * 
	 * @return	The module ID.
	 */
	int getModuleId() throws IOException;
	
	/**
	 * Returns the firmware version.
	 * 
	 * @return	The firmware version.
	 * @throws IOException 
	 */
	String getFirmwareVersion() throws IOException;
	
	/**
	 * Returns the number of channels.
	 * 
	 * @return
	 * @throws IOException 
	 */
	int getNumberOfChannels() throws IOException;
	
	/**
	 * @return the switchThreshold
	 * @throws IOException 
	 */
	int getSwitchThreshold() throws IOException;

	/**
	 * @param switchThreshold the switchThreshold to set
	 * @throws IOException 
	 */
	void setSwitchThreshold(final int switchThreshold) throws IOException;
	
	/**
	 * Set the dimmer threshold. This is the number of milliseconds to press the switch before it starts acting
	 * like a dimmer.
	 * 
	 * @param 		dimmerThreshold		The dimmer threshold.
	 * @throws 		IOException
	 */
	void setDimmerThreshold(final int dimmerThreshold) throws IOException;
	
	/**
	 * Get the dimmer threshold.
	 * 
	 * @throws 		IOException
	 * 
	 * @return 		The number of milliseconds to wait before switching into dimmer mode.
	 */
	int getDimmerThreshold() throws IOException;
	
	/**
	 * Sets the dimmer delay. This is the number of ms to wait before stepping the dimmer again.
	 * 
	 * @param 		dimmerThreshold		The dimmer threshold.
	 * @throws 		IOException			If an IO error occurs during the set.
	 */
	void setDimmerDelay(final int dimmerThreshold) throws IOException;
	
	/**
	 * Get the dimmer delay.
	 * 
	 * @return	The dimmer delay.
	 * 
	 * @throws 	IOException
	 */
	int getDimmerDelay() throws IOException;
	
	/**
	 * Saves this configuration.
	 */
	void save() throws IOException;
	
	/**
	 * Gets the channel configuration for the given channel.
	 * 
	 * @param 		channelNumber		The channel number.
	 * 
	 * @return		The channel configuration.
	 */
	DimmerInputChannelConfiguration getDimmerChannelConfiguration(final int channelNumber);
}
