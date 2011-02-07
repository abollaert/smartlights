package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

final class ActivateMoodHandler extends AbstractHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** Activates a Mood. */
	private static final String URI = "/api/ActivateMood";

	@Override
	final Message processRequest(final Message request) throws IOException {
		final Api.ActivateMood message = (Api.ActivateMood)request;
		
		final Mood mood = this.getDriver().getMoodWithID(message.getMoodId());
		
		if (mood != null) {
			mood.activate();
		}
		
		return createOKResponse();
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
	final Builder createRequestBuilder() {
		return Api.ActivateMood.newBuilder();
	}

}
