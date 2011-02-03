package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;
import java.util.List;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DimMoodElement;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.api.SwitchMoodElement;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler that fetches the whole list of moods.
 * 
 * @author alex
 *
 */
final class GetAllMoodsHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URI. */
	private static final String URI = "/api/GetMoods";
	
	/**
	 * Process the request.
	 */
	@Override
	final Message processRequest(final Message request) throws IOException {
		final List<Mood> moods = this.getDriver().getAllMoods();
		
		final Api.MoodList.Builder listBuilder = Api.MoodList.newBuilder();
		
		for (final Mood mood : moods) {
			final Api.Mood.Builder moodBuilder = Api.Mood.newBuilder();
			moodBuilder.setMoodId(mood.getId());
			moodBuilder.setName(mood.getName());
			
			for (final SwitchMoodElement element : mood.getSwitchMoodElements()) {
				final Api.SwitchMoodElement.Builder moodElementBuilder = Api.SwitchMoodElement.newBuilder();
				moodElementBuilder.setModuleId(element.getModuleId());
				moodElementBuilder.setChannelNumber(element.getChannelNumber());
				moodElementBuilder.setRequestedState(element.getRequestedState() == ChannelState.ON);
				
				moodBuilder.addSwitchElements(moodElementBuilder);
			}
			
			for (final DimMoodElement element : mood.getDimMoodElements()) {
				final Api.DimmerMoodElement.Builder moodElementBuilder = Api.DimmerMoodElement.newBuilder();
				moodElementBuilder.setModuleId(element.getModuleId());
				moodElementBuilder.setChannelNumber(element.getChannelNumber());
				moodElementBuilder.setPercentage(element.getTargetPercentage());
				
				moodBuilder.addDimmerElements(moodElementBuilder);
			}
			
			listBuilder.addMoods(moodBuilder);
		}
		
		return listBuilder.build();
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
		return null;
	}

}
