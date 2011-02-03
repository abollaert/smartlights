package be.abollaert.domotics.light.api;


import java.io.IOException;


/**
 * Input channel configuration.
 * 
 * @author alex
 */
public interface DimmerInputChannelConfiguration {	
	
	/**
	 * Returns the mapped output channel for this input channel.
	 * 
	 * @return	The mapped output channel for this input channel.
	 * @throws IOException 
	 */
	int getMappedOutputChannel() throws IOException;
	
	/**
	 * Sets the mapped output channel.
	 * 
	 * @param	The output channel.
	 * @throws IOException 
	 */
	void setMappedOutputChannel(final int outputChannel) throws IOException;
	
	/**
	 * Gets the timer in seconds.
	 * 
	 * @return	The timer in seconds. 0 means no timer active.
	 * @throws IOException 
	 */
	int getTimerInSeconds() throws IOException;
	
	/**
	 * Sets the timer in seconds.
	 * 		
	 * @param 	seconds		The number of seconds.
	 * @throws IOException 
	 */
	void setTimerInSeconds(final int seconds) throws IOException;
	
	/**
	 * Gets the default state.
	 * 
	 * @return		The default state.
	 * 
	 * @throws		IOException
	 */
	ChannelState getDefaultState() throws IOException;
	
	/**
	 * Sets the new default channel state.
	 * 
	 * @param 	state		The default channel state.
	 */
	void setDefaultState(final ChannelState state) throws IOException;
	
	int getDefaultPercentage() throws IOException;
	
	void setDefaultPercentage(final int percentage) throws IOException;
	
	DimmerDirection getDefaultDirection() throws IOException;
	
	void setDefaultDirection(final DimmerDirection direction) throws IOException;
	
	/**
	 * Returns the name of the channel.
	 * 
	 * @return	The name of the channel, <code>null</code> if the channel has no name.
	 */
	String getName();
	
	/**
	 * Returns true if logging is enabled, false if not.
	 * 
	 * @return	<code>true</code> if logging is enabled, <code>false</code> otherwise.
	 */
	boolean isLoggingEnabled();
	
	/**
	 * Sets the name.
	 * 
	 * @param 	name		The name.
	 */
	void setName(final String name);
	
	/**
	 * Enable logging.
	 * 
	 * @param 	loggingEnabled		If <code>true</code>, logging is enabled for this channel, if <code>false</code> it is not.
	 */
	void setLoggingEnabled(final boolean loggingEnabled);
}
