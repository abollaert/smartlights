package be.abollaert.domotics.light.service.sunsetsunrise;

import java.util.Date;

/**
 * Sunset sunrise calculator interface.
 * 
 * @author alex
 */
public interface SunsetSunriseCalculator {

	/**
	 * Gets the sunset time for the given date.
	 * 
	 * @param 	date			The date.
	 * @param 	longitude		The longitude.
	 * @param 	latitude		The latitude.
	 * 
	 * @return	The sunset date.
	 */
	Date getSunsetTime(final Date date, final float longitude, final float latitude);
	
	/**
	 * Gets the sunrise time for the given date.
	 * 
	 * @param 	date			The date.
	 * @param 	longitude		The longitude.
	 * @param 	latitude		The latitude.
	 * 
	 * @return	The sunrise date.
	 */
	Date getSunriseTime(final Date date, final float longitude, final float latitude);
}
