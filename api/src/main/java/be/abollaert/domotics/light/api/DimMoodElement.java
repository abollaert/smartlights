package be.abollaert.domotics.light.api;

public interface DimMoodElement extends MoodElement {

	/**
	 * Returns the target percentage.
	 * 
	 * @return	The target percentage.
	 */
	int getTargetPercentage();
	
	/**
	 * Sets the target percentage.
	 * 
	 * @param 	percentage		The target percentage.
	 */
	void setTargetPercentage(final int percentage);

}