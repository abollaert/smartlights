package be.abollaert.domotics.light.server.kernel.persistence.sqlite;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import be.abollaert.domotics.light.api.SwitchEvent;

/**
 * Tests the switch logs.
 * 
 * @author alex
 *
 */
public class SwitchLogTest extends AbstractStorageTest {

	/**
	 * Tests the addition of one event.
	 */
	@Test
	public void testAddEvent() {
		this.getStorage().logOnOffEvent(1, 1, true);
		
		final List<SwitchEvent> events = this.getStorage().getSwitchEventsForPeriod(1, 1, null, null);
		Assert.assertEquals(1, events.size());
	}
	
	@Test
	public void testSelectAll() {
		this.getStorage().logOnOffEvent(1, 1, true);
		this.getStorage().logOnOffEvent(1, 1, false);
		this.getStorage().logOnOffEvent(1, 1, true);
		this.getStorage().logOnOffEvent(1, 1, false);
		
		Assert.assertTrue(this.getStorage().getSwitchEventsForPeriod(1, 1, null, null).size() == 4);
	}
	
	@Test
	public void testWithStartDate() {
		this.getStorage().logOnOffEvent(1, 2, true);
		this.getStorage().logOnOffEvent(1, 2, false);
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		final Date startDate = new Date();
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.getStorage().logOnOffEvent(1, 2, true);
		this.getStorage().logOnOffEvent(1, 2, false);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		final Date endDate = new Date();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		this.getStorage().logOnOffEvent(1, 2, true);
		this.getStorage().logOnOffEvent(1, 2, false);
		
		Assert.assertTrue(this.getStorage().getSwitchEventsForPeriod(1, 2, startDate, endDate).size() == 2);
	}
	
}
