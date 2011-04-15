package be.abollaert.domotics.light.webinterface;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * The status page of the web interface.
 * 
 * @author alex
 */
public final class StatusPage extends WebPage {
	
	public StatusPage() {
		super();
		
		this.add(new Label("message", "Hello world"));
	}

}
