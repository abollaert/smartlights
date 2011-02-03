package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DimMoodElement;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.api.SwitchMoodElement;
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
		for (final Iterator<SwitchMoodElement> elementIterator = mood.getSwitchMoodElements().iterator(); elementIterator.hasNext(); ) {
			final SwitchMoodElement moodElement = elementIterator.next();
			
			boolean contains = false;
			
			for (final Iterator<Api.SwitchMoodElement> messageElementIterator = messageElements.iterator(); messageElementIterator.hasNext(); ) {
				final Api.SwitchMoodElement messageElement = messageElementIterator.next();
				
				if (messageElement.getChannelNumber() == moodElement.getChannelNumber() && messageElement.getModuleId() == moodElement.getModuleId()) {
					messageElementIterator.remove();
					contains = true;
				}
			}
			
			if (!contains) {
				elementIterator.remove();
			}
		}
		
		for (final Api.SwitchMoodElement element : messageElements) {
			mood.addSwitchElement(element.getModuleId(), element.getChannelNumber(), ChannelState.ON);
		}
	}
	
	private final void synchronizeDimmerElements(final Mood mood, final List<Api.DimmerMoodElement> messageElements) {
		for (final Iterator<DimMoodElement> elementIterator = mood.getDimMoodElements().iterator(); elementIterator.hasNext(); ) {
			final DimMoodElement moodElement = elementIterator.next();
			
			boolean contains = false;
			
			for (final Api.DimmerMoodElement messageElement : messageElements) {
				if (messageElement.getChannelNumber() == moodElement.getChannelNumber() && messageElement.getModuleId() == moodElement.getModuleId()) {
					moodElement.setTargetPercentage(messageElement.getPercentage());
					contains = true;
				}
			}
			
			if (!contains) {
				elementIterator.remove();
			}
		}
		
		for (final Api.DimmerMoodElement element : messageElements) {
			mood.addDimElement(element.getModuleId(), element.getChannelNumber(), element.getPercentage());
		}
	}
}
