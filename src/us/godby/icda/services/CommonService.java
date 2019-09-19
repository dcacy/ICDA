package us.godby.icda.services;

import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.client.ClientResponse;

import us.godby.icda.app.Config;
import us.godby.utilities.RestBroker;

public class CommonService {

	// utilities
	private Abdera abdera = new Abdera();
	private RestBroker restBroker = new RestBroker();
	
	// retrieve URLs for installed IBM Connections applications / services
	public void getServiceConfigs() {
		String url = Config.URLS.get("host") + Config.URLS.get("serviceconfigs");
		ClientResponse response = restBroker.doGet(url);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			Config.ENVIRONMENT = feed.getGenerator().getAttributeValue(Config.QNAME_SNX_ENVIRONMENT);
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// extract the URL for the application / service
				String service = entry.getTitle();
				String link = abdera.getXPath().valueOf("./a:link[@rel='http://www.ibm.com/xmlns/prod/sn/alternate-ssl']/@href", entry);
				
				// store the URL in the global configuration
				Config.URLS.put(service, link);
				System.out.println(service + ": " + link);
			}	
		}
	}
	
}
