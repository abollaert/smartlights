package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.SwitchEvent;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler for fetching switch events.
 * 
 * @author alex
 *
 */
final class GetSwitchEventsHandler extends AbstractHandler {
	
	/** The URI. */
	private static final String URI = "/api/GetSwitchEvents";

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.GetSwitchEvents.newBuilder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final String getURI() {
		return URI;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Message processRequest(final Message request) throws IOException {
		final Api.GetSwitchEvents message = (Api.GetSwitchEvents)request;
		
		Date startDate = null;
		
		if (message.hasStartDate()) {
			startDate = new Date(message.getStartDate());
		}
		
		Date endDate = null;
		
		if (message.hasEndDate()) {
			endDate = new Date(message.getEndDate());
		}
		
		DigitalModule digitalModule = this.getDriver().getDigitalModuleWithID(message.getModuleId());
		DimmerModule dimmerModule = this.getDriver().getDimmerModuleWithID(message.getModuleId());
		
		List<SwitchEvent> events = null;
		
		if (digitalModule != null) {
			events = digitalModule.getSwitchEvents(message.getChannelNumber(), startDate, endDate);
		} else if (dimmerModule != null) {
			events = dimmerModule.getSwitchEvents(message.getChannelNumber(), startDate, endDate);
		}
		
		final Api.SwitchEventList.Builder listBuilder = Api.SwitchEventList.newBuilder();
		
		if (events != null) {
			for (final SwitchEvent event : events) {
				final Api.SwitchEvent.Builder eventBuilder = Api.SwitchEvent.newBuilder();
				
				eventBuilder.setModuleId(event.getModuleId());
				eventBuilder.setChannelNumber(event.getChannelNumber());
				eventBuilder.setTimestamp(event.getTimestamp().getTime());
				eventBuilder.setState(event.isOn());
				
				listBuilder.addEvents(eventBuilder);
			}
		}
		
		return listBuilder.build();
	}

}
