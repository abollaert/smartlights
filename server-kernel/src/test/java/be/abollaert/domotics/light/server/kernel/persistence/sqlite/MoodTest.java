package be.abollaert.domotics.light.server.kernel.persistence.sqlite;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.server.kernel.persistence.StoredDimMoodElement;
import be.abollaert.domotics.light.server.kernel.persistence.StoredMoodInfo;
import be.abollaert.domotics.light.server.kernel.persistence.StoredSwitchMoodElement;

/**
 * Tests for the storage of moods.
 * 
 * @author alex
 *
 */
public class MoodTest extends AbstractStorageTest {

	@Test
	public void testAddMood() {
		final String name = "TestMood" + System.currentTimeMillis();
		
		final StoredMoodInfo info = this.getStorage().saveMoodInformation(-1, name);
		
		Assert.assertTrue("Check if added mood exists", this.getStorage().loadMoodInformationFor(info.getId()) != null);
	}
	
	@Test
	public void testUpdateMood() {
		final String name = "TestMood" + System.currentTimeMillis();
		
		final StoredMoodInfo dbInformation = this.getStorage().saveMoodInformation(-1, name);
		
		final String newName = "TestMood" + System.currentTimeMillis();
		
		this.getStorage().saveMoodInformation(dbInformation.getId(), newName);
		
		Assert.assertTrue("Check if update went correctly", this.getStorage().loadMoodInformationFor(dbInformation.getId()).getName().equals(newName));
	}
	
	@Test
	public void testAddDimElement() {
		final StoredMoodInfo dbInformation = this.getStorage().saveMoodInformation(-1, "TestMood" + System.currentTimeMillis());
			
		this.getStorage().saveMoodDimElement(dbInformation.getId(), 1, 1, 75);
		Assert.assertTrue(this.checkDimMoodElement(dbInformation.getId(), 1, 1, 75));
		
		this.getStorage().saveMoodDimElement(dbInformation.getId(), 1, 2, 85);
		Assert.assertTrue(this.checkDimMoodElement(dbInformation.getId(), 1, 2, 85));
		
		Assert.assertTrue(this.getStorage().getDimElementsForMood(dbInformation.getId()).size() == 2);
		
		this.getStorage().removeMoodDimElement(dbInformation.getId(), 1, 1);
		
		Assert.assertTrue(this.getStorage().getDimElementsForMood(dbInformation.getId()).size() == 1);
		Assert.assertTrue("Should not have a dim element anymore", this.checkDimMoodElement(dbInformation.getId(), 1, 1, 75) == false);
	}
	
	private final boolean checkDimMoodElement(final int moodId, final int moduleId, final int channelNumber, final int percentage) {
		boolean exists = false;
		
		final List<StoredDimMoodElement> savedDimElements = this.getStorage().getDimElementsForMood(moodId);
		
		for (final StoredDimMoodElement element : savedDimElements) {
			if (element.getMoodId() == moodId && element.getModuleId() == moduleId && element.getChannelNumber() == channelNumber && element.getTargetPercentage() == percentage) {
				exists = true;
			}
		}
		
		return exists;
	}
	
	private final boolean checkSwitchMoodElement(final int moodId, final int moduleId, final int channelNumber, final ChannelState state) {
		boolean exists = false;
		
		final List<StoredSwitchMoodElement> savedSwitchElements = this.getStorage().getSwitchElementsForMood(moodId);
		
		for (final StoredSwitchMoodElement element : savedSwitchElements) {
			if (element.getMoodId() == moodId && element.getModuleId() == moduleId && element.getChannelNumber() == channelNumber && element.getRequestedState() == state) {
				exists = true;
			}
		}

		return exists;
	}
	
	@Test
	public void testAddSwitchElement() {
		final StoredMoodInfo dbInformation = this.getStorage().saveMoodInformation(-1, "TestMood" + System.currentTimeMillis());
		
		this.getStorage().saveMoodSwitchElement(dbInformation.getId(), 1, 1, ChannelState.ON);
		Assert.assertTrue(this.checkSwitchMoodElement(dbInformation.getId(), 1, 1, ChannelState.ON));
		
		this.getStorage().saveMoodSwitchElement(dbInformation.getId(), 1, 2, ChannelState.OFF);
		Assert.assertTrue(this.checkSwitchMoodElement(dbInformation.getId(), 1, 2, ChannelState.OFF));
		
		this.getStorage().saveMoodSwitchElement(dbInformation.getId(), 1, 2, ChannelState.ON);
		Assert.assertTrue(this.checkSwitchMoodElement(dbInformation.getId(), 1, 2, ChannelState.ON));
		
		Assert.assertTrue(this.getStorage().getSwitchElementsForMood(dbInformation.getId()).size() == 2);
		
		this.getStorage().removeMoodSwitchElement(dbInformation.getId(), 1, 1);
	
		Assert.assertTrue(this.getStorage().getSwitchElementsForMood(dbInformation.getId()).size() == 1);
		Assert.assertTrue("Should not have a switch element anymore", this.checkSwitchMoodElement(dbInformation.getId(), 1, 1, ChannelState.ON) == false);
	}
	
	@Test
	public void testRemoveMood() {
		final StoredMoodInfo dbInformation = this.getStorage().saveMoodInformation(-1, "TestMood" + System.currentTimeMillis());
		
		this.getStorage().saveMoodSwitchElement(dbInformation.getId(), 1, 1, ChannelState.ON);
		this.getStorage().saveMoodSwitchElement(dbInformation.getId(), 1, 2, ChannelState.OFF);
		this.getStorage().saveMoodDimElement(dbInformation.getId(), 1, 1, 75);
		this.getStorage().saveMoodDimElement(dbInformation.getId(), 1, 2, 85);
		
		Assert.assertTrue("Mood element exists.", this.getStorage().loadMoodInformationFor(dbInformation.getId()) != null);
		Assert.assertTrue("Mood has 2 switch elements.", this.getStorage().getSwitchElementsForMood(dbInformation.getId()).size() == 2);
		Assert.assertTrue("Mood has 2 dim elements.", this.getStorage().getDimElementsForMood(dbInformation.getId()).size() == 2);
		
		this.getStorage().removeMood(dbInformation.getId());
		
		Assert.assertTrue("Mood element is removed.", this.getStorage().loadMoodInformationFor(dbInformation.getId()) == null);
		Assert.assertTrue("No switch elements for mood.", this.getStorage().getSwitchElementsForMood(dbInformation.getId()).size() == 0);
		Assert.assertTrue("No dim elements for mood.", this.getStorage().getDimElementsForMood(dbInformation.getId()).size() == 0);
	}
	
	@Test(expected = SQLiteStorageException.class)
	public void testMoodFKSwitchElement() {
		this.getStorage().saveMoodSwitchElement(1, 1, 1, ChannelState.ON);
	}
	
	@Test(expected = SQLiteStorageException.class)
	public void testMoodFKDimElement() {
		this.getStorage().saveMoodDimElement(1, 1, 1, 100);
	}
}
