package be.abollaert.domotics.light.api;


import java.io.IOException;

/**
 * Digital module configuration.
 * 
 * @author alex
 */
public interface DigitalModuleConfiguration {

	/**
	 * Returns the module ID.
	 * 
	 * @return	The module ID.
	 */
	int getModuleId() throws IOException;
	
	/**
	 * Set the module ID.
	 * 
	 * @param 		moduleId		The new module ID.
	 * 
	 * @throws		IOException		If an IO error occurs while setting the module ID.
	 */
	void setModuleId(final int moduleId) throws IOException;
	
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
	DigitalInputChannelConfiguration getDigitalChannelConfiguration(final int channelNumber);
}
