package be.abollaert.domotics.light.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Dimmer module API.
 * 
 * @author alex
 */
public interface DimmerModule {

	/**
	 * Returns the configuration of this module.
	 * 
	 * @return The configuration of this module.
	 */
	DimmerModuleConfiguration getDimmerConfiguration();

	/**
	 * Returns the ID of the module.
	 * 
	 * @return The ID of the module.
	 */
	int getId();

	/**
	 * Gets the output channel state.
	 * 
	 * @param channelNumber
	 *            The channel number.
	 * 
	 * @return The output channel state.
	 */
	ChannelState getOutputChannelState(final int channelNumber)
			throws IOException;

	/**
	 * Gets the input channel state.
	 * 
	 * @param channelNumber
	 *            The channel number.
	 * 
	 * @return The input channel state.
	 */
	ChannelState getInputChannelState(final int channelNumber)
			throws IOException;

	/**
	 * Switches the output channel to the desired state.
	 * 
	 * @param channelNumber
	 *            The channel number.
	 * @param desiredState
	 *            The desired state.
	 */
	void switchOutputChannel(final int channelNumber,
			final ChannelState desiredState) throws IOException;

	/**
	 * Dim the channel to the desired percentage.
	 * 
	 * @param 	channelNumber			The channel number.
	 * @param 	desiredPercentage		The desired percentage.
	 * 
	 * @throws	IOException
	 */
	void dim(final int channelNumber, final short desiredPercentage) throws IOException;
	/**
	 * Adds the given listener to the set of listeners.
	 * 
	 * @param listener
	 *            The listener.
	 */
	void addChannelStateListener(final DimmerChannelStateChangeListener listener);

	/**
	 * Removes the given listener from the set.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	void removeChannelStateListener(final DimmerChannelStateChangeListener listener);
	
	/**
	 * Gets the dimmer percentage.
	 * 
	 * @param 	channelNumber
	 * 
	 * @return	The dimmer percentage.
	 * 
	 * @throws	IOException
	 */
	int getDimmerPercentage(final int channelNumber) throws IOException;
	
	/**
	 * Adds a listener for configuration changes.
	 * 
	 * @param 	listener		The listener.
	 */
	void addModuleConfigurationListener(final DimmerModuleConfigurationChangedListener listener);
	
	/**
	 * Removes a listener for configuration changes.
	 * 
	 * @param 	listener		The listener.
	 */
	void removeModuleConfigurationListener(final DimmerModuleConfigurationChangedListener listener);
	
	/**
	 * Returns the port name.
	 * 
	 * @return	The port name.
	 */
	String getPortName();
	
	/**
	 * Upgrade the module. This is an optional method because not all drivers support this.
	 * 
	 * @param 	hexFileStream		The hex file stream.
	 * @throws IOException 
	 */
	void upgrade(final InputStream hexFileStream) throws UnsupportedOperationException, IOException;

	/**
	 * Gets the switch log events for the given channel for the given period. startDate and endDate can both be null.
	 * 
	 * @param 	channelNumber		The channel number.
	 * @param 	startDate			The start date.
	 * @param 	endDate				The end date.
	 * 
	 * @return	The switch log events for the given channel and period.
	 */
	List<SwitchEvent> getSwitchEvents(final int channelNumber, final Date startDate, final Date endDate) throws IOException;
}
