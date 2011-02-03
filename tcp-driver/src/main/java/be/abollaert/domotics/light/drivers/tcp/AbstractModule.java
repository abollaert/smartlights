package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.abollaert.domotics.light.api.SwitchEvent;
import be.abollaert.domotics.light.protocolbuffers.Api;

/**
 * Base class for the modules.
 * 
 * @author alex
 */
abstract class AbstractModule {

	/** The TCP client. */
	private final TCPClient tcpClient;
	
	/** The ID. */
	private final int id;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	tcpClient	The TCP client.
	 */
	AbstractModule(final int id, final TCPClient tcpClient) {
		this.id = id;
		this.tcpClient = tcpClient;
	}
	
	/**
	 * Return the TCP client.
	 * 
	 * @return	The TCP client.
	 */
	final TCPClient getTCPClient() {
		return this.tcpClient;
	}
	
	/**
	 * 
	 * @param channelNumber
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public final List<SwitchEvent> getSwitchEvents(int channelNumber, Date startDate, Date endDate) throws IOException {
		final Api.SwitchEventList apiEventList = this.getTCPClient().getSwitchEvents(this.id, channelNumber, startDate, endDate);
		final List<SwitchEvent> events = new ArrayList<SwitchEvent>();
		
		if (apiEventList != null) {
			for (final Api.SwitchEvent apiEvent : apiEventList.getEventsList()) {
				final SwitchEvent event = new SwitchEvent(apiEvent.getModuleId(), apiEvent.getChannelNumber(), new Date(apiEvent.getTimestamp()), apiEvent.getState());
				events.add(event);
			}
		}
		
		return events;
	}
}
