package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler that is responsible for returning all the modules on the system.
 * 
 * @author alex
 */
final class GetModulesHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final String getURI() {
		return "/api/GetModules";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Message processRequest(final Message request) throws IOException {
		final Api.GetModulesResponse.Builder responseBuilder = Api.GetModulesResponse.newBuilder();
		
		for (final DigitalModule digitalModule : this.getDriver().getAllDigitalModules()) {
			final Api.DigitalModule.Builder moduleBuilder = Api.DigitalModule.newBuilder();
			moduleBuilder.setModuleId(digitalModule.getId());
			moduleBuilder.setNumberOfChannels(digitalModule.getDigitalConfiguration().getNumberOfChannels());
			moduleBuilder.setFirmwareVersion(digitalModule.getDigitalConfiguration().getFirmwareVersion());
			
			final Api.DigitalModuleConfig.Builder configBuilder = Api.DigitalModuleConfig.newBuilder();
			configBuilder.setSwitchThresholdInMs(digitalModule.getDigitalConfiguration().getSwitchThreshold());
			
			moduleBuilder.setConfiguration(configBuilder);
			
			final Api.Module.Builder builder = Api.Module.newBuilder();
			builder.setType(Api.Module.Type.DIGITAL);
			builder.setDigitalModule(moduleBuilder);
			
			responseBuilder.addModules(builder);
		}
		
		for (final DimmerModule dimmerModule : this.getDriver().getAllDimmerModules()) {
			final Api.DimmerModule.Builder moduleBuilder = Api.DimmerModule.newBuilder();
			moduleBuilder.setModuleId(dimmerModule.getId());
			moduleBuilder.setNumberOfChannels(dimmerModule.getDimmerConfiguration().getNumberOfChannels());
			moduleBuilder.setFirmwareVersion(dimmerModule.getDimmerConfiguration().getFirmwareVersion());
			
			final Api.DimmerModuleConfig.Builder moduleConfigBuilder = Api.DimmerModuleConfig.newBuilder();
			moduleConfigBuilder.setSwitchThresholdInMs(dimmerModule.getDimmerConfiguration().getSwitchThreshold());
			moduleConfigBuilder.setDimmerThresholdInMs(dimmerModule.getDimmerConfiguration().getDimmerThreshold());
			moduleConfigBuilder.setDimmerDelay(dimmerModule.getDimmerConfiguration().getDimmerDelay());
			
			moduleBuilder.setConfiguration(moduleConfigBuilder);
			
			final Api.Module.Builder builder = Api.Module.newBuilder();
			builder.setType(Api.Module.Type.DIMMER);
			builder.setDimmerModule(moduleBuilder);
			
			responseBuilder.addModules(builder);
		}
		
		return responseBuilder.build();
	}
}
