package be.abollaert.domotics.light.servers.rest.resources;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import be.abollaert.domotics.light.servers.rest.model.SystemStatus;

/**
 * Returns the status of the system.
 * 
 * @author alex
 */
@Path("/status")
@Produces("application/json")
public final class StatusResource {

	@GET
	public final String getStatus() {
		final SystemStatus status = new SystemStatus("1.2.3");
		
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(status);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
