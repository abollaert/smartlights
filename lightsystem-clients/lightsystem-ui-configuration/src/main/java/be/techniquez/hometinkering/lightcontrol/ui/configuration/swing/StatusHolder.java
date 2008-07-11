package be.techniquez.hometinkering.lightcontrol.ui.configuration.swing;

/**
 * Status holder.
 * 
 * @author alex
 *
 */
public interface StatusHolder {

	/**
	 * Called by other components to signify status has been changed.
	 * 
	 * @param 	message		The message that should get displayed.
	 */
	void statusChange(final String message);
}
