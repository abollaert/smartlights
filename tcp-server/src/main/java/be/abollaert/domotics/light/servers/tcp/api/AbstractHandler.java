package be.abollaert.domotics.light.servers.tcp.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.protocolbuffers.Api;
import be.abollaert.domotics.light.protocolbuffers.Api.MessageResponse;

import com.google.protobuf.Message;

/**
 * Base class for the handlers.
 * 
 * @author alex
 */
abstract class AbstractHandler extends HttpServlet {

	/** Serial version UID. */
	private static final long serialVersionUID = 1L;

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(AbstractHandler.class.getName());
	
	/** The underlying driver. */
	private Driver driver;
	
	/**
	 * Processes the request. Returns the response.
	 * 
	 * @param 		request			The request to process.
	 * 
	 * @return		The response to the request.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	abstract Message processRequest(final Message request) throws IOException;
	
	/**
	 * Returns the URL on which the handler should be registered.
	 * 
	 * @return	The handler URI.
	 */
	abstract String getURI();
	
	/**
	 * Create a default request.
	 * 
	 * @return	A default request.
	 */
	abstract Message.Builder createRequestBuilder();
	
	/**
	 * Returns a reference to the {@link Driver}.
	 * 
	 * @return	A reference to the {@link Driver}.
	 */
	final Driver getDriver() {
		return this.driver;
	}
	
	/**
	 * Register the handler with the given {@link HttpContext}.
	 * 
	 * @param 	httpService		The {@link HttpService} to register with.
	 * @param	httpContext		The {@link HttpContext}.
	 * @param	driver			The {@link Driver}.
	 */
	final void register(final HttpService httpService, final HttpContext httpContext, final Driver driver) {
		this.driver = driver;
		
		try {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Registering [" + this.getClass().getName() + "]");
			}
			
			httpService.registerServlet(this.getURI(), this, null, httpContext);
		} catch (NamespaceException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final byte[] payload = getPayload(req);
		Message request = null;
		
		final Message.Builder builder = this.createRequestBuilder();
		
		if (builder != null && payload.length > 0) {
			builder.mergeFrom(payload);
			
			request = builder.build();
		}
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Handler [" + this.getClass().getName() + "] handling request.");
		}
		
		final Message response = this.processRequest(request);
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Handler [" + this.getClass().getName() + "] finished request, generated response of type [" + (response != null ? response.getClass().getName() : "NONE") + "]");
		}
		
		if (response != null) {
			response.writeTo(resp.getOutputStream());
			resp.getOutputStream().flush();
		}
		
		resp.getOutputStream().close();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		throw new ServletException("GET method is not supported !");
	}
	
	/**
	 * Gets the payload out of the request.
	 * 
	 * @param 		request		The request.
	 * 
	 * @return		The payload.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	private static final byte[] getPayload(final HttpServletRequest request) throws IOException {
		ByteArrayOutputStream outputStream = null;
		
		if (request.getInputStream() != null) {
			try {
				outputStream = new ByteArrayOutputStream();
				
				final byte[] buffer = new byte[2048];
				
				int numberOfBytesRead = 0;
				
				while (numberOfBytesRead != -1) {
					numberOfBytesRead = request.getInputStream().read(buffer);

					if (numberOfBytesRead != -1) {
						outputStream.write(buffer, 0, numberOfBytesRead);
						outputStream.flush();
					}
				}
				
				return outputStream.toByteArray();
			} finally {
				try {
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (IOException e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "IO error while closing output stream : [" + e.getMessage() + "]", e);
					}
				}
			}
		}
		
		return new byte[0];
	}
	
	/**
	 * Creates an error response message.
	 * 
	 * @param 		message		The message string.
	 * 
	 * @return		An error message to be serialized.
	 */
	static final MessageResponse createErrorResponse(final String message) {
		final Api.MessageResponse.Builder builder = Api.MessageResponse.newBuilder();
		builder.setType(Api.MessageResponse.Type.ERROR);
		builder.setMessage(message != null ? message : "No message.");
		
		return builder.build();
	}
	
	/**
	 * Creates an OK response.
	 * 
	 * @return	An OK response.
	 */
	static final MessageResponse createOKResponse() {
		final Api.MessageResponse.Builder builder = Api.MessageResponse.newBuilder();
		builder.setType(Api.MessageResponse.Type.OK);
		
		return builder.build();
	}
}
