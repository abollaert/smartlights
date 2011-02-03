package be.abollaert.domotics.light.api;

import java.io.IOException;
import java.util.List;

/**
 * Interface specification for a Mood. This is a combination of multiple elements.
 * 
 * @author alex
 */
public interface Mood {
	
	/**
	 * Activates this mood.
	 * 
	 * @throws 	IOException		If an IO error occurs during the activation.
	 */
	void activate() throws IOException;
	
	/**
	 * Returns the name of the mood.
	 * 
	 * @return	The name of the mood.
	 */
	String getName();
	
	/**
	 * Returns the ID of the mood.
	 * 
	 * @return	The ID of the mood.
	 */
	int getId();
	
	/**
	 * Adds a switch element.
	 * 
	 * @param 	moduleId			The module ID.
	 * @param 	channelNumber		The channel number.
	 * @param 	state				The state.
	 */
	void addSwitchElement(final int moduleId, final int channelNumber, final ChannelState state);
	
	/**
	 * Adds a dimmer element.
	 * 
	 * @param 		moduleId		The module ID.
	 * @param 		channelNumber	The channel number.
	 * @param 		percentage		The target percentage.
	 */
	void addDimElement(final int moduleId, final int channelNumber, final int percentage);
	
	/**
	 * Returns the switch mood elements.
	 * 
	 * @return	The switch mood elements.
	 */
	List<SwitchMoodElement> getSwitchMoodElements();
	
	/**
	 * Returns the switch element that is defined on the given channel.
	 * 
	 * @param 	moduleId			The hosting module.
	 * @param 	channelNumber		The channel number.
	 * 
	 * @return	A {@link SwitchMoodElement} if one is defined for the channel, <code>null</code> if not.
	 */
	SwitchMoodElement getSwitchElementFor(final int moduleId, final int channelNumber);
	
	/**
	 * Returns the dimmer element that is defined on the given channel.
	 * 
	 * @param 	moduleId			The hosting module.
	 * @param 	channelNumber		The channel number.
	 * 
	 * @return	A {@link DimMoodElement} if one is defined for the channel, <code>null</code> if not.
	 */
	DimMoodElement getDimmerElementFor(final int moduleId, final int channelNumber);
	
	/**
	 * Returns the dimmer mood elements.
	 * 
	 * @return	The dimmer mood elements.
	 */
	List<DimMoodElement> getDimMoodElements();
	
	/**
	 * Remove the given switch element.
	 * 
	 * @param 	element		The element to remove.
	 */
	void removeSwitchElement(final int moduleId, final int channelNumber);
	
	/**
	 * Removes the given dimmer element.
	 * 
	 * @param 	element		The element to remove.
	 */
	void removeDimmerElement(final int moduleId, final int channelNumber);
	
	/**
	 * Sets the name of the mood.
	 * 
	 * @param	 name		The new name.
	 */
	void setName(final String name);
	
	/**
	 * Save the mood.
	 * 
	 * @throws 	IOException 	If an IO error occurs while saving.
	 */
	void save() throws IOException;
	
	/**
	 * Remove the mood.
	 */
	void remove();
	
	/**
	 * Set the ID of the mood.
	 * 
	 * @param 	id		The ID of the mood.
	 */
	void setId(final int id);
}
