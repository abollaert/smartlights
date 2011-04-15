package be.abollaert.domotics.light.webinterface;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.OsgiResourceStreamLocator;

/**
 * Application entry point.
 * 
 * @author alex
 *
 */
public class WebInterface extends WebApplication {
	
	public WebInterface() {
		super();
	}
	
	@Override
	protected final void init() {
		this.getResourceSettings().setResourceStreamLocator(new OsgiResourceStreamLocator());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Class<? extends Page> getHomePage() {
		return StatusPage.class;
	}
}
