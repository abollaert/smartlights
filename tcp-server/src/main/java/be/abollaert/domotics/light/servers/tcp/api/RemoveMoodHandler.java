package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Removes a {@link Mood}.
 * 
 * @author alex
 *
 */
final class RemoveMoodHandler extends AbstractHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** URI. */
	private static final String URI = "/api/RemoveMood";

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Message processRequest(final Message request) throws IOException {
		final Api.RemoveMood message = (Api.RemoveMood)request;
		
		this.getDriver().removeMood(message.getMoodId());
		
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
		return Api.RemoveMood.newBuilder();
	}

}
