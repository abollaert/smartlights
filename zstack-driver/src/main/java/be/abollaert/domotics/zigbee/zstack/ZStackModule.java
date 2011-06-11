package be.abollaert.domotics.zigbee.zstack;

/**
 * Interface definition for a ZStack module.
 * 
 * @author alex
 */
public interface ZStackModule {
	
	void connect() throws ZStackException;
	
	String getFirmwareVersion() throws ZStackException;

	void disconnect() throws ZStackException;
}
