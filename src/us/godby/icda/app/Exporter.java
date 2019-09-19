package us.godby.icda.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import us.godby.icda.ic.Activity;
import us.godby.icda.ic.ActivityNode;
import us.godby.icda.ic.Blog;
import us.godby.icda.ic.BlogPost;
import us.godby.icda.ic.Bookmark;
import us.godby.icda.ic.Colleague;
import us.godby.icda.ic.Comment;
import us.godby.icda.ic.Community;
import us.godby.icda.ic.Event;
import us.godby.icda.ic.FeedLink;
import us.godby.icda.ic.File;
import us.godby.icda.ic.Folder;
import us.godby.icda.ic.Forum;
import us.godby.icda.ic.ForumTopic;
import us.godby.icda.ic.ForumTopicReply;
import us.godby.icda.ic.Member;
import us.godby.icda.ic.Profile;
import us.godby.icda.ic.ProfileTag;
import us.godby.icda.ic.Recommendation;
import us.godby.icda.ic.Widget;
import us.godby.icda.ic.Wiki;
import us.godby.icda.ic.WikiPage;
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

public class Exporter {

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
	private Document exportDoc = null;
	private String exportFileName = "";
	
	public Exporter(String exportFileName, String userFileName) {
		// data output file
		this.exportFileName = exportFileName;
		
		// load the user XML document
		userDoc = xmlUtils.getXmlFromFile(Config.DIR_DATA, userFileName);
		
		// create a new XML document for export
		exportDoc = xmlUtils.getNewDocument();
		
		// add default Connections application elements
		Element root = exportDoc.createElement("icda");
		exportDoc.appendChild(root);
		String[] apps = {"activities","blogs","bookmarks","communities","files","forums","profiles","wikis"};
		for (int x=0; x < apps.length; x++) {
			Element app = exportDoc.createElement(apps[x]);
			root.appendChild(app);
		}
		
		Element elem = (Element) exportDoc.getElementsByTagName("profiles").item(0);
		xmlUtils.createElement(exportDoc, elem, "colleagues", null, null);
	}
	
	// build activity xml
	private Element getActivityXML(Element parent, Activity activity, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "activity", null, false);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", activity.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", activity.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "content", activity.getContent(), true);
		if (activity.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(activity.getTags()), false); }
		if (activity.isComplete()) { xmlUtils.createElement(exportDoc, elem, "complete", "true", false); }
		return elem;
	}
	
	// build activity node xml
	private Element getActivityNodeXML(Element parent, ActivityNode node, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, node.getType(), null, null);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "title", node.getTitle(), false);
		// Connections API will return errors if it receives a section with a content node...
		if (!node.getType().equalsIgnoreCase("section")) {
			xmlUtils.createElement(exportDoc, elem, "content", node.getContent(), true);
		}
		// todos can have additional elements
		if (node.getType().equalsIgnoreCase("todo")) {
			if (node.isCompleted()) { xmlUtils.createElement(exportDoc, elem, "complete", "true", false); }
			try {
				// retrieve assigned UID; exception will be thrown if assigned is not in user XML file
				String assignedUid = Config.PROFILES_UUID.get(node.getAssignedToUuid()).getUid();
				Element elem2 = xmlUtils.createElement(exportDoc, elem, "assigned", null, null);
				elem2.setAttribute("user", assignedUid);
			} catch (Exception ex) {}
		}
		return elem;
	}
	
	// build blog xml
	private Element getBlogXML(Element parent, Blog blog, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, blog.getType(), null, false);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", blog.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", blog.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "handle", blog.getHandle(), false);
		xmlUtils.createElement(exportDoc, elem, "summary", stringUtils.removeHTML(blog.getSummary()), true);
		if (blog.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(blog.getTags()), false); }
		return elem;
	}

	// build blog post xml
	private Element getBlogPostXML(Element parent, BlogPost post, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "post", null, null);
		elem.setAttribute("user", authorUid);				
		xmlUtils.createElement(exportDoc, elem, "uuuid", post.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", post.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "content", post.getContent(), true);
		if (post.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(post.getTags()), false); }
		return elem;
	}

	// build bookmark xml
	private Element getBookmarkXML(Element parent, Bookmark bookmark, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "bookmark", null, false);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", bookmark.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", bookmark.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "content", bookmark.getContent(), true);
		xmlUtils.createElement(exportDoc, elem, "link", bookmark.getLink(), true);
		if (bookmark.isImportant()) { xmlUtils.createElement(exportDoc, elem, "important", "true", false); }
		if (bookmark.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(bookmark.getTags()), false); }
		return elem;
	}
	
	// build colleague xml
	private Element getColleagueXML(Element parent, Colleague colleague, String fromUid, String toUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "colleague", null, null);
		elem.setAttribute("to", toUid);
		elem.setAttribute("from", fromUid);
		elem.setAttribute("status", colleague.getStatus());
		elem.setAttribute("content", colleague.getContent());
		return elem;
	}
	
	// build community xml
	private Element getCommunityXML(Element parent, Community community, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "community", null, false);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", community.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", community.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "content", community.getContent(), true);
		xmlUtils.createElement(exportDoc, elem, "type", community.getType(), false);
		xmlUtils.createElement(exportDoc, elem, "isExternal", String.valueOf(community.isExternal()), false);
		if(!community.getLogo().trim().equalsIgnoreCase("")) { xmlUtils.createElement(exportDoc, elem, "logo", community.getLogo(), false); }
		if (community.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(community.getTags()), false); }
		return elem;
	}
	
	// build event xml
	private Element getEventXML(Element parent, Event event, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "event", null, null);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", event.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", event.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "content", event.getContent(), true);
		xmlUtils.createElement(exportDoc, elem, "location", event.getLocation(), false);
		xmlUtils.createElement(exportDoc, elem, "allDay", event.getAllDay(), false);
		xmlUtils.createElement(exportDoc, elem, "frequency", event.getFrequency(), false);
		xmlUtils.createElement(exportDoc, elem, "interval", event.getInterval(), false);
		xmlUtils.createElement(exportDoc, elem, "custom", event.getCustom(), false);
		xmlUtils.createElement(exportDoc, elem, "dateUntil", event.getDateUntil(), false);
		xmlUtils.createElement(exportDoc, elem, "dateStart", event.getDateStart(), false);
		xmlUtils.createElement(exportDoc, elem, "dateEnd", event.getDateEnd(), false);
		xmlUtils.createElement(exportDoc, elem, "byDay", event.getByDay(), false);
		return elem;
	}
	
	// build feed link xml
	private Element getFeedLinkXML(Element parent, FeedLink feedLink, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "feed", null, null);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", feedLink.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", feedLink.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "content", feedLink.getContent(), true);
		xmlUtils.createElement(exportDoc, elem, "link", feedLink.getLink(), true);
		if (feedLink.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(feedLink.getTags()), false); }
		return elem;
	}
	
	// build file xml
	private Element getFileXML(Element parent, File file, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "file", null, null);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", file.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "label", file.getLabel(), false);
		xmlUtils.createElement(exportDoc, elem, "type", file.getFileType(), false);
		xmlUtils.createElement(exportDoc, elem, "summary", file.getSummary(), true);
		xmlUtils.createElement(exportDoc, elem, "visibility", file.getVisibility(), false);
		if (file.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(file.getTags()), false); }
		return elem;
	}
	
	// build folder xml
	private Element getFolderXML(Element parent, Folder folder, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "folder", null, false);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", folder.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "label", folder.getLabel(), false);
		xmlUtils.createElement(exportDoc, elem, "summary", folder.getSummary(), true);
		xmlUtils.createElement(exportDoc, elem, "visibility", folder.getVisibility(), false);
		return elem;
	}
	
	// build forum xml
	private Element getForumXML(Element parent, Forum forum, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "forum", null, null);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", forum.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", forum.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "content", forum.getContent(), true);
		if (forum.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(forum.getTags()), false); }
		return elem;
	}
	
	// build forum topic xml
	private Element getForumTopicXML(Element parent, ForumTopic topic, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "topic", null, null);
		elem.setAttribute("user", authorUid);				
		xmlUtils.createElement(exportDoc, elem, "uuid", topic.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", topic.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "content", topic.getContent(), true);
		if (topic.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(topic.getTags()), false); }
		if (topic.isPinned()) { xmlUtils.createElement(exportDoc, elem, "pinned", "true", false); }
		if (topic.isLocked()) { xmlUtils.createElement(exportDoc, elem, "locked", "true", false); }
		if (topic.isQuestion()) { xmlUtils.createElement(exportDoc, elem, "question", "true", false); }
		if (topic.isAnswered()) { xmlUtils.createElement(exportDoc, elem, "answered", "true", false); }
		return elem;
	}
	
	// build forum topic reply xml
	private Element getForumTopicReplyXML(Element parent, ForumTopicReply reply, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "reply", null, null);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", reply.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", reply.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "content", reply.getContent(), true);
		if (reply.isAnswered()) { xmlUtils.createElement(exportDoc, elem, "answer", "true", false); }
		if (reply.isDeleted()) { xmlUtils.createElement(exportDoc, elem, "deleted", "true", false); }
		return elem;
	}
	
	// build member xml
	private Element getMemberXML(Element parent, Member member, String memberUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "member", null, null);
		elem.setAttribute("user", memberUid);
		elem.setAttribute("role", member.getRole());
		return elem;
	}
	
	// build wiki xml
	private Element getWikiXML(Element parent, Wiki wiki, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "wiki", null, null);
		elem.setAttribute("user", authorUid);
		xmlUtils.createElement(exportDoc, elem, "uuid", wiki.getuUid(), false); // dpc
		xmlUtils.createElement(exportDoc, elem, "title", wiki.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "summary", wiki.getSummary(), true);
		xmlUtils.createElement(exportDoc, elem, "label", wiki.getLabel(), false);
		xmlUtils.createElement(exportDoc, elem, "visibility", wiki.getVisibility(), false);
		return elem;
	}
	
	// build wiki page xml
	private Element getWikiPageXML(Element parent, WikiPage page, String authorUid) {
		Element elem = xmlUtils.createElement(exportDoc, parent, "page", null, null);
		elem.setAttribute("user", authorUid);				
		xmlUtils.createElement(exportDoc, elem, "uuid", page.getuUid(), false);
		xmlUtils.createElement(exportDoc, elem, "title", page.getTitle(), false);
		xmlUtils.createElement(exportDoc, elem, "label", page.getLabel(), false);
		xmlUtils.createElement(exportDoc, elem, "content", page.getContent(), true);
		if (page.getTags().size() > 0) { xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(page.getTags()), false); }
		return elem;
	}
	
	// write activities for the specified user to XML output file
	public void exportActivities(Profile profile) {
		printSection("Activities", profile);
		Element root = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/activities");
		// retrieve activities
		List<Activity> activities = svcActivity.getAllMyActivities(profile);
		// export activities
		for (Activity activity : activities) {
			// prevent duplicates by only exporting content created by current user
			if (activity.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				// if not community content
				if (activity.getCommunityUuid().equalsIgnoreCase("")) {
					exportActivity(root, activity, null, "");
				}
			}
		}
	}
		
	// export specified activity
	private void exportActivity(Element parent, Activity activity, Community community, String prefix) {
		try {
			Profile profile = Config.PROFILES_UUID.get(activity.getAuthorUuid());
			if (profile == null) { 
				profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
				activity.setAuthorUuid(community.getAuthorUuid());
			}
			String profileUid = profile.getUid();
			
			// build activity xml
			Element elem = getActivityXML(parent, activity, profileUid);
			System.out.println(prefix + "Retrieved activity [" + activity.getTitle() + "] for user [" + profile.getDisplayName() + "]");
			
			if (community == null) {
				// retrieve members
				List<Member> members = svcActivity.getMembers(activity, profile);
				exportMembers(elem, members, "  ");
			}
			
			// retrieve nodes for this activity
			List<ActivityNode> nodes = svcActivity.getNodes(activity, profile);
			if (nodes.size() > 0) {
				Element elem2 = xmlUtils.createElement(exportDoc, elem, "nodes", null, null);
				xmlUtils.createElement(exportDoc, elem2, "uuid", activity.getuUid(), null); // dpc
				exportActivityNodes(elem2, nodes, activity.getuUid(), prefix, activity);
			}
		} catch (Exception e) {}
	}
	
	// In order to understand recursion, one must first understand recursion.
	// The Activities API just gives you a single dump of nodes.  Add nodes to sections by checking parent uUids.
	private void exportActivityNodes(Element parent, List<ActivityNode> nodes, String parentUuid, String prefix, Activity activity) {
		for (ActivityNode node : nodes) {
			// only export nodes if it references the parent uUid
			if (node.getSectionUuid().equalsIgnoreCase(parentUuid)) {
				try {
					Profile profile = Config.PROFILES_UUID.get(node.getAuthorUuid());
					if (profile == null) { 
						profile = Config.PROFILES_UUID.get(activity.getAuthorUuid());
						node.setAuthorUuid(activity.getAuthorUuid());
					}
					String profileUid = profile.getUid();
					
					// export node
					Element elem = getActivityNodeXML(parent, node, profileUid);
					System.out.println(prefix + "  Retrieved " + node.getType() + " [" + node.getTitle() + "] for user [" + profile.getDisplayName() + "]");
					
					// run through this again to look for child nodes
					exportActivityNodes(elem, nodes, node.getuUid(), prefix + "  ", activity);
				} catch (Exception e) {}
			}
		}
	}
	
	// write retrieved blogs for the specified user to XML output file
	public void exportBlogs(Profile profile) {
		printSection("Blogs", profile);
		Element root = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/blogs");
		// retrieve blogs
		List<Blog> blogs = svcBlog.getMyBlogs(profile);
		// export blogs
		for (Blog blog : blogs) {
			// prevent duplicates by only exporting content created by current user
			if (blog.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				exportBlog(root, blog, null, "");
			}
		}
	}
	
	// export specified blog
	private void exportBlog(Element parent, Blog blog, Community community, String prefix) {
		try {
			Profile profile = Config.PROFILES_UUID.get(blog.getAuthorUuid());
			if (profile == null) { 
				profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
				blog.setAuthorUuid(community.getAuthorUuid());
			}
			String profileUid = profile.getUid();
			
			// export blog
			Element elem = getBlogXML(parent, blog, profileUid);
			System.out.println(prefix + "Retrieved " + blog.getType() + " [" + blog.getTitle() + "] for user [" + profile.getDisplayName() + "]");
			
			// retrieve posts for this blog
			List<BlogPost> posts = svcBlog.getBlogPosts(blog, profile);
			if (posts.size() > 0) {
				Element elem2 = xmlUtils.createElement(exportDoc, elem, "posts", null, null);
				exportBlogPosts(elem2, posts, blog, prefix + "  ");
			}
		} catch (Exception e) {}
	}
	
	// export blog posts for the specified blog
	private void exportBlogPosts(Element parent, List<BlogPost> posts, Blog blog, String prefix) {
		for (BlogPost post : posts) {
			try {
				// only export if user is listed in XML user file by forcing exception
				Profile postProfile = Config.PROFILES_UUID.get(post.getAuthorUuid());
				if (postProfile == null) { 
					postProfile = Config.PROFILES_UUID.get(blog.getAuthorUuid());
					post.setAuthorUuid(blog.getAuthorUuid());
				}
				String postProfileUid = postProfile.getUid();
				
				// export blog post
				Element elem = getBlogPostXML(parent, post, postProfileUid);
				xmlUtils.createElement(exportDoc, elem, "uuid", post.getuUid(), null); // dpc .. not sure about this
				System.out.println(prefix + "Retrieved post [" + post.getTitle() + "] for user [" + postProfile.getDisplayName() + "]");
				
				// retrieve likes for this blog post
				List<Recommendation> likes = new ArrayList<Recommendation>();
				if (blog.getType().equalsIgnoreCase("ideationblog")) {
					// ideation blog likes (votes) are considered confidential and cannot be retrieved in bulk via API
					// the workaround here is to pull the votes feed for each individual community member
					
					// retrieve a list of community members
					Community community = new Community();
					community.setuUid(blog.getCommunityUuid());
					List<Member> members = svcCommunity.getMembers(community, postProfile);
					// for each member, check the "myvotes" feed for the current blog post/idea
					for (Member member : members) {
						Profile memberProfile = Config.PROFILES_UUID.get(member.getuUid());
						boolean voted = svcBlog.votedForIdea(post, memberProfile);
						if (voted) {
							Recommendation vote = new Recommendation();
							vote.setOwnerUuid(memberProfile.getuUid());
							likes.add(vote);
						}
					}
				}
				else {
					likes = svcBlog.getBlogPostLikes(post, postProfile);
				}
				exportLikes(elem, likes, prefix + "  ");
				
				// retrieve comments for this blog post
				List<Comment> comments = svcBlog.getBlogPostComments(post, postProfile);
				if (comments.size() > 0) {
					Element elem2 = xmlUtils.createElement(exportDoc, elem, "comments", null, null);
					for (Comment comment : comments) {
						Element elem3 = exportComment(elem2, comment, prefix + "  ", postProfile);
						
						// retrieve likes for each of the blog post comments
						likes = svcBlog.getBlogPostCommentLikes(comment, post, postProfile);
						exportLikes(elem3, likes, prefix + "    ");
					}
				}
			} catch (Exception e) {}
		}
	}
	
	// write retrieved bookmarks to XML output file
	public void exportBookmarks(Profile profile) {
		printSection("Bookmarks", profile);
		Element root = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/bookmarks");
		// retrieve bookmarks
		List<Bookmark> bookmarks = svcBookmark.getMyBookmarks(profile);
		for (Bookmark bookmark : bookmarks) {
			// prevent duplicates by only exporting content created by current user
			if (bookmark.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				exportBookmark(root, bookmark, null, "");
			}
		}
	}
	
	// export specified bookmark
	private void exportBookmark(Element parent, Bookmark bookmark, Community community, String prefix) {
		try {
			Profile profile = Config.PROFILES_UUID.get(bookmark.getAuthorUuid());
			if (profile == null) { 
				profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
				bookmark.setAuthorUuid(community.getAuthorUuid());
			}
			String profileUid = profile.getUid();
			
			// export bookmark
			getBookmarkXML(parent, bookmark, profileUid);
			System.out.println(prefix + "Retrieved bookmark [" + bookmark.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		} catch (Exception e) {}
	}
	
	// write retrieved communities to XML output file
	public void exportCommunities(Profile profile) {
		printSection("Communities", profile);
		Element root = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/communities");
		// retrieve communities
		List<Community> communities = svcCommunity.getMyCommunities(profile);
		for (Community community : communities) {
			// prevent duplicates by only exporting content created by current user
			if (community.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				exportCommunity(root, community, profile);
			}
		}
	}
	
	public void exportCommunity(String communityUuid, Profile profile) {
		printSection("Communities", profile);
		Element root = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/communities");
		
		Community community = new Community();
		community.setuUid(communityUuid);
		community = svcCommunity.getCommunity(community, profile);
		
		exportCommunity(root, community, profile);
		
		// write XML to output file
		xmlUtils.writeXMLDocToFile(Config.DIR_DATA, exportFileName, exportDoc);
	}
	
	// write retrieved community to XML output file
	private void exportCommunity(Element parent, Community community, Profile profile) {
		try {
			Element elem = getCommunityXML(parent, community, profile.getUid());
			System.out.println("Retrieved community [" + community.getTitle() + "] for user [" + profile.getDisplayName() + "]");
			
			// retrieve members
			List<Member> members = svcCommunity.getMembers(community, profile);
			exportMembers(elem, members, "  ");
			
			// subcommunities navigation
			boolean subCommunityNav = false;
			
			// retrieve widgets
			List<Widget> widgets = svcCommunity.getWidgets(community, profile);
			for (Widget widget : widgets) {
				Element elem2 = null;
				
				// *** ACTIVITIES ***
				if (widget.getDefId().equalsIgnoreCase("activities")) {
					elem2 = xmlUtils.createElement(exportDoc, elem, "activities", null, null);
					// retrieve & export activities
					List<Activity> activities = svcActivity.getCommunityActivities(community, profile);
					for (Activity activity : activities) {
						exportActivity(elem2, activity, community, "  ");
					}
				}
				
				// *** BLOG ***
				if (widget.getDefId().equalsIgnoreCase("blog")) {
					elem2 = (Element) elem.getElementsByTagName("blogs").item(0);
					if (elem2 == null) { elem2 = xmlUtils.createElement(exportDoc, elem, "blogs", null, null); }
					// retrieve & export blog (there can only be one in a community)
					Blog blog = new Blog();
					blog.setAuthorUuid(community.getAuthorUuid());
					blog.setCommunityUuid(community.getuUid());
					blog.setHandle(community.getuUid());
					blog.setType("communityblog");
					blog = svcBlog.getBlog(blog, profile);
					//Blog blog = svcBlog.getCommunityBlog(community, profile);
					exportBlog(elem2, blog, community, "  ");
				}
				
				// *** IDEATION BLOGS ***
				if (widget.getDefId().equalsIgnoreCase("ideationblog")) {
					elem2 = (Element) elem.getElementsByTagName("blogs").item(0);
					if (elem2 == null) { elem2 = xmlUtils.createElement(exportDoc, elem, "blogs", null, null); }
					// retrieve & export ideation blogs
					List<Blog> blogs = svcBlog.getIdeationBlogs(community, profile);
					for (Blog blog : blogs) {
						exportBlog(elem2, blog, community, "  ");
					}
				}
				
				// *** BOOKMARKS ***
				if (widget.getDefId().equalsIgnoreCase("bookmarks")) {
					elem2 = xmlUtils.createElement(exportDoc, elem, "bookmarks", null, null);
					// retrieve & export bookmarks
					List<Bookmark> bookmarks = svcCommunity.getBookmarks(community, profile);
					for (Bookmark bookmark : bookmarks) {
						exportBookmark(elem2, bookmark, community, "  ");
					}
				}
				
				// *** EVENTS ***
				if (widget.getDefId().equalsIgnoreCase("calendar")) {
					elem2 = xmlUtils.createElement(exportDoc, elem, "calendar", null, null);
					// retrieve & export events
					List<Event> events = svcCommunity.getEvents(community, profile);
					for (Event event : events) {
						exportEvent(elem2, event, community, "  ");			
					}
				}
				
				// *** FEEDS ***
				if (widget.getDefId().equalsIgnoreCase("feeds")) {
					elem2 = xmlUtils.createElement(exportDoc, elem, "feeds", null, null);
					// retrieve & export feed links
					List<FeedLink> feedLinks = svcCommunity.getFeedLinks(community, profile);
					for (FeedLink feedLink : feedLinks) {
						exportFeedLink(elem2, feedLink, community, "  ");			
					}
				}
				
				// *** FILES ***
				if (widget.getDefId().equalsIgnoreCase("files")) {
					elem2 = xmlUtils.createElement(exportDoc, elem, "files", null, null);
					// retrieve & export files
					List<File> files = svcFile.getCommunityFiles(community, profile);
					for (File file : files) {
						exportFile(elem2, file, "  ", community);
					}
				}
				
				// *** FORUMS ***
				if (widget.getDefId().equalsIgnoreCase("forum")) {
					elem2 = xmlUtils.createElement(exportDoc, elem, "forums", null, null);
					// retrieve & export forums
					List<Forum> forums = svcForum.getCommunityForums(community, profile);
					for (Forum forum : forums) {
						exportForum(elem2, forum, "  ", community);
					}
				}
				
				// *** WIKIS ***
				if (widget.getDefId().equalsIgnoreCase("wiki")) {
					elem2 = xmlUtils.createElement(exportDoc, elem, "wikis", null, null);
					// retrieve & export wiki (there can only be one in a community)
					Wiki wiki = svcWiki.getCommunityWiki(community, profile);
					// a community wiki returns a blank author uUid (00000000-0000-0000-0000-000000000000), so replace with community owner
					wiki.setAuthorUuid(community.getAuthorUuid());
					exportWiki(elem2, wiki, community, "  ");
				}

				// *** SUBCOMMUNITIES NAV ***
				if (widget.getDefId().equalsIgnoreCase("subcommunitynav")) {
					subCommunityNav = true;
				}
				
				/*
				// widget properties
				if (elem2 != null) {
					elem2.setAttribute("location", widget.getLocation());
					elem2.setAttribute("title", widget.getTitle());
				}
				*/
			}
			
			// *** SUBCOMMUNITIES ***
			Element elem3 = xmlUtils.createElement(exportDoc, elem, "subcommunities", null, null);
			elem3.setAttribute("nav", String.valueOf(subCommunityNav));
			// retrieve subcommunities
			List<Community> subcommunities = svcCommunity.getSubCommunities(community, profile);
			for (Community subcommunity : subcommunities) {
				exportCommunity(elem3, subcommunity, profile);
			}
			
			
		} catch (Exception e) {}
	}
	
	// export calendar events from Communities app/widget
	private void exportEvent(Element parent, Event event, Community community, String prefix) {
		try {
			Profile profile = Config.PROFILES_UUID.get(event.getAuthorUuid());
			if (profile == null) { 
				profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
				event.setAuthorUuid(community.getAuthorUuid());
			}
			String profileUid = profile.getUid();
			
			// export event
			getEventXML(parent, event, profileUid);
			System.out.println(prefix + "Retrieved calendar event [" + event.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		} catch (Exception e) {}
	}
	
	// export feed links from Communities app/widget
	private void exportFeedLink(Element parent, FeedLink feedLink, Community community, String prefix) {
		try {
			Profile profile = Config.PROFILES_UUID.get(feedLink.getAuthorUuid());
			if (profile == null) { 
				profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
				feedLink.setAuthorUuid(community.getAuthorUuid());
			}
			String profileUid = profile.getUid();
			
			// export feed link
			getFeedLinkXML(parent, feedLink, profileUid);
			System.out.println(prefix + "Retrieved feed link [" + feedLink.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		} catch (Exception e) {}
	}
	
	// write retrieved files & folders to XML output file
	public void exportFiles(Profile profile) {
		printSection("Files", profile);
		Element root = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/files");
		String uids = "";
		
		// folders and any files they contain
		List<Folder> folders = svcFile.getMyFolders(profile);
		for (Folder folder : folders) {
			// prevent duplicates by only exporting content created by current user
			if (folder.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				Element elem = getFolderXML(root, folder, profile.getUid());
				System.out.println("Retrieved folder [" + folder.getLabel() + "] for user [" + profile.getDisplayName() + "]");
				
				// retrieve files in this folder
				List<File> folderFiles = svcFile.getFolderFiles(folder, profile);
				if (folderFiles.size() > 0) {
					Element elem2 = xmlUtils.createElement(exportDoc, elem, "files", null, null);
					for (File file : folderFiles) {
						exportFile(elem2, file, "  ", null);
						uids += "," + file.getuUid();
					}
				}
			}
		}
		
		// stand-alone files; not in a folder
		List<File> files = svcFile.getMyFiles(profile);
		if (files.size() > 0) { printSection("Files", profile); }
		for (File file : files) {
			// prevent duplicates by only exporting content created by current user
			if (file.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				// if not already exported (in this case, in one of the folders above)
				if (!uids.contains(file.getuUid())) {
					exportFile(root, file, "", null);
				}
			}
		}
	}
	
	// write retrieved file to XML output file
	private void exportFile(Element parent, File file, String prefix, Community community) {
		try {
			Profile profile = Config.PROFILES_UUID.get(file.getAuthorUuid());
			if (profile == null) { 
				profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
				file.setAuthorUuid(community.getAuthorUuid());
			}
			String profileUid = profile.getUid();
		
			// build the file xml
			Element elem = getFileXML(parent, file, profileUid);
			System.out.println(prefix + "Retrieved file metadata [" + file.getLabel() + "] for user [" + profile.getDisplayName() + "]");
			
			// attempt to download and store the actual file
			file = svcFile.downloadFile(file, profile);
			
			// retrieve comments for this file
			List<Comment> comments = svcFile.getFileComments(file, profile);
			exportComments(elem, comments, prefix + "  ", profile);
		} catch (Exception e) {}
	}
	
	// write retrieved forums to XML output file
	public void exportForums(Profile profile) {
		printSection("Forums", profile);
		Element root = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/forums");
		// retrieve forums
		List<Forum> forums = svcForum.getMyForums(profile);
		for (Forum forum : forums) {
			// prevent duplicates by only exporting content created by current user
			if (forum.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				exportForum(root, forum, "", null);
			}
		}
	}
	
	// write retrieved forums to XML output file
	private void exportForum(Element parent, Forum forum, String prefix, Community community) {
		try {
			Profile profile = Config.PROFILES_UUID.get(forum.getAuthorUuid());
			if (profile == null) { 
				profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
				forum.setAuthorUuid(community.getAuthorUuid());
			}
			String profileUid = profile.getUid();
			
			// export forum
			Element elem = getForumXML(parent, forum, profileUid);
			System.out.println(prefix + "Retrieved forum [" + forum.getTitle() + "] for user [" + profile.getDisplayName() + "]");
			
			// retrieve topics for this forum
			List<ForumTopic> topics = svcForum.getTopics(forum, profile);
			if (topics.size() > 0) {
				Element elem2 = xmlUtils.createElement(exportDoc, elem, "topics", null, null);
				exportForumTopics(elem2, topics, prefix + "  ", forum);
			}
		} catch (Exception e) {}
	}
	
	// export topics for the specified forum
	private void exportForumTopics(Element parent, List<ForumTopic> topics, String prefix, Forum forum) {
		for (ForumTopic topic : topics) {
			try {
				Profile user = Config.PROFILES_UUID.get(topic.getAuthorUuid());
				if (user == null) { 
					user = Config.PROFILES_UUID.get(forum.getAuthorUuid());
					topic.setAuthorUuid(forum.getAuthorUuid());
				}
				String uid = user.getUid();
				
				// build the forum topic xml						
				Element elem = getForumTopicXML(parent, topic, uid);
				System.out.println(prefix + "Retrieved topic [" + topic.getTitle() + "] for user [" + user.getDisplayName() + "]");
				
				// retrieve likes for this forum topic
				List<Recommendation> likes = svcForum.getForumTopicLikes(topic, user);
				exportLikes(elem, likes, prefix + "  ");
				
				// retrieve replies to this forum topic
				List<ForumTopicReply> replies = svcForum.getForumTopicReplies(topic, user);
				if (replies.size() > 0) {
					Element elem2 = xmlUtils.createElement(exportDoc, elem, "replies", null, null);
					exportForumReplies(elem2, replies, topic.getuUid(), prefix + "  ", topic);
				}
			} catch (Exception e) {}
		}
	}
	
	// In order to understand recursion, one must first understand recursion.
	// The Forums API just gives you a single dump of replies.  Threads can only be reassembled by checking parent uUids
	private void exportForumReplies(Element parent, List<ForumTopicReply> replies, String parentUuid, String prefix, ForumTopic topic) {
		for (ForumTopicReply reply: replies) {
			// only export replies if it references the parent uUid
			if (reply.getTopicUuid().equalsIgnoreCase(parentUuid)) {
				try {
					// only export if content owner is in user XML file
					Profile replyProfile = Config.PROFILES_UUID.get(reply.getAuthorUuid());
					if (replyProfile == null) { 
						replyProfile = Config.PROFILES_UUID.get(topic.getAuthorUuid());
						reply.setAuthorUuid(topic.getAuthorUuid());
					}
					String replyProfileUid = replyProfile.getUid();
					
					// build the forum topic reply xml
					Element elem = getForumTopicReplyXML(parent, reply, replyProfileUid);					
					System.out.println(prefix + "Retrieved reply [" + reply.getTitle() + "] for user [" + replyProfile.getDisplayName() + "]");
					
					// retrieve likes for this forum topic reply
					List<Recommendation> likes = svcForum.getForumTopicReplyLikes(reply, replyProfile);
					exportLikes(elem, likes, prefix + "  ");
					
					// run through this again to look for replies to the current reply
					exportForumReplies(elem, replies, reply.getuUid(), prefix + "  ", topic);
					
				} catch (Exception e) {}
			}
		}
	}
	
	// write retrieved profiles data to XML output file
	public void exportProfile(Profile profile) {
		printSection("Profiles", profile);
		Element root = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/profiles");
		Element collElem = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/profiles/colleagues");
		
		// retrieve any tags for the profile
		List<ProfileTag> tags = svcProfile.getTags(profile);
		if (tags.size() > 0) {
			// build the profile xml
			Element elem = xmlUtils.createElement(exportDoc, root, "profile", null, null);
			elem.setAttribute("user", profile.getUid());
			
			// write CSV list of tags to xml
			List<String> output = new ArrayList<String>();
			for (ProfileTag tag : tags) {
				output.add(tag.getTerm());
			}
			xmlUtils.createElement(exportDoc, elem, "tags", stringUtils.join(output), false);
			System.out.println("Retrieved " + tags.size() + " profile tags for user [" + profile.getDisplayName() + "]");
		}
		
		// retrieve any network connections for the profile
		List<Colleague> colleagues = svcProfile.getAllColleagues(profile);
		for (Colleague colleague : colleagues) {
			// prevent duplicates, use the receiver's connection since it has data in the content element
			if (!colleague.getOwnerUuid().equalsIgnoreCase(colleague.getFromUuid())) {	
				try {
					// yes, this seems backwards!  See getColleagues function in ProfileService class for details
					String fromUid = Config.PROFILES_UUID.get(colleague.getToUuid()).getUid();
					String toUid = Config.PROFILES_UUID.get(colleague.getFromUuid()).getUid();
					
					// build the tag xml
					getColleagueXML(collElem, colleague, fromUid, toUid);
					System.out.println("Retrieved " + colleague.getStatus() + " connection from [" + fromUid + "] to [" + toUid + "]");
				} catch (Exception e) {}
			}
		}
	}
	
	// write retrieved wikis to XML output file
	public void exportWikis(Profile profile) {
		printSection("Wikis", profile);
		Element root = (Element) xmlUtils.getNodeByXPath(exportDoc, "/icda/wikis");
		List<Wiki> wikis = svcWiki.getMyWikis(profile);
		for (Wiki wiki : wikis) {
			// prevent duplicates by only exporting content created by current user
			if (wiki.getAuthorUuid().equalsIgnoreCase(profile.getuUid())) {
				exportWiki(root, wiki, null, "");
			}
		}
	}
	
	// write wiki to the XML output file
	private void exportWiki(Element parent, Wiki wiki, Community community, String prefix) {
		try {
			Profile profile = Config.PROFILES_UUID.get(wiki.getAuthorUuid());
			if (profile == null) { 
				profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
				wiki.setAuthorUuid(community.getAuthorUuid());
			}
			String profileUid = profile.getUid();
			
			// export wiki 
			Element elem = getWikiXML(parent, wiki, profileUid);
			System.out.println(prefix + "Retrieved wiki [" + wiki.getTitle() + "] for user [" + profile.getDisplayName() + "]");
			
			if (community == null) {
				// retrieve members for this wiki
				wiki = svcWiki.getMembers(wiki, profile);
				exportMembers(elem, wiki.getMembers(), "  ");
			}
			
			// retrieve wiki pages for this wiki
			List<WikiPage> pages = svcWiki.getWikiPages(wiki, profile);
			if (pages.size() > 0) {
				Element elem2 = xmlUtils.createElement(exportDoc, elem, "pages", null, null);
				exportWikiPages(elem2, pages, prefix + "  ", wiki);
			}
		} catch (Exception e) {}
	}
	
	// export wiki pages for the specified wiki
	private void exportWikiPages(Element parent, List<WikiPage> pages, String prefix, Wiki wiki) {
		for (WikiPage page : pages) {
			try {
				Profile user = Config.PROFILES_UUID.get(page.getAuthorUuid());
				if (user == null) { 
					user = Config.PROFILES_UUID.get(wiki.getAuthorUuid());
					page.setAuthorUuid(wiki.getAuthorUuid());
				}
				String uid = user.getUid();
				
				// build the wiki page xml
				Element elem = getWikiPageXML(parent, page, uid);
				System.out.println(prefix + "Retrieved page [" + page.getTitle() + "] for user [" + user.getDisplayName() + "]");
				
				// retrieve comments for this wiki page
				List<Comment> comments = svcWiki.getWikiPageComments(page, user);
				exportComments(elem, comments, prefix + "  ", user);

			} catch (Exception ex) {}
		}
	}
	
	// write retrieved members to XML output file
	public void exportMembers(Element parent, List<Member> members, String prefix) {
		if (members.size() > 0) {
			Element root = xmlUtils.createElement(exportDoc, parent, "members", null, null);
			for (Member member : members) {
				try {
					// retrieve member UID; exception will be thrown if member is not in user XML file
					Profile profile = Config.PROFILES_UUID.get(member.getuUid());
					String profileUid = profile.getUid();
					
					// build member xml
					getMemberXML(root, member, profileUid);
					System.out.println(prefix + "Retrieved " + member.getRole() + " [" + profile.getDisplayName() + "]");
				} catch (Exception ex) {}
			}
		}
	}
	
	// write retrieved comments to XML output file
	public void exportComments(Element parent, List<Comment> comments, String prefix, Profile profile) {
		if (comments.size() > 0) {
			Element root = xmlUtils.createElement(exportDoc, parent, "comments", null, null);
			for (Comment comment : comments) {
				exportComment(root, comment, prefix, profile);
			}
		}
	}
	
	// write retrieved comment to XML output file
	public Element exportComment(Element parent, Comment comment, String prefix, Profile parentProfile) {
		Element elem = null;
		try {
			Profile profile = Config.PROFILES_UUID.get(comment.getOwnerUuid());
			if (profile == null) { 
				profile = parentProfile;
			}
			String ownerUid = profile.getUid();
			
			// build the comment xml
			elem = xmlUtils.createElement(exportDoc, parent, "comment", null, null);
			elem.setAttribute("user", ownerUid);
			xmlUtils.createElement(exportDoc, elem, "content", comment.getContent(), true);
			
			System.out.println(prefix + "Retrieved comment for user [" + profile.getDisplayName() + "]");
		} catch (Exception ex) {}
		return elem;
	}
	
	// write retrieved likes to XML output file
	public void exportLikes(Element parent, List<Recommendation> likes, String prefix) {
		if (likes.size() > 0) {
			Element root = xmlUtils.createElement(exportDoc, parent, "likes", null, null);
			for (Recommendation like : likes) {
				try {
					// only export if user is listed in XML user file by forcing exception
					Profile profile = Config.PROFILES_UUID.get(like.getOwnerUuid());
					String ownerUid = profile.getUid();
					
					// build the like xml
					Element elem = xmlUtils.createElement(exportDoc, root, "like", null, null);
					elem.setAttribute("user", ownerUid);
					
					System.out.println(prefix + "Retrieved like for user [" + profile.getDisplayName() + "]");
				} catch (Exception ex) {}
			}
		}
	}
	
	// export all content for the specified users and applications
	public void exportContent() {
		// for each user
		for (Map.Entry<String, Profile> entry: Config.PROFILES.entrySet()) {
			Profile profile = (Profile) entry.getValue();
			
			// if the user in the export section of the XML file, add the user's content to the data list
			if (idFound("activities", profile)) { exportActivities(profile); }
			if (idFound("communities", profile)) { exportCommunities(profile); }
			if (idFound("files", profile)) { exportFiles(profile); }
			if (idFound("profiles", profile)) { exportProfile(profile); }
			
			if (Config.ENVIRONMENT.equalsIgnoreCase("on-premise")) {
				if (idFound("blogs", profile)) { exportBlogs(profile); }
				if (idFound("bookmarks", profile)) { exportBookmarks(profile); }
				if (idFound("forums", profile)) { exportForums(profile); }
				if (idFound("wikis", profile)) { exportWikis(profile); }
			}
			
		}
		
		// write XML to output file
		xmlUtils.writeXMLDocToFile(Config.DIR_DATA, exportFileName, exportDoc);
	}
	
	private void printSection(String app, Profile profile) {
		System.out.println("");
		System.out.println("[Exporting " + app + " for " + profile.getDisplayName() + "]");
	}
	
	// is the uid for the given profile listed delete section of the user XML file
	private boolean idFound(String app, Profile profile) {
		String userids = xmlUtils.getStringByXPath(userDoc, "/icda/export/" + app);
		boolean result = stringUtils.stringContainsUid(userids, profile.getUid());
		return result;
	}
}
