package be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer;

import java.io.IOException;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.LightControlBoard;

/**
 * Dimmer light control board, implements dimmer functionality.
 * 
 * @author alex
 */
public interface DimmerLightControlBoard extends LightControlBoard {

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
	void switchLight(final int lightIndex, final boolean on) throws IOException;
	
	/**
	 * Sets the percentage of the given light.
	 * 
	 * @param 	lightIndex		The index of the light for which to set the percentage.
	 * @param 	percentage		The percentage it should be set to.
	 */
	void setLightPercentage(final int lightIndex, final int percentage) throws IOException;
	
	/**
	 * Returns the percentage the light has been dimmed to.
	 * 
	 * @param 	lightIndex	The index of the light for which to return the percentage.
	 * 
	 * @return	The percentage the light is currently dimmed to.
	 */
	int getLightPercentage(final int lightIndex);
	
	/**
	 * Adds the given listener to the list of state change listeners.
	 * 
	 * @param 	listener	The listener that should be added to the list of state change listeners.
	 */
	void addStateChangeListener(final DimmerLightControllerStateListener listener);
	
	/**
	 * Removes the given listener from the list of state change listeners.
	 * 
	 * @param 	listener	The listener that should be removed from the list of state change listeners.
	 */
	void removeStateChangeListener(final DimmerLightControllerStateListener listener);
}
