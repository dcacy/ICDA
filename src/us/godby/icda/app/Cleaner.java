package us.godby.icda.app;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import us.godby.icda.ic.Activity;
import us.godby.icda.ic.Blog;
import us.godby.icda.ic.Bookmark;
import us.godby.icda.ic.Colleague;
import us.godby.icda.ic.Community;
import us.godby.icda.ic.File;
import us.godby.icda.ic.Folder;
import us.godby.icda.ic.Forum;
import us.godby.icda.ic.Profile;
import us.godby.icda.ic.Wiki;
import us.godby.icda.services.ActivityService;
import us.godby.icda.services.BlogService;
import us.godby.icda.services.BookmarkService;
import us.godby.icda.services.CommunityService;
import us.godby.icda.services.FileService;
import us.godby.icda.services.ForumService;
import us.godby.icda.services.ProfileService;
import us.godby.icda.services.WikiService;
import us.godby.utilities.StringUtils;
import us.godby.utilities.XmlUtils;

public class Cleaner {

	// utilities
	private StringUtils stringUtils = new StringUtils();
	private XmlUtils xmlUtils = new XmlUtils();
	
	// connections
	private ActivityService svcActivity = new ActivityService();
	private BlogService svcBlog = new BlogService();
	private BookmarkService svcBookmark = new BookmarkService();
	private CommunityService svcCommunity = new CommunityService();
	private FileService svcFile = new FileService();
	private ForumService svcForum = new ForumService();
	private ProfileService svcProfile = new ProfileService();
	private WikiService svcWiki = new WikiService();
	
	// data
	private Document userDoc = null;
	
	public Cleaner(String userFileName) {
		// load the user XML document
		userDoc = xmlUtils.getXmlFromFile(Config.DIR_DATA, userFileName);
	}
	
	// delete activities for the specified user
	public void cleanActivities(Profile profile) {
		printSection("Activities", profile);
		// retrieve activities
		List<Activity> activities = svcActivity.getAllMyActivities(profile);
		for (Activity activity : activities) {
			// only delete content created by current user
			if (activity.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				// if not community content
				if (activity.getCommunityUuid().equalsIgnoreCase("")) {
					svcActivity.deleteActivity(activity, profile);
				}
			}
		}
	}
	
	// delete communities for the specified user
	public void cleanCommunities(Profile profile) {
		printSection("Communities", profile);
		// retrieve communities
		List<Community> communities = svcCommunity.getMyCommunities(profile);
		for (Community community : communities) {
			// only delete content created by current user
			if (community.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				svcCommunity.deleteCommunity(community, profile);
			}
		}
	}
	
	// delete blogs for the specified user
	public void cleanBlogs(Profile profile) {
		printSection("Blogs", profile);
		// retrieve blogs
		List<Blog> blogs = svcBlog.getMyBlogs(profile);
		for (Blog blog : blogs) {
			// only delete content created by current user
			if (blog.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				svcBlog.deleteBlog(blog, profile);
			}
		}
	}
	
	// delete bookmarks for the specified user
	public void cleanBookmarks(Profile profile) {
		printSection("Bookmarks", profile);
		// retrieve bookmarks
		List<Bookmark> bookmarks = svcBookmark.getMyBookmarks(profile);
		for (Bookmark bookmark : bookmarks) {
			// only delete content created by current user
			if (bookmark.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				svcBookmark.deleteBookmark(bookmark, profile);
			}
		}
	}
	
	// delete files and folders for the specified user
	public void cleanFiles(Profile profile) {
		printSection("Files", profile);
		// retrieve folders
		List<Folder> folders = svcFile.getMyFolders(profile);
		for (Folder folder : folders) {
			// only delete content created by current user
			if (folder.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				svcFile.deleteFolder(folder, profile);
			}
		}
		
		// retrieve files
		List<File> files = svcFile.getMyFiles(profile);
		for (File file : files) {
			// only delete content created by current user
			if (file.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				svcFile.deleteFile(file, profile);
			}
		}
	}
	
	// delete forums for the specified user
	public void cleanForums(Profile profile) {
		printSection("Forums", profile);
		// retrieve forums
		List<Forum> forums = svcForum.getMyForums(profile);
		for (Forum forum : forums) {
			// only delete content created by current user
			if (forum.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				svcForum.deleteForum(forum, profile);
			}
		}
	}
	
	// delete profiles data for the specified user
	public void cleanProfiles(Profile profile) {
		printSection("Profiles", profile);
		// retrieve colleague connections
		List<Colleague> colleagues = svcProfile.getAllColleagues(profile);
		for (Colleague colleague : colleagues) {
			svcProfile.deleteColleague(colleague, profile);
		}
	}
	
	// delete wikis for the specified user
	public void cleanWikis(Profile profile) {
		printSection("Wikis", profile);
		// retrieve wikis
		List<Wiki> wikis = svcWiki.getMyWikis(profile);
		for (Wiki wiki : wikis) {
			// only delete content created by current user
			if (wiki.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				svcWiki.deleteWiki(wiki, profile);
			}
		}
	}
	
	// clean (delete) all content for the specified users and applications!
	public void cleanContent() {
		// for each user
		for (Map.Entry<String, Profile> entry: Config.PROFILES.entrySet()) {
			Profile profile = (Profile) entry.getValue();
			
			// delete content if the user is flagged for the specified app
			if (idFound("activities", profile)) { cleanActivities(profile); }
			if (idFound("communities", profile)) { cleanCommunities(profile); }
			if (idFound("files", profile)) { cleanFiles(profile); }
			if (idFound("profiles", profile)) { cleanProfiles(profile); }
			
			if (Config.ENVIRONMENT.equalsIgnoreCase("on-premise")) {
				if (idFound("blogs", profile)) { cleanBlogs(profile); }
				if (idFound("bookmarks", profile)) { cleanBookmarks(profile); }
				if (idFound("forums", profile)) { cleanForums(profile); }
				if (idFound("wikis", profile)) { cleanWikis(profile); }
			}
		}
	}
	
	private void printSection(String app, Profile profile) {
		System.out.println("");
		System.out.println("[Deleting " + app + " for " + profile.getDisplayName() + "]");
	}
	
	// is the uid for the given profile listed in the delete section of the user XML file
	private boolean idFound(String app, Profile profile) {
		String userids = xmlUtils.getStringByXPath(userDoc, "/icda/delete/" + app);
		boolean result = stringUtils.stringContainsUid(userids, profile.getUid());
		return result;
	}
}
