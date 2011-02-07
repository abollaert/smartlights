package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	/** Logger definition. */
	private static final Logger logger = Logger
			.getLogger(RemoveMoodHandler.class.getName());
	
	/** URI. */
	private static final String URI = "/api/RemoveMood";

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Message processRequest(final Message request) throws IOException {
		final Api.RemoveMood message = (Api.RemoveMood)request;
		
		final Mood mood = this.getDriver().getMoodWithID(message.getMoodId());
		
		if (mood == null) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Did not find a Mood with ID [" + message.getMoodId() + "], doing nothing.");
			}
			
			return createErrorResponse("Did not find a Mood with ID [" + message.getMoodId() + "]");
		}
		
		// FIXME: Not implemented yet : Need an API for this.
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
