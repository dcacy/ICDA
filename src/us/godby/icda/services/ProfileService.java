package us.godby.icda.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.stax.FOMCategories;
import org.apache.abdera.protocol.client.ClientResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import us.godby.icda.app.Config;
import us.godby.icda.ic.Colleague;
import us.godby.icda.ic.Profile;
import us.godby.icda.ic.ProfileTag;
import us.godby.utilities.RestBroker;
import us.godby.utilities.XmlUtils;

public class ProfileService {

	// utilities
	private Abdera abdera = new Abdera();
	private RestBroker restBroker = new RestBroker();
	private XmlUtils xmlUtils = new XmlUtils();
	
	// load multiple users from XML input file
	public void loadProfiles(List<String> users, Document doc) {
		for (String user : users) {
			loadProfile(user, doc);
		}
	}
	
	// load a user from XML input file
	public void loadProfile(String user, Document doc) {
		Node node = xmlUtils.getNodeByXPath(doc, "/icda/users/user[@uid='" + user + "']");
		try { node.getNodeName(); } catch (Exception e) { return; }
		
		// create a new profile
		Profile profile = new Profile();
		profile.setUid(xmlUtils.getStringByXPath(node, "./@uid"));
		profile.setEmail(xmlUtils.getStringByXPath(node, "./@mail"));
		profile.setPassword(xmlUtils.getStringByXPath(node, "./@password"));
		
		// hit the Activities API to verify this is a valid Connections user
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_getMyActivities");
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			// extract the user's Connections uUid
			String uUid = feed.getAuthor().getExtension(Config.QNAME_SNX_USER).getText();
			
			profile.setuUid(uUid);
			profile.setDisplayName(feed.getAuthor().getName());
			
			// now that you have the Connections uUid, go retrieve the Profile key
			String url2 = Config.URLS.get("profiles") + Config.URLS.get("profiles_getProfile");
			url2 = url2.replace("${uUid}", profile.getuUid());
			ClientResponse response2 = restBroker.doGet(url2, profile);
			if (response2.getStatus() == 200) {
				Feed feed2 = (Feed) response2.getDocument().getRoot();
				Entry entry2 = feed2.getEntries().get(0);
				
				String key = entry2.getId().toString();
				key = key.substring(key.lastIndexOf(":") + 6);
				profile.setKey(key);
			}
			
			// store the profile to allow for lookups via XML input file uid
			Config.PROFILES.put(profile.getUid(), profile);
			// store the profile to allow for lookups via Connections uUid
			Config.PROFILES_UUID.put(profile.getuUid(), profile);
			
			System.out.println("Loaded user [" + profile.getDisplayName() + "] with uUid [" + profile.getuUid() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to load user [" + profile.getUid() + "]");
		}
	}
	
	// delete all colleagues for the user
	public void deleteColleagues(Profile profile) {
		List<Colleague> colleagues = getAllColleagues(profile);
		for (Colleague colleague : colleagues) {
			deleteColleague(colleague, profile);
		}
	}
	
	// delete a specific colleague for the user
	public void deleteColleague(Colleague colleague, Profile profile) {
		String url = Config.URLS.get("profiles") + Config.URLS.get("profiles_deleteColleague");
		url = url.replace("${uUid}", colleague.getuUid());
		ClientResponse response = restBroker.doDelete(url, profile);
		
		if (response.getStatus() == 200) {
			System.out.println("Deleted colleague for user [" + profile.getDisplayName() + "]");
		}
	}
	
	// get all colleagues for the user
	public List<Colleague> getAllColleagues(Profile profile) {
		List<Colleague> colleagues = getMyAcceptedColleagues(profile);
		List<Colleague> pending = getMyPendingColleagues(profile);
		colleagues.addAll(pending);
		//List<Colleague> unconfirmed = getMyUnconfirmedColleagues(profile);
		//colleagues.addAll(unconfirmed);
		return colleagues;
	}
	
	public List<Colleague> getMyAcceptedColleagues(Profile profile) {
		String url = Config.URLS.get("profiles") + Config.URLS.get("profiles_getColleagues");
		url = url.replace("${uUid}", profile.getuUid());
		url = url.replace("${status}", "accepted");
		return getColleagues(url, profile);
	}
	
	public List<Colleague> getMyPendingColleagues(Profile profile) {
		String url = Config.URLS.get("profiles") + Config.URLS.get("profiles_getColleagues");
		url = url.replace("${uUid}", profile.getuUid());
		url = url.replace("${status}", "pending");
		return getColleagues(url, profile);
	}
	
	public List<Colleague> getMyUnconfirmedColleagues(Profile profile) {
		String url = Config.URLS.get("profiles") + Config.URLS.get("profiles_getColleagues");
		url = url.replace("${uUid}", profile.getuUid());
		url = url.replace("${status}", "unconfirmed");
		return getColleagues("unconfirmed", profile);
	}
	
	// get colleagues for the specified profile
	private List<Colleague> getColleagues(String url, Profile profile) {
		List<Colleague> list = new ArrayList<Colleague>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf(":") + 6);
				
				// the API flips the source & target values depending on who is retrieving the data
				// if the user who retrieved the data is listed as the entry author:
				//    snx:connection --> source --> connection initiator
				//    snx:connection --> target --> connection receiver
				// if the user who retrieved the data is NOT listed as the entry author:
				//    snx:connection --> source --> connection receiver
				//    snx:connection --> target --> connection initiator
				// Also, the content element will only contain data if the user retrieving the data is the receiver
				
				String fromUuid = "";
				String toUuid = "";
				// retrieve the actual from and to values for this connection
				Element connection = entry.getExtension(Config.QNAME_SNX_CONNECTION);
				List<Element> contributors = connection.getElements();
				for (Element contributor : contributors) {
					String userid = contributor.getFirstChild(Config.QNAME_SNX_USER).getText();
					String type = contributor.getAttributeValue(Config.QNAME_SNX_REL);
					
					if (type.endsWith("source")) { fromUuid = userid; }
					if (type.endsWith("target")) { toUuid = userid; }
				}
				
				// create colleague
				Colleague coll = new Colleague();
				coll.setContent(entry.getContent());
				coll.setFromUuid(fromUuid);
				coll.setOwnerUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				coll.setStatus(entry.getCategories("http://www.ibm.com/xmlns/prod/sn/status").get(0).getTerm());
				coll.setToUuid(toUuid);
				coll.setuUid(uUid);
				
				list.add(coll);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getColleagues(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	public List<ProfileTag> getTags(Profile profile) {
		List<ProfileTag> list = new ArrayList<ProfileTag>();
		
		String url = Config.URLS.get("profiles") + Config.URLS.get("profiles_getTags");
		url = url.replace("${key}", profile.getKey());
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			FOMCategories fomcats = (FOMCategories) response.getDocument().getRoot();
			List<Category> categories = fomcats.getCategories();
			for (Category category : categories) {
				// create profile tag
				ProfileTag tag = new ProfileTag();
				tag.setFrequency(category.getAttributeValue(Config.QNAME_SNX_FREQUENCY));
				tag.setIntensityBin(category.getAttributeValue(Config.QNAME_SNX_INTENSITYBIN));
				tag.setTerm(category.getTerm());
				tag.setType(category.getAttributeValue(Config.QNAME_SNX_TYPE));
				tag.setVisibilityBin(category.getAttributeValue(Config.QNAME_SNX_VISIBILITYBIN));
				
				list.add(tag);
			}
		}
		
		return list;
	}
	
	// create colleague connection
	public Colleague createColleague(Colleague colleague, Profile source, Profile target) {
		String url = Config.URLS.get("profiles") + Config.URLS.get("profiles_createColleague");
		url = url.replace("${key}", target.getKey());
		ClientResponse response = restBroker.doPost(url, colleague.getAtomDocument(), source);
		
		if (response.getStatus() == 201) {
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.indexOf("=") + 1);
			colleague.setuUid(uUid);
			
			System.out.println("Created colleague connection from user [" + source.getDisplayName() + "] to [" + target.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create colleague connection from user [" + source.getDisplayName() + "] to [" + target.getDisplayName() + "]");
		}
		
		return colleague;
	}
	
	// accept a pending colleague connection
	public void acceptColleague(Colleague colleague, Profile profile) {
		String url = Config.URLS.get("profiles") + Config.URLS.get("profiles_acceptColleague");
		url = url.replace("${uUid}", colleague.getuUid());
		ClientResponse response = restBroker.doPut(url, colleague.getAtomDocument(), profile);
		
		if (response.getStatus() == 200) {
			System.out.println("  Pending connection accepted by [" + profile.getDisplayName() + "]");
		}
	}
	
	// add tags to the specified profile
	public void addTags(List<String> tags, Profile profile) {
		// get the current tags
		String url = Config.URLS.get("profiles") + Config.URLS.get("profiles_getTags");
		url = url.replace("${key}", profile.getKey());
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			// add new tags to the existing tags
			FOMCategories fomcats = (FOMCategories) response.getDocument().getRoot();
			boolean updateTags = false;
			for (String tag : tags) {
				if (!tag.trim().equalsIgnoreCase("")) {
					Category cat = abdera.getFactory().newCategory();
					cat.setTerm(tag);
					fomcats.addCategory(cat);
					
					updateTags = true;
				}
			}
			
			if (updateTags) {
				// put the updated tags back to the server
				String url2 = Config.URLS.get("profiles") + Config.URLS.get("profiles_createTags");
				url2 = url2.replace("${targetKey}", profile.getKey());
				url2 = url2.replace("${sourceKey}", profile.getKey());
				ClientResponse response2 = restBroker.doPut(url2, fomcats, profile);
				
				if (response2.getStatus() == 200) {
					System.out.println("Updated profile tags for user [" + profile.getDisplayName() + "]");
				}
				else {
					System.out.println("[" + response2.getStatus() + ": " + response2.getStatusText() +"] Failed to update tags for user [" + profile.getDisplayName() + "]");
				}
			}
		}
	}
}
