package be.abollaert.domotics.zigbee.zstack;

/**
 * Interface definition for a ZStack module.
 * 
 * @author alex
 */
public interface ZStackModule {
	
	void connect() throws ZStackException;
	
	<REQ extends ZStackRequest, RESP extends ZStackResponse> void sendRequest(final REQ request, final RESP response) throws ZStackException;
}
