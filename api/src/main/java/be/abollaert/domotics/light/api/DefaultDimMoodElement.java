package be.abollaert.domotics.light.api;

/**
 * Mood element for a dimmer.
 * 
 * @author alex
 */
public class DefaultDimMoodElement extends AbstractMoodElement implements DimMoodElement {

	/** The target percentage. */
	private int percentage;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	moduleId			The module ID.
	 * @param 	channelNumber		The channel number.
	 */
	public DefaultDimMoodElement(final int moduleId, final int channelNumber, final int targetPercentage) {
		super(moduleId, channelNumber);
		
		this.percentage = targetPercentage;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final int getTargetPercentage() {
		return this.percentage;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void setTargetPercentage(final int percentage) {
		this.percentage = percentage;
	}
}
