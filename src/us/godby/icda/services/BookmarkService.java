package us.godby.icda.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.protocol.client.ClientResponse;

import us.godby.icda.app.Config;
import us.godby.icda.ic.Bookmark;
import us.godby.icda.ic.Profile;
import us.godby.utilities.RestBroker;
import us.godby.utilities.StringUtils;

public class BookmarkService {
	
	// utilities
	private RestBroker restBroker = new RestBroker();
	private StringUtils stringUtils = new StringUtils();
	
	// delete the specified bookmarks
	public void deleteBookmarks(List<Bookmark> bookmarks, Profile profile) {
		for (Bookmark bookmark : bookmarks) {
			deleteBookmark(bookmark, profile);
		}
	}
	
	// delete a specific bookmark for the user
	public void deleteBookmark(Bookmark bookmark, Profile profile) {
		String url = Config.URLS.get("dogear") + Config.URLS.get("dogear_deleteBookmark");
		url = url.replace("${url}", stringUtils.urlEncode(bookmark.getLink()));
		
		ClientResponse response = restBroker.doDelete(url, profile);
		if (response.getStatus() == 204) {
			System.out.println("Deleted bookmark [" + bookmark.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
	}
	
	// get a list of bookmarks for the specified user
	public List<Bookmark> getMyBookmarks(Profile profile) {
		String url = Config.URLS.get("dogear") + Config.URLS.get("dogear_getMyBookmarks");
		url = url.replace("${uUid}", profile.getuUid());
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
				bookmark.setuUid(entry.getExtension(Config.QNAME_SNX_LINK).getAttributeValue("linkid"));
				bookmark.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				bookmark.setTitle(entry.getTitle());
				bookmark.setContent(entry.getContent());
				bookmark.setLink(entry.getLinks().get(0).getHref().toString());
				// tags
				List<Category> categories = entry.getCategories();
				for (Category category : categories) {
					bookmark.addTag(category.getTerm());
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
	
	// create bookmark
	public Bookmark createBookmark(Bookmark bookmark, Profile profile) {
		String url = Config.URLS.get("dogear") + Config.URLS.get("dogear_createBookmark");
		ClientResponse response = restBroker.doPost(url, bookmark.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			bookmark.setAuthorUuid(profile.getuUid());
			
			System.out.println("Created bookmark [" + bookmark.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create bookmark [" + bookmark.getTitle() + "]");
		}
		
		return bookmark;
	}
}
