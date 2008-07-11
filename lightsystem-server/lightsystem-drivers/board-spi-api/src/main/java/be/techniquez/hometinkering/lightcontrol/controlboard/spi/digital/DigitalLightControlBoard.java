package be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital;

import java.io.IOException;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.LightControlBoard;


/**
 * Specification for a digital light control board.
 * 
 * @author alex
 */
public interface DigitalLightControlBoard extends LightControlBoard {

	/**
	 * Returns true if the light with the given index is on, false if it is not.
	 * 
	 * @param 	lightIndex	The index of the light.
	 * 
	 * @return	True if the light is on, false if it is not.
	 */
	boolean isLightOn(final int lightIndex);

	/**
	 * Switches the light.
	 * 
	 * @param 	lightIndex	Index of the light to switch.
	 * 
	 * @param 	on	True if the light should be switched to on, false if it should be
	 * 				switched off.
	 */
	void switchLight(final int lightIndex, final boolean on)
			throws IOException;

	/**
	 * Adds the given listener to the list of parties interested in state changes.
	 * 
	 * @param 	listener		The listener to add to the list of interested parties.
	 */
	void addStateEventListener(
			final DigitalLightControllerStateListener listener);

	/**
	 * Removes the given state listener from the list of interested parties.
	 * 
	 * @param 	listener	The listener that needs to be removed from the list of interested parties.
	 */
	void removeStateEventListener(
			final DigitalLightControllerStateListener listener);

}