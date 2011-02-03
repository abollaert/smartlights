package be.abollaert.domotics.light.services.upgrade.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sun.misc.CRC16;

import be.abollaert.domotics.light.services.upgrade.UpgradeService;
import be.abollaert.domotics.light.services.upgrade.proto.UpgradeProto.UpgradeRequest;
import be.abollaert.domotics.light.services.upgrade.proto.UpgradeProto.UpgradeResponse;
import be.abollaert.domotics.light.services.upgrade.proto.UpgradeProto.UpgradeResponse.Builder;
import be.abollaert.domotics.light.services.upgrade.proto.UpgradeProto.UpgradeResponse.ResponseCode;

/**
 * Servlet that you can upload a file to, that will then 
 * @author alex
 *
 * FIXME: This just needs to be a handler, not a servlet in se.
 */
public final class UpgradeServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(UpgradeServlet.class
			.getName());
	
	/** The upgrade service. */
	private final UpgradeService upgradeService;
	
	/**
	 * Create a new instance specifying the upgrade service.
	 * 
	 * @param 	upgradeService		The upgrade service.
	 */
	public UpgradeServlet(final UpgradeService upgradeService) {
		this.upgradeService = upgradeService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		throw new ServletException("GET method is not supported, you need to use POST");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void doPost(final HttpServletRequest request, final HttpServletResponse response) {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Received request for upgrade from [" + request.getRemoteAddr() + "].");
		}
		
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		try {
			inputStream = request.getInputStream();
			outputStream = response.getOutputStream();
			
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Parsing request.");
			}
			
			final UpgradeRequest upgradeRequest = UpgradeRequest.parseFrom(inputStream);
			
			final int moduleId = upgradeRequest.getModuleId();
			final byte[] hexFile = upgradeRequest.getHexFile().toByteArray();
			final int crc = upgradeRequest.getHexFileCrc();
			
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Request to upgrade module with ID [" + moduleId + "], hex file with size [" + hexFile.length + "], crc is [" + crc + "].");
			}
			
			final CRC16 calculatedCrc = new CRC16();
			
			for (final byte b : hexFile) {
				calculatedCrc.update(b);
			}
			
			if (calculatedCrc.value == crc) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Upgrade finished successfully.");
				}
			} else {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "CRC mismatch, calculated [" + calculatedCrc.value + "], passed [" + crc + "] !");
				}
			}

			
			final Builder builder = UpgradeResponse.newBuilder();
			builder.setResponseCode(ResponseCode.OK);
			
			final UpgradeResponse upgradeResponse = builder.build();
			upgradeResponse.writeTo(outputStream);
			
			outputStream.flush();
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Could not process upgrade request due to an IO error reading the request [" + e.getMessage() + "] !", e);
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Could not close input stream due to an IO error [" + e.getMessage() + "] !", e);
					}
				}
			}
			
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Could not close output stream due to an IO error [" + e.getMessage() + "] !", e);
					}
				}
			}
		}
	}
}
