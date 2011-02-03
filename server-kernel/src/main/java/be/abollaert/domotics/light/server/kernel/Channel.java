package be.abollaert.domotics.light.server.kernel;


import java.io.IOException;
import java.io.InputStream;

import be.abollaert.domotics.light.server.kernel.ProtocolParser.RequestPDU;
import be.abollaert.domotics.light.server.kernel.ProtocolParser.ResponsePDU;

/**
 * Channel interface.
 * 
 * @author alex
 */
public interface Channel {

	/**
	 * Connect the channel.
	 * 
	 * @throws 	IOException		If an IO error occurs while connecting.
	 */
	void connect() throws IOException;

	/**
	 * Probes for the type. 
	 * 
	 * @return	The type, null if we could not find out.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	ChannelType probeType() throws IOException;

	/**
	 * Sends the given command and returns the response.
	 * 
	 * @param		command		The command to send.
	 * 
	 * @return		The response to the command.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	ResponsePDU sendCommand(final RequestPDU command) throws IOException;

	/**
	 * Disconnect the channel.
	 */
	void disconnect();

	/**
	 * Indicates whether the channel is connected.
	 * 
	 * @return	<code>true</code> if the channel is connected, false if it is not.
	 */
	boolean isConnected();

	void addEventListener(final CommunicationChannelEventListener eventListener);

	void removeEventListener(final CommunicationChannelEventListener listener);
	
	String getName();
	
	/**
	 * This is an <b>optional</b> method to upgrade a node. It is not implemented by all drivers.
	 * 
	 * @param 	moduleId			The module ID.
	 * @param 	hexFileStream		The hex file stream.
	 * 
	 * @throws 	IOException
	 * @throws 	UnsupportedOperationException
	 */
	void upgrade(final int moduleId, final InputStream hexFileStream) throws IOException, UnsupportedOperationException;
	
	/**
	 * Check if this channel supports upgrades.
	 * 
	 * @return	<code>true</code> if the {@link Channel} supports upgrading, <code>false</code> if it does not
	 */
	boolean supportsUpgrade();

}