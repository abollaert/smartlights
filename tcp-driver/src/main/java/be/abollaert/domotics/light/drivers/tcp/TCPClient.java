package be.abollaert.domotics.light.drivers.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DigitalModuleConfiguration;
import be.abollaert.domotics.light.api.DimMoodElement;
import be.abollaert.domotics.light.api.DimmerDirection;
import be.abollaert.domotics.light.api.DimmerInputChannelConfiguration;
import be.abollaert.domotics.light.api.DimmerModuleConfiguration;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.api.SwitchMoodElement;
import be.abollaert.domotics.light.protocolbuffers.Api;
import be.abollaert.domotics.light.protocolbuffers.Api.GetDigitalModuleConfigResponse;
import be.abollaert.domotics.light.protocolbuffers.Api.MessageResponse;

import com.google.protobuf.Message;

/**
 * The TCP client class provides access to the API.
 * 
 * @author alex
 */
public final class TCPClient {
	
	/** The URI for the GetModules request. */
	private static final String URI_GET_MODULES = "/api/GetModules";
	
	/** URI for the GetDigitalChannelConfig request. */
	private static final String URI_GET_DIGITAL_CHANNEL_CONFIG = "/api/GetDigitalInputChannelConfig";
	
	/** URI for the SwitchOutput request. */
	private static final String URI_SWITCH_OUTPUT ="/api/SwitchOutput";
	
	/** The URI to get the output channels. */
	private static final String URI_GET_OUTPUT_CHANNELS = "/api/GetOutputChannels";
	
	/** The URL to set an input channel configuration. */
	private static final String URI_SET_DIGITAL_INPUT_CONFIG = "/api/SetDigitalInputChannelConfig";
	
	/** The URL to set a digital module configuration. */
	private static final String URI_SET_DIGITAL_MODULE_CONFIG = "/api/SetDigitalModuleConfig";
	
	/** THe URL for saving the configuration of a {@link DigitalModule}. */
	private static final String URI_SAVE_DIGITAL_MODULE_CONFIG = "/api/SaveModuleConfig";
	
	/** URI to get the digital module configuration. */
	private static final String URI_GET_DIGITAL_MODULE_CONFIG = "/api/GetDigitalModuleConfiguration";
	
	/** URI used to get dimmer input channel configuration. */
	private static final String URI_GET_DIMMER_INPUT_CHANNEL_CONFIG = "/api/GetDimmerInputChannelConfig";
	
	/** URI for saving a mood. */
	private static final String URI_SAVE_MOOD = "/api/SaveMood";
	
	/** The URI for dimming. */
	private static final String URI_DIM = "/api/Dim";
	
	/** URI to set the dimmer module config. */
	private static final String URI_SET_DIMMER_MODULE_CONFIG = "/api/SetDimmerModuleConfig";
	
	/** URI to set the dimmer input configuration. */
	private static final String URI_SET_DIMMER_INPUT_CONFIG = "/api/SetDimmerInputConfiguration";
	
	/** URI to get the switch requests. */
	private static final String URI_GET_SWITCH_EVENTS = "/api/GetSwitchEvents";
	
	/** URI for getting the output state of a digital channel. */
	private static final String URI_GET_DIGITAL_OUTPUT_STATE = "/api/GetDigitalOutputState";
	
	/** URI for getting the output state of a dimmer channel. */
	private static final String URI_GET_DIMMER_OUTPUT_STATE = "/api/GetDimmerOutputState";
	
	/** URI for getting all moods. */
	private static final String URI_GET_MOODS = "/api/GetMoods";
	
	private static final String URI_ACTIVATE_MOOD = "/api/ActivateMood";
	
	private static final String URI_REMOVE_MOOD = "/api/RemoveMood";
	
	/** The HTTP client. */
	private HttpClient httpClient;
	
	/** The event listener. */
	private EventListener eventListener;
	
	/** The server address. */
	private String serverAddress;
	
	/** The server port. */
	private int port;
	
	/** The http client lock. */
	private final Lock httpClientLock = new ReentrantLock();
	
	/**
	 * Connects the client.
	 * 
	 * @param 	serverAddress		The server address.
	 * @param 	port				The port.
	 * 
	 * @throws 	IOException			If an IO error occurs during the connect.
	 */
	public void connect(final String serverAddress, final int port, final InetAddress iface) throws IOException {
		this.serverAddress = serverAddress;
		this.port = port;
		
		this.httpClient = new HttpClient();
		
		this.eventListener = new EventListener();
		this.eventListener.start(iface);
	}
	
	/**
	 * Disconnects the client.
	 * 
	 * @throws 	IOException		If an IO error occurs while disconnecting.
	 */
	public void disconnect() throws IOException {
		this.eventListener.stop();
		this.eventListener = null;
		
		this.httpClient = null;
	}
	
	/**
	 * Gets the modules from the server.
	 * 
	 * @return		The modules from the server. 
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	public final Api.GetModulesResponse getModules() throws IOException {
		return (Api.GetModulesResponse)this.execute(URI_GET_MODULES, null, Api.GetModulesResponse.newBuilder());
	}
	
	/**
	 * Returns the digital channel config for the requested channel.
	 * 
	 * @param 	moduleId			The module ID.
	 * @param 	channelNumber		The channel number.
	 * 
	 * @return	The config.
	 * 
	 * @throws 	IOException
	 */
	public final Api.GetDigitalInputChannelConfigResponse getDigitalChannelConfiguration(final int moduleId, final int channelNumber) throws IOException {
		final Api.GetDigitalInputChannelConfig.Builder builder = Api.GetDigitalInputChannelConfig.newBuilder();
		builder.setModuleId(moduleId);
		builder.setChannelNumber(channelNumber);
		
		return (Api.GetDigitalInputChannelConfigResponse)this.execute(URI_GET_DIGITAL_CHANNEL_CONFIG, builder.build(), Api.GetDigitalInputChannelConfigResponse.newBuilder());
	}
	
	public final void switchOutput(final int moduleId, final int channelNumber, final ChannelState desiredState) throws IOException {
		final Api.SwitchOutput.Builder builder = Api.SwitchOutput.newBuilder();
		builder.setModuleId(moduleId);
		builder.setChannelNumber(channelNumber);
		builder.setRequiredState(desiredState == ChannelState.ON ? true : false);
		
		this.executeVoidMessage(URI_SWITCH_OUTPUT, builder.build());
	}
	
	/**
	 * Returns the switch events for the given module ID and channel number.
	 * 
	 * @param 	moduleId
	 * @param 	channelNumber
	 * @param 	startDate
	 * @param 	endDate
	 * @return
	 * @throws IOException
	 */
	public final Api.SwitchEventList getSwitchEvents(final int moduleId, final int channelNumber, final Date startDate, final Date endDate) throws IOException {
		final Api.GetSwitchEvents.Builder requestBuilder = Api.GetSwitchEvents.newBuilder();
		
		requestBuilder.setModuleId(moduleId);
		requestBuilder.setChannelNumber(channelNumber);
		
		if (startDate != null) {
			requestBuilder.setStartDate(startDate.getTime());
		}
		
		if (endDate != null) {
			requestBuilder.setEndDate(endDate.getTime());
		}
		
		return (Api.SwitchEventList)this.execute(URI_GET_SWITCH_EVENTS, requestBuilder.build(), Api.SwitchEventList.newBuilder());
	}
	
	/**
	 * Dim the given channel.
	 * 
	 * @param 	moduleId			The module ID.
	 * @param	channelNumber		The channel number.
	 * @param 	percentage			The percentage.
	 * 
	 * @throws 	IOException			If an IO error occurs during the action.
	 */
	public final void dim(final int moduleId, final int channelNumber, final int percentage) throws IOException {
		final Api.Dim.Builder messageBuilder = Api.Dim.newBuilder();
		messageBuilder.setModuleId(moduleId);
		messageBuilder.setChannelNumber(channelNumber);
		messageBuilder.setPercentage(percentage);
		
		this.executeVoidMessage(URI_DIM, messageBuilder.build());
	}
	
	public final Api.GetOutputChannelsResponse getOutputChannels() throws IOException {
		try {
			return (Api.GetOutputChannelsResponse)this.execute(URI_GET_OUTPUT_CHANNELS, null, Api.GetOutputChannelsResponse.newBuilder());
		} catch (HttpException e) {
			throw new IOException("HTTP error while getting modules : [" + e.getMessage() + "]");
		}
	}
	
	/**
	 * Set the configuration for a particular digital input channel.
	 * 
	 * @param 		newConfiguration		The new configuration.
	 * 
	 * @return		The response.
	 * 
	 * @throws		 IOException		If an IO error occurs during the set.
	 */
	public final void setDigitalInputChannelConfiguration(final DigitalInputChannelConfiguration newConfiguration) throws IOException {
		final Api.DigitalInputChannelConfig.Builder configurationBuilder = Api.DigitalInputChannelConfig.newBuilder();
		configurationBuilder.setCurrentOutputState(false);
		configurationBuilder.setCurrentSwitchState(false);
		configurationBuilder.setDefaultState(newConfiguration.getDefaultState() == ChannelState.ON ? true : false);
		configurationBuilder.setMappedOutputChannel(newConfiguration.getMappedOutputChannel());
		configurationBuilder.setTimerInSec(newConfiguration.getTimerInSeconds());
		
		if (newConfiguration.getName() != null) {
			configurationBuilder.setName(newConfiguration.getName());
		}
		
		configurationBuilder.setEnableLogging(newConfiguration.isLoggingEnabled());
		
		final Api.SetDigitalInputConfig.Builder messageBuilder = Api.SetDigitalInputConfig.newBuilder();
		messageBuilder.setModuleId(newConfiguration.getModuleId());
		messageBuilder.setChannelNumber(newConfiguration.getChannelNumber());
		messageBuilder.setConfig(configurationBuilder);
		
		this.executeVoidMessage(URI_SET_DIGITAL_INPUT_CONFIG, messageBuilder.build());
	}
	
	/**
	 * Sets the configuration for a dimmer module.
	 * 
	 * @param 		newConfiguration		The new configuration.
	 * 
	 * @throws 		IOException				If an IO error occurs while setting.
	 */
	public final void setDimmerModuleConfiguration(final DimmerModuleConfiguration newConfiguration) throws IOException {
		final Api.DimmerModuleConfig.Builder configBuilder = Api.DimmerModuleConfig.newBuilder();
		
		configBuilder.setDimmerDelay(newConfiguration.getDimmerDelay());
		configBuilder.setDimmerThresholdInMs(newConfiguration.getDimmerThreshold());
		configBuilder.setSwitchThresholdInMs(newConfiguration.getSwitchThreshold());
		
		final Api.SetDimmerModuleConfig.Builder messageBuilder = Api.SetDimmerModuleConfig.newBuilder();
		messageBuilder.setConfiguration(configBuilder);
		messageBuilder.setModuleId(newConfiguration.getModuleId());
		
		this.executeVoidMessage(URI_SET_DIMMER_MODULE_CONFIG, messageBuilder.build());
	}
	
	/**
	 * Sets the configuration for a dimmer input.
	 * 
	 * @param 		newConfiguration		The new configuration.
	 * 
	 * @throws 		IOException				If an IO error occurs while setting.
	 */
	public final void setDimmerInputConfiguration(final int moduleId, final int channelNumber, final DimmerInputChannelConfiguration newConfiguration) throws IOException {
		final Api.DimmerInputChannelConfig.Builder configBuilder = Api.DimmerInputChannelConfig.newBuilder();
		
		configBuilder.setCurrentDimmerPercentage(0);
		configBuilder.setCurrentOutputState(false);
		configBuilder.setCurrentSwitchState(false);
		configBuilder.setDefaultDirection(newConfiguration.getDefaultDirection() == DimmerDirection.UP ? true : false);
		configBuilder.setDefaultPercentage(newConfiguration.getDefaultPercentage());
		configBuilder.setDefaultState(newConfiguration.getDefaultState() == ChannelState.ON ? true : false);
		configBuilder.setMappedOutputChannel(newConfiguration.getMappedOutputChannel());
		configBuilder.setTimerInSec(newConfiguration.getTimerInSeconds());
		
		if (newConfiguration.getName() != null) {
			configBuilder.setName(newConfiguration.getName());
		}
		
		configBuilder.setEnableLogging(newConfiguration.isLoggingEnabled());
		
		final Api.SetDimmerInputConfig.Builder messageBuilder = Api.SetDimmerInputConfig.newBuilder();
		messageBuilder.setConfig(configBuilder);
		messageBuilder.setModuleId(moduleId);
		messageBuilder.setChannelNumber(channelNumber);
		
		this.executeVoidMessage(URI_SET_DIMMER_INPUT_CONFIG, messageBuilder.build());
	}
	
	/**
	 * Sets the new digital module configuration.
	 * 
	 * @param 		newConfiguration		The new configuration.
	 * 
	 * @return		The response.
	 * 
	 * @throws 		IOException				If an IO error occurs.
	 */
	public final void setDigitalModuleConfiguration(final DigitalModuleConfiguration newConfiguration) throws IOException {
		final Api.DigitalModuleConfig.Builder configurationBuilder = Api.DigitalModuleConfig.newBuilder();
		configurationBuilder.setSwitchThresholdInMs(newConfiguration.getSwitchThreshold());
		
		final Api.SetDigitalModuleConfig.Builder messageBuilder = Api.SetDigitalModuleConfig.newBuilder();
		messageBuilder.setModuleId(newConfiguration.getModuleId());
		messageBuilder.setConfiguration(configurationBuilder);
		
		this.executeVoidMessage(URI_SET_DIGITAL_MODULE_CONFIG, messageBuilder.build());
	}
 	
	/**
	 * Saves a digital module configuration.
	 * 
	 * @param 		moduleId		The module ID.
	 * 
	 * @return		The response.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	public final void saveModuleConfiguration(final int moduleId) throws IOException {
		final Api.SaveDigitalModuleConfig.Builder messageBuilder = Api.SaveDigitalModuleConfig.newBuilder();
		messageBuilder.setModuleId(moduleId);
		
		this.executeVoidMessage(URI_SAVE_DIGITAL_MODULE_CONFIG, messageBuilder.build());
	}
	
	public final int saveMood(final Mood mood) throws IOException {
		final Api.Mood.Builder moodBuilder = Api.Mood.newBuilder();
		moodBuilder.setName(mood.getName());
		moodBuilder.setMoodId(mood.getId());
		
		for (final SwitchMoodElement switchElement : mood.getSwitchMoodElements()) {
			final Api.SwitchMoodElement.Builder elementBuilder = Api.SwitchMoodElement.newBuilder();
			
			elementBuilder.setModuleId(switchElement.getModuleId());
			elementBuilder.setChannelNumber(switchElement.getChannelNumber());
			elementBuilder.setRequestedState(switchElement.getRequestedState() == ChannelState.ON);
			
			moodBuilder.addSwitchElements(elementBuilder.build());
		}
		
		for (final DimMoodElement dimmerElement : mood.getDimMoodElements()) {
			final Api.DimmerMoodElement.Builder elementBuilder = Api.DimmerMoodElement.newBuilder();
			
			elementBuilder.setModuleId(dimmerElement.getModuleId());
			elementBuilder.setChannelNumber(dimmerElement.getChannelNumber());
			elementBuilder.setPercentage(dimmerElement.getTargetPercentage());
			
			moodBuilder.addDimmerElements(elementBuilder.build());
		}
		
		final Api.SaveMood.Builder requestBuilder = Api.SaveMood.newBuilder();
		requestBuilder.setMood(moodBuilder.build());
		
		return ((Api.SaveMoodResponse)this.execute(URI_SAVE_MOOD, requestBuilder.build(), Api.SaveMoodResponse.newBuilder())).getMoodId();
	}
	
	/**
	 * REturns the configuration of a digital module.
	 * 
	 * @param 		moduleId		The ID of the module.
	 * 
	 * @return		The configuration.
	 * 
	 * @throws 		IOException
	 */
	public final Api.DigitalModuleConfig getDigitalModuleConfig(final int moduleId) throws IOException {
		final Api.GetDigitalModuleConfig.Builder messageBuilder = Api.GetDigitalModuleConfig.newBuilder();
		messageBuilder.setModuleId(moduleId);
		
		final GetDigitalModuleConfigResponse response = (Api.GetDigitalModuleConfigResponse)this.execute(URI_GET_DIGITAL_MODULE_CONFIG, messageBuilder.build(), Api.GetDigitalModuleConfigResponse.newBuilder());
		return response.getConfig();
	}
	
	/**
	 * Returns the event listener.
	 * 
	 * @return	The event listener.
	 */
	public final EventListener getEventListener() {
		return this.eventListener;
	}
	
	/**
	 * Generates the URL for the given URI.
	 * 
	 * @param 		uri		The URI to generate an URL for.
	 * 
	 * @return		The URL to use.
	 */
	private final String generateURL(final String uri) {
		return new StringBuilder("http://").append(this.serverAddress).append(":").append(this.port).append(uri).toString();
	}
	
	/**
	 * Get the configuration of an input channel on a dimmer module.
	 * 
	 * @param 		moduleId			The ID of the module.
	 * @param 		channelNumber		The channel number.
	 * 
	 * @return		The configuration of the channel.
	 * 		
	 * @throws 		IOException			If an IO error occurs.
	 */
	public final Api.DimmerInputChannelConfig getDimmerInputChannelConfiguration(final int moduleId, final int channelNumber) throws IOException {
		final Api.GetDimmerInputChannelConfig.Builder requestMessageBuilder = Api.GetDimmerInputChannelConfig.newBuilder();
		requestMessageBuilder.setModuleId(moduleId);
		requestMessageBuilder.setChannelNumber(channelNumber);
		
		final Api.GetDimmerInputChannelConfigResponse response = (Api.GetDimmerInputChannelConfigResponse)this.execute(URI_GET_DIMMER_INPUT_CHANNEL_CONFIG, requestMessageBuilder.build(), Api.GetDimmerInputChannelConfigResponse.newBuilder());
		return response.getConfig();
	}
	
	public final Api.DigitalChannelOutputState getDigitalChannelOutputState(final int moduleId, final int channelNumber) throws IOException {
		final Api.GetOutputChannelState.Builder requestBuilder = Api.GetOutputChannelState.newBuilder();
		requestBuilder.setModuleId(moduleId);
		requestBuilder.setChannelNumber(channelNumber);
		
		return (Api.DigitalChannelOutputState)this.execute(URI_GET_DIGITAL_OUTPUT_STATE, requestBuilder.build(), Api.DigitalChannelOutputState.newBuilder());
	}
	
	public final Api.DimmerChannelOutputState getDimmerChannelOutputState(final int moduleId, final int channelNumber) throws IOException {
		final Api.GetOutputChannelState.Builder requestBuilder = Api.GetOutputChannelState.newBuilder();
		requestBuilder.setModuleId(moduleId);
		requestBuilder.setChannelNumber(channelNumber);
		
		return (Api.DimmerChannelOutputState)this.execute(URI_GET_DIMMER_OUTPUT_STATE, requestBuilder.build(), Api.DimmerChannelOutputState.newBuilder());
	}
	
	/**
	 * Executes the given message against the given URI, and returns the reult message if any.
	 * 
	 * @param 	uri					The URI.
	 * @param 	message				The message.
	 * @param 	responseBuilder		The response builder.
	 * 
	 * @return	The response message.
	 * 
	 * @throws 	IOException			If an IO error occurs.
	 */
	private final Message execute(final String uri, final Message message, final Message.Builder responseBuilder) throws IOException {
		PostMethod method = new PostMethod(this.generateURL(uri));
		
		try {
			this.httpClientLock.lock();
			
			if (message != null) {
				method.setRequestEntity(new ByteArrayRequestEntity(message.toByteArray()));
			}
			
			this.httpClient.executeMethod(method);
			
			final InputStream responseBodyStream = method.getResponseBodyAsStream();
			
			if (responseBodyStream != null) {
				responseBuilder.mergeFrom(responseBodyStream);
			}
			
			return responseBuilder.build();
		} finally {
			method.releaseConnection();
			this.httpClientLock.unlock();
		}
	}
	
	private final void executeVoidMessage(final String uri, final Message message) throws IOException {
		final Api.MessageResponse response = (Api.MessageResponse)this.execute(uri, message, MessageResponse.newBuilder());
		
		if (response.getType() == Api.MessageResponse.Type.ERROR) {
			throw new IOException(response.getMessage());
		}
	}
	
	public final Api.MoodList getAllMoods() throws IOException {
		return (Api.MoodList)this.execute(URI_GET_MOODS, null, Api.MoodList.newBuilder());
	}
	
	public final void activateMood(final int moodId) throws IOException {
		final Api.ActivateMood.Builder requestBuilder = Api.ActivateMood.newBuilder();
		requestBuilder.setMoodId(moodId);
		
		this.executeVoidMessage(URI_ACTIVATE_MOOD, requestBuilder.build());
	}
	
	public final void removeMood(final int moodId) throws IOException {
		final Api.RemoveMood.Builder requestBuilder = Api.RemoveMood.newBuilder();
		requestBuilder.setMoodId(moodId);
		
		this.executeVoidMessage(URI_REMOVE_MOOD, requestBuilder.build());
	}
}
