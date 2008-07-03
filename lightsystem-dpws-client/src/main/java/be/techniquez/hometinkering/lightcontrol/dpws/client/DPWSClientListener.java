package be.techniquez.hometinkering.lightcontrol.dpws.client;

/**
 * Listener interface for the DPWS client.
 * 
 * @author alex
 */
public interface DPWSClientListener {

	/**
	 * Called when the client has finished the discovery process and is 
	 * fully initialized.
	 */
	void clientInitialized();
}
