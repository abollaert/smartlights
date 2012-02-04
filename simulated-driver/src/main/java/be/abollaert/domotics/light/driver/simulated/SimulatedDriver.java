package be.abollaert.domotics.light.driver.simulated;

import java.io.IOException;
import java.util.List;

import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.driver.base.AbstractDriver;
import be.abollaert.domotics.light.driver.base.Channel;

public final class SimulatedDriver extends AbstractDriver {

	@Override
	public void unload() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Mood> getAllMoods() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mood getNewMood(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mood getMoodWithID(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeMood(int id) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void allLightsOff() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void probe() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected List<Channel> searchChannels() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
