package be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer;

/**
 * Listener interface implemented by applications that are interested in receiving
 * state changes from the control board (feedback).
 * 
 * @author alex
 */
public interface DimmerLightControllerStateListener {
	
	/**
	 * Called when the light with the given index has been switched on.
	 * 
	 * @param 	lightIndex	The index of the light that has been switched on.
	 */
	void lightSwitchedOn(final int boardId, final int lightIndex);
	
	/**
	 * Called when the light with the given index has been switched off.
	 * 
	 * @param 	lightIndex	The index of the light that has been switched off.
	 */
	void lightSwitchedOff(final int boardId, final int lightIndex);
	
	/**
	 * Called when the percentage of the light with the given index has been changed.
	 * 
	 * @param 	lightIndex		The index of the light for which the percentage has changed.
	 * @param 	newPercentage	The percentage it has changed to.
	 */
	void percentageChanged(final int boardId, final int lightIndex, final int newPercentage);
}
