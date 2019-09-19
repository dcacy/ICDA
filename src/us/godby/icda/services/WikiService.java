package us.godby.icda.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.protocol.client.ClientResponse;

import us.godby.icda.app.Config;
import us.godby.icda.ic.Comment;
import us.godby.icda.ic.Community;
import us.godby.icda.ic.Member;
import us.godby.icda.ic.Profile;
import us.godby.icda.ic.Wiki;
import us.godby.icda.ic.WikiPage;
import us.godby.utilities.Countdown;
import us.godby.utilities.RestBroker;
import us.godby.utilities.StringUtils;

public class WikiService {

	// utilities
	private RestBroker restBroker = new RestBroker();
	private StringUtils stringUtils = new StringUtils();
	
	// delete the specified wikis
	public void deleteWikis(List<Wiki> wikis, Profile profile) {
		for (Wiki wiki : wikis) {
			deleteWiki(wiki, profile);
		}
	}
	
	// delete a specific wiki for the user
	public void deleteWiki(Wiki wiki, Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_deleteWiki");
		url = url.replace("${uUid}", wiki.getuUid());
		ClientResponse response = restBroker.doDelete(url, profile);
		
		if (response.getStatus() == 200) {
			System.out.println("Deleted wiki [" + wiki.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
	}
	
	// delete a specific wiki page
	public void deleteWikiPage(WikiPage page, Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_deleteWikiPage");
		url = url.replace("${wikiUuid}", page.getWikiUuid());
		url = url.replace("${pageUuid}", page.getuUid());
		
		ClientResponse response = restBroker.doDelete(url, profile);
		if (response.getStatus() == 204) {
			System.out.println("  Deleted page [" + page.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
	}
	
	public Wiki getCommunityWiki(Community community, Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_getCommunityWikis"); 
		url = url.replace("${commUuid}", community.getuUid());
		return getWikis(url, profile).get(0);
	}
	
	// get a list of wikis for the user
	public List<Wiki> getMyWikis(Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_getMyWikis");
		return getWikis(url, profile);
	}
	
	// get a list of wikis
	private List<Wiki> getWikis(String url, Profile profile) {
		List<Wiki> list = new ArrayList<Wiki>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// create wiki
				Wiki wiki = new Wiki();
				wiki.setLabel(entry.getExtension(Config.QNAME_TD_LABEL).getText());
				wiki.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				wiki.setSummary(entry.getSummary());
				wiki.setTitle(entry.getTitle());
				wiki.setuUid(entry.getExtension(Config.QNAME_TD_UUID).getText());
				wiki.setVisibility(entry.getExtension(Config.QNAME_TD_VISIBILITYCOMPUTED).getText());
				
				list.add(wiki);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getWikis(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of the members for the wiki
	public Wiki getMembers(Wiki wiki, Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_getMembers");
		url = url.replace("${uUid}", wiki.getuUid());
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				Element elem = entry.getContentElement().getFirstChild(Config.QNAME_CA_MEMBER);
				
				// create member
				Member member = new Member();
				member.setComponent("wikis");
				member.setRole(elem.getAttributeValue(Config.QNAME_CA_ROLE));
				member.setuUid(elem.getAttributeValue(Config.QNAME_CA_ID));
				
				wiki.addMember(member);	
			}
		}
		
		return wiki;
	}
	
	// get a list of wiki pages for a specific wiki
	public List<WikiPage> getWikiPages(Wiki wiki, Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_getWikiPages");
		url = url.replace("${uUid}", wiki.getuUid());
		return getWikiPages(url, profile, wiki);
	}
	
	private List<WikiPage> getWikiPages(String url, Profile profile, Wiki wiki) {
		List<WikiPage> list = new ArrayList<WikiPage>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// create wiki page
				WikiPage page = new WikiPage();
				page.setLabel(entry.getExtension(Config.QNAME_TD_LABEL).getText());
				page.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				page.setTitle(entry.getTitle());
				page.setuUid(entry.getExtension(Config.QNAME_TD_UUID).getText());
				page.setWikiUuid(wiki.getuUid());
				// tags
				List<Category> categories = entry.getCategories();
				for (Category category : categories) {
					if (category.getScheme() == null) {
						page.addTag(category.getTerm());
					}
				}

				// content
				//String url2 = entry.getLinkResolvedHref("edit").toString();
				String url2 = entry.getLinkResolvedHref("edit-media").toString();
				//String url2 = Config.URLS.get("wikis") + Config.URLS.get("wikis_getWikiPageMedia");
				//url2 = url2.replace("${wikiUuid}", wiki.getuUid());
				//url2 = url2.replace("${pageUuid}", page.getuUid());
				ClientResponse response2 = restBroker.doGet(url2, profile);
				if (response2.getStatus() == 200) {
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(response2.getInputStream()));
						String result = "";
						String line;
						while ((line = in.readLine()) != null) {
							result += line;
						}
						in.close();
						
						/*
						// remove XML definition tag
						result = result.replaceAll("\\<\\?xml(.+?)\\?\\>", "");
						// remove DOCTYPE definition tag <--- NEED TO REPLACE WITH WORKING REGEX :)
						if (result.startsWith("<!DOCTYPE")) {
							int start = result.indexOf(">]>") + 3;
							result = result.substring(start);
						}
						
						// remove extra CDATA sections from Community pages
						if (result.startsWith("<![CDATA[<div><![CDATA[<div>")) {
							result = result.replace("<![CDATA[<div><![CDATA[<div>", "");
							result = result.replace("</div>]]</div>]]", "");
						}
						*/
						
						page.setContent(result);
						
					} catch (Exception e) {}
				}
				
				list.add(page);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getWikiPages(link.getHref().toString(), profile, wiki));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of comments for the specified wiki page
	public List<Comment> getWikiPageComments(WikiPage page, Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_getWikiPageComments");
		url = url.replace("${wikiUuid}", page.getWikiUuid());
		url = url.replace("${pageUuid}", page.getuUid());
		return getWikiPageComments(url, profile, page);
	}
	
	// get a list of comments for the specified wiki page
	private List<Comment> getWikiPageComments(String url, Profile profile, WikiPage page) {
		List<Comment> list = new ArrayList<Comment>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// create comment
				Comment comment = new Comment();
				comment.setuUid(entry.getExtension(Config.QNAME_TD_UUID).getText());
				comment.setOwnerUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				comment.setContent(entry.getContent());
				comment.setRefId(page.getuUid());
				comment.setRefType("wikipage");
				
				list.add(comment);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getWikiPageComments(link.getHref().toString(), profile, page));
				}
			} catch (Exception e) {}
		}

		return list;
	}
	
	// create wiki
	public Wiki createWiki(Wiki wiki, Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_createWiki");
		ClientResponse response = restBroker.doPost(url, wiki.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			wiki.setAuthorUuid(profile.getuUid());
			
			// Wikis does not return a Location header link, so go retrieve the uUid
			String url2 = Config.URLS.get("wikis") + Config.URLS.get("wikis_getWiki");
			url2 = url.replace("${uUid}", stringUtils.urlEncode(wiki.getLabel()));
			ClientResponse response2 = restBroker.doGet(url2, profile);
			if (response2.getStatus() == 200) {
				Feed feed = (Feed) response2.getDocument().getRoot();
				List<Entry> entries = feed.getEntries();
				for (Entry entry : entries) {
					if (entry.getExtension(Config.QNAME_TD_LABEL).getText().equalsIgnoreCase(wiki.getLabel())) {
						wiki.setuUid(entry.getExtension(Config.QNAME_TD_UUID).getText());		
					}
				}
			}

			System.out.println("Created wiki [" + wiki.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create wiki [" + wiki.getTitle() + "]");
		}
		
		return wiki;
	}
	
	// create wiki page
	public WikiPage createWikiPage(WikiPage page, Profile profile) {
		return createWikiPage(page, profile, 0);
	}
	
	// create wiki page
	private WikiPage createWikiPage(WikiPage page, Profile profile, int retryAttempt) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_createWikiPage");
		url = url.replace("${uUid}", page.getWikiUuid());
		ClientResponse response = restBroker.doPost(url, page.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			page.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.indexOf("/page") + 6);
			uUid = uUid.replace("/entry", "");
			page.setuUid(uUid);
			
			System.out.println("  Created page [" + page.getTitle() + "] for user [" + profile.getDisplayName() + "]");
			/*
			// HACK
			String loc = response.getLocation().toString();
			ClientResponse cr2 = restBroker.doGet(loc, profile);
			Entry entry = (Entry) cr2.getDocument().getRoot();
			entry.setContent(page.getContent());
			cr2 = restBroker.doPost(loc, entry, profile);
			System.out.println(cr2.getStatus());
			*/
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create page [" + page.getTitle() + "]");
			
			// it takes time for community members to propagate to a new community activity
			// if a 403 is received, this could be a community acl issue, so retry
			if (response.getStatus() == 403) {
				if (retryAttempt++ < Config.RETRY_COMMUNITY_WIKIS) {
					System.out.println("  API request retry " + retryAttempt + " of " + Config.RETRY_COMMUNITY_WIKIS);
					try {
						Countdown cd = new Countdown(3, Config.SLEEP_COMMUNITY_WIKIS);
						cd.start();
						return createWikiPage(page, profile, retryAttempt);
					} catch (Exception e) {}
				}
			}
		}
		
		return page;
	}
	
	// create a comment for the specified wiki page
	public Comment createWikiPageComment(WikiPage page, Comment comment, Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_createWikiPageComment");
		url = url.replace("${wikiUuid}", page.getWikiUuid());
		url = url.replace("${pageUuid}", page.getuUid());
		ClientResponse response = restBroker.doPost(url, comment.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.lastIndexOf("/") + 1);
			comment.setuUid(uUid);
			
			System.out.println("    Created comment for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to comment [" + profile.getDisplayName() + "]");
		}
		
		return comment;
	}
	
	// update the metadata for the specified wiki
	public Wiki updateWiki(Wiki wiki, Profile profile) {
		String url = Config.URLS.get("wikis") + Config.URLS.get("wikis_updateWiki");
		url = url.replace("${uUid}", wiki.getLabel());
		ClientResponse response = restBroker.doPut(url, wiki.getAtomDocument(), profile);
				
		if (response.getStatus() == 200) {
			System.out.println("Updated wiki metadata [" + wiki.getTitle() + "]");
		}
		else {
				System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to update blog [" + wiki.getTitle() + "]");
		}
		
		return wiki;
	}
}
