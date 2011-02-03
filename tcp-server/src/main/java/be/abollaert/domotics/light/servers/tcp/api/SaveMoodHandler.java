package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * THe handler for saving moods.
 * 
 * @author alex
 *
 */
final class SaveMoodHandler extends AbstractHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(SaveMoodHandler.class
			.getName());
	
	/** The URI. */
	private static final String URI = "/api/SaveMood";

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.SaveMood.newBuilder();
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
		try {
			final Api.SaveMood message = (Api.SaveMood)request;
			
			Mood mood = this.getDriver().getMoodWithID(message.getMood().getMoodId());
			
			if (mood == null) {
				mood = this.getDriver().getNewMood(message.getMood().getName());
			}
			
			mood.setName(message.getMood().getName());
			
			this.synchronizeDimmerElements(mood, message.getMood().getDimmerElementsList());
			this.synchronizeSwitchElements(mood, message.getMood().getSwitchElementsList());
			
			mood.save();
			
			final Api.SaveMoodResponse.Builder responseBuilder = Api.SaveMoodResponse.newBuilder();
			responseBuilder.setMoodId(mood.getId());
			
			return responseBuilder.build();
		} catch (RuntimeException e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, "Could not save Mood due to a runtime error [" + e.getMessage() + "], e");
			}
			
			return createErrorResponse("Could not save Mood due to a runtime error [" + e.getMessage() + "]");
		}
	}
	
	private final void synchronizeSwitchElements(final Mood mood, final List<Api.SwitchMoodElement> messageElements) {
		mood.getSwitchMoodElements().clear();
		
		for (final Api.SwitchMoodElement element : messageElements) {
			mood.addSwitchElement(element.getModuleId(), element.getChannelNumber(), element.getRequestedState() ? ChannelState.ON : ChannelState.OFF);
		}
	}
	
	private final void synchronizeDimmerElements(final Mood mood, final List<Api.DimmerMoodElement> messageElements) {
		mood.getDimMoodElements().clear();
		
		for (final Api.DimmerMoodElement element : messageElements) {
			mood.addDimElement(element.getModuleId(), element.getChannelNumber(), element.getPercentage());
		}
	}
}
