package us.godby.icda.services;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.protocol.client.ClientResponse;

import us.godby.icda.app.Config;
import us.godby.icda.ic.Bookmark;
import us.godby.icda.ic.Community;
import us.godby.icda.ic.Event;
import us.godby.icda.ic.FeedLink;
import us.godby.icda.ic.Member;
import us.godby.icda.ic.Profile;
import us.godby.icda.ic.Widget;
import us.godby.utilities.RestBroker;

public class CommunityService {

	// utilities
	private RestBroker restBroker = new RestBroker();
	
	// delete the specified communities
	public void deleteCommunities(List<Community> communities, Profile profile) {
		for (Community community : communities) {
			deleteCommunity(community, profile);
		}
	}
	
	// delete a specific community for the user
	public void deleteCommunity(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_deleteCommunity");
		url = url.replace("${uUid}", community.getuUid());
		ClientResponse response = restBroker.doDelete(url, profile);
		
		if (response.getStatus() == 200) {
			System.out.println("Deleted community [" + community.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
	}
	
	// get my communities for the user
	public List<Community> getMyCommunities(Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getMyCommunities");
		return getCommunities(url, profile, false);
	}
	
	// get my communities for the user
	public List<Community> getSubCommunities(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getSubCommunities");
		url = url.replace("${uUid}", community.getuUid());
		return getCommunities(url, profile, true);
	}
	
	// get a list of the specified communities for the user
	private List<Community> getCommunities(String url, Profile profile, boolean isSubCommunity) {
		List<Community> list = new ArrayList<Community>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// only retrieve parent communities; sub-communities are pulled in the EXPORTER class
				Link link = entry.getLink("http://www.ibm.com/xmlns/prod/sn/parentcommunity");
				if ( ((link == null) && !isSubCommunity) || (isSubCommunity) ) {
					// create community
					Community community = new Community();
					community.setuUid(entry.getExtension(Config.QNAME_SNX_COMMUNITYUUID).getText());
					// the feed does not contain content element data...  So, go get it.  /sigh
					community = getCommunity(community, profile);
					
					list.add(community);
				}
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getCommunities(link.getHref().toString(), profile, isSubCommunity));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a specific community for the user
	public Community getCommunity(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getCommunity");
		url = url.replace("${uUid}", community.getuUid());
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Entry entry = (Entry) response.getDocument().getRoot();
			community.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
			community.setContent(entry.getContent());
			community.setExternal(Boolean.valueOf(entry.getExtension(Config.QNAME_SNX_ISEXTERNAL).getText()));
			community.setTitle(entry.getTitle());
			community.setType(entry.getExtension(Config.QNAME_SNX_COMMUNITYTYPE).getText());
			community.setuUid(entry.getExtension(Config.QNAME_SNX_COMMUNITYUUID).getText());
			// tags
			List<Category> categories = entry.getCategories();
			for (Category category : categories) {
				if (category.getScheme() == null) {
					community.addTag(category.getTerm());
				}
			}
			// logo
			community = getLogo(community, profile);
		}
		return community;
	}
	
	// download the logo
	public Community getLogo(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getLogo");
		url = url.replace("${uUid}", community.getuUid());
		ClientResponse response = restBroker.doGet(url, profile);

		if (response.getStatus() == 200) {
			// header only appears if custom logo is in place
			if (response.getHeader("Content-Disposition") != null) {
				// logo must be in JPEG, GIF, or PNG format
				String ext = response.getContentType().toString();
				ext = ext.substring(ext.indexOf("/") + 1);
				String filename = community.getuUid() + "." + ext;
					
				// save the logo to the "files" sub-directory
				try {
					InputStream is = response.getInputStream();
					byte[] buffer = new byte[4096];
					int n = -1;
					
					java.io.File outFile = new java.io.File(Config.DIR_FILES, filename);
					OutputStream os = new FileOutputStream(outFile, false);
					while ((n = is.read(buffer)) != -1) {
						os.write(buffer, 0, n);
					}
					os.close();
					community.setLogo(filename);
				} catch (Exception e) {}
			}
		}
		return community;
	}
	
	// get a list of the members for the community
	public List<Member> getMembers(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getMembers");
		url = url.replace("${uUid}", community.getuUid());
		return getMembers(url, profile);
	}
	
	private List<Member> getMembers(String url, Profile profile) {
		List<Member> list = new ArrayList<Member>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// create member
				Member member = new Member();
				member.setComponent("communities");
				member.setRole(entry.getExtension(Config.QNAME_SNX_ROLE).getText());
				member.setuUid(entry.getContributors().get(0).getExtension(Config.QNAME_SNX_USER).getText());
						
				list.add(member);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getMembers(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of widgets for the community
	public List<Widget> getWidgets(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getWidgets");
		url = url.replace("${uUid}", community.getuUid());
		return getWidgets(url, profile);
	}
	
	private List<Widget> getWidgets(String url, Profile profile) {
		List<Widget> list = new ArrayList<Widget>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// create widget
				Widget widget = new Widget();
				try { widget.setLocation(entry.getExtension(Config.QNAME_SNX_LOCATION).getText()); } catch (Exception e) {}
				widget.setTitle(entry.getTitle());
				widget.setDefId(entry.getExtension(Config.QNAME_SNX_WIDGETDEFID).getText());
				widget.setInstanceID(entry.getExtension(Config.QNAME_SNX_WIDGETINSTANCEID).getText());
				// widget properties
				List<Element> elements = entry.getExtensions(Config.QNAME_SNX_WIDGETPROPERTY);
				for (Element elem : elements) {
					String key = elem.getAttributeValue("key");
					String value = elem.getText();
					widget.setProperty(key, value);
				}
				
				list.add(widget);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getWidgets(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of community bookmarks
	public List<Bookmark> getBookmarks(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getBookmarks");
		url = url.replace("${uUid}", community.getuUid());
		return getBookmarks(url, profile);
	}
	
	private List<Bookmark> getBookmarks(String url, Profile profile) {
		List<Bookmark> list = new ArrayList<Bookmark>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// create bookmark
				Bookmark bookmark = new Bookmark();
				bookmark.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				bookmark.setTitle(entry.getTitle());
				bookmark.setContent(entry.getContent());
				bookmark.setLink(entry.getLinks().get(0).getHref().toString());
				// tags
				List<Category> categories = entry.getCategories();
				for (Category category : categories) {
					if (category.getScheme() == null) {
						bookmark.addTag(category.getTerm());
					}
					else if (category.getScheme().toString().equalsIgnoreCase("http://www.ibm.com/xmlns/prod/sn/flags")) {
						bookmark.setImportant(true);
					}
				}
				
				list.add(bookmark);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getBookmarks(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of community feeds
	public List<Event> getEvents(Community community, Profile profile) {	
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getEvents");
		url = url.replace("${uUid}", community.getuUid());
		return getEvents(url, profile);
	}
	
	private List<Event> getEvents(String url, Profile profile) {
		List<Event> list = new ArrayList<Event>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf(":") + 1);
				
				// create event
				Event event = new Event();
				event.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				event.setAllDay(entry.getExtension(Config.QNAME_SNX_ALLDAY).getText());
				event.setContent(entry.getContent());
				event.setLocation(entry.getExtension(Config.QNAME_SNX_LOCATION).getText());
				event.setTitle(entry.getTitle());
				event.setType("event");
				event.setuUid(uUid);
				
				// event recurrence
				ExtensibleElement elem = entry.getExtension(Config.QNAME_SNX_RECURRENCE);
				event.setCustom(elem.getAttributeValue("custom"));
				
				if (event.getCustom().equalsIgnoreCase("no")) {
					event.setFrequency(elem.getAttributeValue("frequency"));
					event.setInterval(elem.getAttributeValue("interval"));
					if (event.getInterval().equalsIgnoreCase("null")) { event.setInterval(""); }
					event.setDateUntil(elem.getExtension(Config.QNAME_SNX_UNTIL).getText());
					event.setDateStart(elem.getExtension(Config.QNAME_SNX_STARTDATE).getText());
					event.setDateEnd(elem.getExtension(Config.QNAME_SNX_ENDDATE).getText());
					try { event.setByDay(elem.getExtension(Config.QNAME_SNX_BYDAY).getText()); } catch (Exception e) {}
					try { event.setByDay(elem.getExtension(Config.QNAME_SNX_BYDATE).getText()); } catch (Exception e) {}
				}
				else {
					ExtensibleElement elem2 = elem.getExtension(Config.QNAME_SNX_PERIOD);
					event.setDateStart(elem2.getExtension(Config.QNAME_SNX_STARTDATE).getText());
					event.setDateEnd(elem2.getExtension(Config.QNAME_SNX_ENDDATE).getText());
				}
				
				list.add(event);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getEvents(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of community feeds
	public List<FeedLink> getFeedLinks(Community community, Profile profile) {	
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getFeedLinks");
		url = url.replace("${uUid}", community.getuUid());
		return getFeedLinks(url, profile);
	}
	
	private List<FeedLink> getFeedLinks(String url, Profile profile) {
		List<FeedLink> list = new ArrayList<FeedLink>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf("=") + 1);
				
				// create feed link
				FeedLink fl = new FeedLink();
				fl.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				fl.setContent(entry.getContent());
				fl.setTitle(entry.getTitle());
				fl.setuUid(uUid);
				// link
				List<Link> links = entry.getLinks();
				for (Link link : links) {
					if (link.getRel() == null) {
						fl.setLink(link.getHref().toString());
					}
				}
				// tags
				List<Category> categories = entry.getCategories();
				for (Category category : categories) {
					if (category.getScheme() == null) {
						fl.addTag(category.getTerm());
					}
				}
				
				list.add(fl);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getFeedLinks(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of remote applications for the community
	public void getRemoteApplications(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_getRemoteApplications");
		url = url.replace("${uUid}", community.getuUid());
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			System.out.println(feed.toString());
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				System.out.println(entry.toString());
			}
		}
	}
	
	// create community
	public Community createCommunity(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_createCommunity");
		return createCommunity(url, profile, community);
	}
	
	// update community
	public Community updateCommunity(Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_updateCommunity");
		url = url.replace("${uUid}", community.getuUid());
		return updateCommunity(url, profile, community);
	}
	
	// create community
	public Community createSubCommunity(Community community, Community parentCommunity, Profile profile) {
		community.setParentUuid(parentCommunity.getuUid());
		
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_createSubCommunity");
		url = url.replace("${uUid}", community.getParentUuid());
		return createCommunity(url, profile, community);		
	}
	
	private Community createCommunity(String url, Profile profile, Community community) {
		ClientResponse response = restBroker.doPost(url, community.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			community.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.indexOf("=") + 1);
			community.setuUid(uUid);
			
			System.out.println("Created community [" + community.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create community [" + community.getTitle() + "]");
		}
		
		return community;
	}
	
	/*
	 * dpc: Added this so we can change the community description after the community has been created.
	 * This is because the description may have links to other apps in this community, so we need to
	 * modify those links to point to this community.
	 */
	private Community updateCommunity(String url, Profile profile, Community community) {
		ClientResponse response = restBroker.doPut(url, community.getAtomDocument(), profile);
		
		if (response.getStatus() == 200) {
			System.out.println("Updated community [" + community.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to update community [" + community.getTitle() + "]");
		}
		
		return community;
	}
	
	// update community logo
	public void updateLogo(Community community, Profile profile) {
		// upload the logo from the "files" sub-directory
		try {
			java.io.File outFile = new java.io.File(Config.DIR_FILES, community.getLogo());
			if (!outFile.exists()) {
				System.out.println("  Failed to update logo.  File does not exist. [" + community.getTitle() + "]");
				return; 
			}
			
			String url = Config.URLS.get("communities") + Config.URLS.get("communities_updateLogo");
			url = url.replace("${uUid}", community.getuUid());
			ClientResponse response = restBroker.doPut(url, outFile, profile);
			
			if (response.getStatus() == 200) {
				System.out.println("  Updated logo for community [" + community.getTitle() + "]");
			}
			else {
				System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to update logo [" + community.getTitle() + "]");
			}
			
		} catch (Exception e) {
			//System.out.println("  Failed to update logo.  File does not exist. [" + community.getTitle() + "]");
		}
	}
	
	// add a member to the specified community
	public void addMember(Community community, Member member, Profile profile) {		
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_addMember");
		url = url.replace("${uUid}", community.getuUid());
		ClientResponse response = restBroker.doPost(url, member.getAtomDocument(), profile);
		
		// used for output only...
		Profile memberProfile = Config.PROFILES_UUID.get(member.getuUid());
		
		if (response.getStatus() == 201) {
			System.out.println("  Added " + member.getRole() + " [" + memberProfile.getDisplayName() + "]");
		}
	}
	
	// add a widget to the specified community
	public Widget addWidget(Community community, Widget widget, Profile profile) {		
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_addWidget");
		url = url.replace("${uUid}", community.getuUid());
		ClientResponse response = restBroker.doPost(url, widget.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			// retrieve instance id from location header
			String instanceId = response.getLocation().toString();
			instanceId = instanceId.substring(instanceId.indexOf("=") + 1); // dpc
			widget.setInstanceID(instanceId);
			
			// dpc: capture the wiki instance ID for later modification of the community's description
			if ( widget.getDefId().equals("Wiki")) {
				String wikiInstanceId = instanceId.substring(instanceId.lastIndexOf("=") + 1);
				community.setWikiInstanceId(wikiInstanceId);
			}
			
			System.out.println("  Added [" + widget.getDefId() + "] widget to community");
		}
		
		return widget;
	}
	
	public Bookmark createBookmark(Bookmark bookmark, Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_createBookmark");
		url = url.replace("${uUid}", community.getuUid());
		ClientResponse response = restBroker.doPost(url, bookmark.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			bookmark.setAuthorUuid(profile.getuUid());
			
			System.out.println("  Created bookmark [" + bookmark.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create bookmark [" + bookmark.getTitle() + "]");
		}
		
		return bookmark;
	}
	
	public Event createEvent(Event event, Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_createEvent");
		url = url.replace("${uUid}", community.getuUid());
		ClientResponse response = restBroker.doPost(url, event.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			event.setAuthorUuid(profile.getuUid());
			
			System.out.println("  Created calendar event [" + event.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create calendar event [" + event.getTitle() + "]");
		}
		
		return event;
	}
	
	public FeedLink createFeedLink(FeedLink feed, Community community, Profile profile) {
		String url = Config.URLS.get("communities") + Config.URLS.get("communities_createFeedLink");
		url = url.replace("${uUid}", community.getuUid());
		ClientResponse response = restBroker.doPost(url, feed.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			feed.setAuthorUuid(profile.getuUid());
			
			System.out.println("  Created feed link [" + feed.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create feed link [" + feed.getTitle() + "]");
		}
		
		return feed;
	}
	
}
