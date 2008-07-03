package be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital;

/**
 * Interface needs to be implemented by applications using the library to 
 * control the lights in the home.
 * 
 * @author alex
 */
public interface DigitalLightControllerStateListener {

	/**
	 * Called when a light with the given index has been switched on.
	 * 
	 * @param 	lightIndex	The index of the light that has been switched on.
	 */
	void lightSwitchedOn(final int boardId, final int lightIndex);
	
	/**
	 * Called when a light with the given index has been switched off.
	 * 
	 * @param 	lightIndex	The index of the light that has been switched off.
	 */
	void lightSwitchedOff(final int boardId, final int lightIndex);
}
