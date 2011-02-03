package be.abollaert.domotics.light.server.kernel.persistence.sqlite;

import junit.framework.Assert;

import org.junit.Test;

import be.abollaert.domotics.light.server.kernel.persistence.StoredChannelConfiguration;

/**
 * Test cases for the storage of channel configuration.
 * 
 * @author alex
 */
public final class ChannelConfigTest extends AbstractStorageTest {

	/**
	 * Tests the creation of a channel configuration.
	 */
	@Test
	public final void testCreateChannelConfig() {
		final StoredChannelConfiguration configuration = new StoredChannelConfiguration("testchannel", true);
		this.getStorage().saveChannelConfiguration(1, 1, configuration);
		
		assertDatabaseData(1, 1, configuration);
	}
	
	/**
	 * Tests the update of a channel configuration.
	 */
	@Test
	public final void testUpdateChannelConfig() {
		StoredChannelConfiguration configuration = new StoredChannelConfiguration("testchannel", true);
		this.getStorage().saveChannelConfiguration(1, 1, configuration);
		
		assertDatabaseData(1, 1, configuration);
		
		configuration = new StoredChannelConfiguration("testchannel_updated", false);
		this.getStorage().saveChannelConfiguration(1, 1, configuration);
		
		assertDatabaseData(1, 1, configuration);
	}
	
	/**
	 * Asserts the expected channel configuration is found in the database under said form.
	 * 
	 * @param 	expected		The expected configuration.
	 */
	private final void assertDatabaseData(final int moduleId, final int channelNumber, final StoredChannelConfiguration expected) {
		final StoredChannelConfiguration database = this.getStorage().loadChannelConfiguration(moduleId, channelNumber);
		
		Assert.assertEquals(database, expected);
	}
}
