package us.godby.icda.app;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

public class Importer {

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
	private Document importDoc = null;
	
	// global Connections data (workaround for Communities-to-Apps ACL propagation issues)
	private Map<Community,List<Activity>> communityActivityMap = new TreeMap<Community, List<Activity>>(new Comparator<Community>() {
		public int compare(Community c1, Community c2) {
			return c1.getTitle().compareTo(c2.getTitle());
		}
	});
	private List<Blog> communityBlogs = new ArrayList<Blog>();
	private List<Blog> ideationBlogs = new ArrayList<Blog>();
	
		
	public Importer(String importFileName, String userFileName) {		
		// load the user XML document
		userDoc = xmlUtils.getXmlFromFile(Config.DIR_DATA, userFileName);
		// load the data XML document
		importDoc = xmlUtils.getXmlFromFile(Config.DIR_DATA, importFileName);
	}
	
	// build activity object from xml fragment
	private Activity getActivity(Element elem) {
		Activity activity = new Activity();
		activity.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		activity.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		try { activity.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		try { activity.setComplete(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./complete"))); } catch (Exception e) {}
		return activity;
	}
	
	// build activity node object from xml fragment
	private ActivityNode getActivityNode(Element elem, String type) {
		ActivityNode an = new ActivityNode();
		an.setType(type);
		an.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		// sections do not have a content element, all other nodes do
		if (!an.getType().equalsIgnoreCase("section")) {
			an.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		}
		// todos can have additional elements
		if (an.getType().equalsIgnoreCase("todo")) {
			try { an.setCompleted(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./complete"))); } catch (Exception e) {}
			try {
				Profile assignedProfile = Config.PROFILES.get(xmlUtils.getStringByXPath(elem, "./assigned/@user"));
				an.setAssignedToUuid(assignedProfile.getuUid());
			} catch (Exception e) {}
		}
		return an;
	}
	
	// build blog object from xml fragement
	private Blog getBlog(Element elem, String type) {
		Blog blog = new Blog();
		blog.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		blog.setHandle(xmlUtils.getStringByXPath(elem, "./handle"));
		blog.setSummary(stringUtils.removeHTML(xmlUtils.getStringByXPath(elem, "./summary")));
		blog.setType(type);
		try { blog.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		return blog;
	}
	
	// build blog post object from xml fragment
	private BlogPost getBlogPost(Element elem) {
		BlogPost post = new BlogPost();
		post.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		post.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		try { post.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		return post;
	}
	
	// build bookmark object from xml fragment
	private Bookmark getBookmark(Element elem) {
		Bookmark bookmark = new Bookmark();
		bookmark.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		bookmark.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		bookmark.setLink(xmlUtils.getStringByXPath(elem, "./link"));
		try { bookmark.setImportant(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./important"))); } catch (Exception e) {}
		try { bookmark.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		return bookmark;
	}
	
	// build community object from xml fragment
	private Community getCommunity(Element elem) {
		Community community = new Community();
		community.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		community.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		community.setType(xmlUtils.getStringByXPath(elem, "./type"));
		community.setExternal(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./isExternal")));
		try { community.setLogo(xmlUtils.getStringByXPath(elem, "./logo")); } catch (Exception e) {}
		try { community.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		return community;
	}
	
	// build community event object from xml fragment
	private Event getEvent(Element elem) {
		Event event = new Event();		
		event.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		event.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		event.setLocation(xmlUtils.getStringByXPath(elem, "./location"));
		event.setAllDay(xmlUtils.getStringByXPath(elem, "./allDay"));
		event.setFrequency(xmlUtils.getStringByXPath(elem, "./frequency"));
		event.setInterval(xmlUtils.getStringByXPath(elem, "./interval"));
		event.setCustom(xmlUtils.getStringByXPath(elem, "./custom"));
		event.setDateUntil(xmlUtils.getStringByXPath(elem, "./dateUntil"));
		event.setDateStart(xmlUtils.getStringByXPath(elem, "./dateStart"));
		event.setDateEnd(xmlUtils.getStringByXPath(elem, "./dateEnd"));
		event.setByDay(xmlUtils.getStringByXPath(elem, "./byDay"));
		return event;
	}
	
	// build community feed link object from xml fragment
	private FeedLink getFeedLink(Element elem) {
		FeedLink feed = new FeedLink();
		feed.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		feed.setLink(xmlUtils.getStringByXPath(elem, "./link"));
		feed.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		try { feed.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		return feed;
	}
	
	// build file object from xml fragment
	private File getFile(Element elem) {
		// create file
		File file = new File();
		file.setLabel(xmlUtils.getStringByXPath(elem, "./label"));
		try { file.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		file.setSummary(xmlUtils.getStringByXPath(elem, "./summary"));
		file.setVisibility(xmlUtils.getStringByXPath(elem, "./visibility"));
		try { file.setAuthorUuid(Config.PROFILES.get(elem.getAttribute("user")).getuUid()); } catch(Exception e) {}
		return file;
	}
	
	// build folder object from xml fragment
	private Folder getFolder(Element elem) {
		Folder folder = new Folder();
		folder.setLabel(xmlUtils.getStringByXPath(elem, "./label"));
		folder.setSummary(xmlUtils.getStringByXPath(elem, "./summary"));
		folder.setVisibility(xmlUtils.getStringByXPath(elem, "./visibility"));
		return folder;
	}
	
	// build forum object from xml fragment
	private Forum getForum(Element elem) {
		Forum forum = new Forum();
		forum.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		forum.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		try { forum.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		return forum;
	}
	
	// build forum topic object from xml fragment
	private ForumTopic getForumTopic(Element elem) {
		ForumTopic topic = new ForumTopic();
		topic.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		topic.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		try { topic.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		try { topic.setAnswered(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./answered"))); } catch (Exception e) {}
		try { topic.setLocked(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./locked"))); } catch (Exception e) {}
		try { topic.setPinned(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./pinned"))); } catch (Exception e) {}
		try { topic.setQuestion(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./question"))); } catch (Exception e) {}
		return topic;
	}
	
	// build forum topic reply object from xml fragment
	private ForumTopicReply getForumTopicReply(Element elem, String parentUuid) {
		ForumTopicReply reply = new ForumTopicReply();
		reply.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		reply.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		try { reply.setAnswered(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./answer"))); } catch (Exception e) {}
		try { reply.setDeleted(Boolean.valueOf(xmlUtils.getStringByXPath(elem, "./deleted"))); } catch (Exception e) {}
		reply.setTopicUuid(parentUuid);
		return reply;
	}
	
	// build member object from xml fragment
	private Member getMember(Element elem, String component, String uid) {
		Member member = new Member();
		member.setComponent(component);
		member.setRole(elem.getAttribute("role"));
		member.setuUid(Config.PROFILES.get(uid).getuUid());
		return member;
	}
	
	// get all members from xml
	private List<Member> getMembers(NodeList memberNodes, String component) {
		List<Member> list = new ArrayList<Member>();
		for (int y=0; y < memberNodes.getLength(); y++) {
			Element elem2 = (Element) memberNodes.item(y);
			String uid = elem2.getAttribute("user");
			Member member = getMember(elem2, component, uid);
			list.add(member);
		}
		return list;
	}
	
	// get wiki object from xml fragment
	private Wiki getWiki(Element elem) {
		Wiki wiki = new Wiki();
		wiki.setLabel(xmlUtils.getStringByXPath(elem, "./label"));
		wiki.setSummary(xmlUtils.getStringByXPath(elem, "./summary"));
		wiki.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		wiki.setVisibility(xmlUtils.getStringByXPath(elem, "./visibility"));
		return wiki;
	}
	
	// get wiki page object from xml fragment
	private WikiPage getWikiPage(Element elem) {
		WikiPage page = new WikiPage();
		page.setTitle(xmlUtils.getStringByXPath(elem, "./title"));
		page.setLabel(xmlUtils.getStringByXPath(elem, "./label"));
		//page.setContent(stringUtils.removeEntities(xmlUtils.getStringByXPath(elem, "./content")));
		page.setContent(xmlUtils.getStringByXPath(elem, "./content"));
		try { page.setTags(stringUtils.explode(xmlUtils.getStringByXPath(elem, "./tags"), ",")); } catch (Exception e) {}
		return page;
	}
	
	// import all activities for the specified user
	public void importActivities(Profile profile) {
		printSection("Activities", profile);
		NodeList nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/activities/activity[@user='" + profile.getUid() + "']");
		importActivities(nodes, null);
	}
	
	private void importActivities(NodeList nodes, Community community) {
		for (int x=0; x < nodes.getLength(); x++) {
			importActivity(nodes.item(x), community);
		}
	}
	
	private void importActivity(Node node, Community community) {
		Element elem = (Element) node;
		
		// create activity
		Profile profile = Config.PROFILES.get(elem.getAttribute("user"));
		Activity activity = getActivity(elem);
		if (community == null) {
			activity = svcActivity.createActivity(activity, profile);
			if (activity.getuUid() == "") { return; }
			
			// add members to the activity
			NodeList memberNodes = xmlUtils.getNodeListByXPath(elem, "./members/member");
			List<Member> members = getMembers(memberNodes, "activities");
			for (Member member : members) {
				svcActivity.addMember(activity, member, profile);
			}
		}
		else {
			activity = svcActivity.createCommunityActivity(activity, community, profile);
		}
		if (activity.getuUid() == "") { return; }
		
		// add child nodes to the top level of the activity
		String[] nodeTypes = {"section", "entry", "todo"};
		for (int x = 0; x < nodeTypes.length; x++) {
			NodeList nl = xmlUtils.getNodeListByXPath(elem, "./nodes/" + nodeTypes[x]);
			importActivityNodes(nl, nodeTypes[x], activity, null);
		}
	}
	
	private void importActivityNodes(NodeList nodes, String type, Activity activity, ActivityNode parent) {
		for (int x=0; x < nodes.getLength(); x++) {
			importActivityNode(nodes.item(x), type, activity, parent);
		}
	}
	
	private void importActivityNode(Node node, String type, Activity activity, ActivityNode parent) {
		Element elem = (Element) node;
		
		// create the activity node
		Profile profile = Config.PROFILES.get(elem.getAttribute("user"));
		ActivityNode an = getActivityNode(elem, type);
		an.setActivityUuid(activity.getuUid());
		if (parent != null) {
			an.setSectionUuid(parent.getuUid());
		}
		an = svcActivity.createActivityNode(an, profile);
		if (an.getuUid() == "") { return; }
		
		// add child nodes to the current node
		String[] nodeTypes = {"entry", "todo", "reply"};
		for (int x = 0; x < nodeTypes.length; x++) {
			NodeList nl = xmlUtils.getNodeListByXPath(elem, "./" + nodeTypes[x]);
			importActivityNodes(nl, nodeTypes[x], activity, an);
		}
	}
	
	// import all blogs for the specified user
	public void importBlogs(Profile profile) {
		printSection("Blogs", profile);
		NodeList nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/blogs/blog[@user='" + profile.getUid() + "']");
		importBlogs(nodes, null);
	}
	
	private void importBlogs(NodeList nodes, Community community) {
		for (int x=0; x < nodes.getLength(); x++) {
			importBlog(nodes.item(x), community);
		}
	}
	
	private void importBlog(Node node, Community community) {
		Element elem = (Element) node;
		
		// create the blog
		Profile profile = Config.PROFILES.get(elem.getAttribute("user"));
		Blog blog = getBlog(elem, elem.getNodeName());
		if (community == null) {
			blog = svcBlog.createBlog(blog, profile);
		}
		else if (blog.getType().equalsIgnoreCase("communityblog")) {
			// In this scenario, the blog is created when the App is added to the community.  So, just update.
			blog.setCommunityUuid(community.getuUid());
			blog.setHandle(community.getuUid());
			blog = svcBlog.updateBlog(blog, profile);
		}
		else if (blog.getType().equalsIgnoreCase("ideationblog")) {
			// In this scenario, the blog is created when the App is added to the community.  So, just update.
			// TEMPORARY:  only supports a single ideation blog
			blog.setCommunityUuid(community.getuUid());
			List<Widget> widgets = svcCommunity.getWidgets(community, profile);
			for (Widget widget : widgets) {
				if (widget.getDefId().equalsIgnoreCase("ideationblog")) { blog.setHandle(widget.getInstanceID()); }
			}
			//blog = svcBlog.createIdeationBlog(blog, community, profile);
			blog = svcBlog.updateBlog(blog, profile);
		}
		if (blog.getuUid() == "") { return; }
		
		// add posts to the blog
		NodeList postNodes = xmlUtils.getNodeListByXPath(elem, "./posts/post");
		importBlogPosts(postNodes, blog);
	}	
	
	private void importBlogPosts(NodeList postNodes, Blog blog) {
		for (int y=0; y < postNodes.getLength(); y++) {
			importBlogPost(postNodes.item(y), blog);
		}
	}
	
	private void importBlogPost(Node node, Blog blog) {
		Element elem2 = (Element) node;
		
		// create blog post
		Profile postProfile = Config.PROFILES.get(elem2.getAttribute("user"));
		BlogPost post = getBlogPost(elem2);
		post.setAuthorUuid(postProfile.getuUid());
		post.setBlogHandle(blog.getHandle());
		post = svcBlog.createBlogPost(post, postProfile);
		
		// add likes to the blog post
		NodeList likeNodes = xmlUtils.getNodeListByXPath(elem2, "./likes/like");
		for (int z=0; z < likeNodes.getLength(); z++) {
			Element elem3 = (Element) likeNodes.item(z);
			
			Profile likeProfile = Config.PROFILES.get(elem3.getAttribute("user"));
			svcBlog.createBlogPostLike(post, likeProfile);
		}
		if (post.getuUid() == "") { return; }
		
		// add comments to the blog post
		NodeList commentNodes = xmlUtils.getNodeListByXPath(elem2, "./comments/comment");
		for (int z=0; z < commentNodes.getLength(); z++) {
			Element elem3 = (Element) commentNodes.item(z);
				
			// create comment
			Profile commentProfile = Config.PROFILES.get(elem3.getAttribute("user"));
			Comment comment = new Comment();
			comment.setRefId(post.getuUid());
			comment.setRefType("blogpost");
			comment.setContent(xmlUtils.getStringByXPath(elem3, "./content"));
			comment = svcBlog.createBlogPostComment(post, comment, commentProfile);
				
			// add likes to the blog post comment
			likeNodes = xmlUtils.getNodeListByXPath(elem3, "./likes/like");
			for (int a=0; a < likeNodes.getLength(); a++) {
				Element elem4 = (Element) likeNodes.item(a);
				
				Profile likeProfile = Config.PROFILES.get(elem4.getAttribute("user"));
				svcBlog.createBlogPostCommentLike(post, comment, likeProfile);
			}
		}
	}
	
	// import bookmarks for the specified user
	public void importBookmarks(Profile profile) {
		printSection("Bookmarks", profile);
		NodeList nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/bookmarks/bookmark[@user='" + profile.getUid() + "']");
		importBookmarks(nodes, null);
	}
	
	private void importBookmarks(NodeList nodes, Community community) {
		for (int x=0; x < nodes.getLength(); x++) {
			Element elem = (Element) nodes.item(x);
			
			// create the bookmark
			Profile profile = Config.PROFILES.get(elem.getAttribute("user"));
			Bookmark bookmark = getBookmark(elem);
			if (community == null) {
				bookmark = svcBookmark.createBookmark(bookmark, profile);
			}
			else {
				bookmark = svcCommunity.createBookmark(bookmark, community, profile);
			}
		}
	}
	
	// =======================================
	// Communities - with workarounds for Community-to-App ACL propagation issues
	// - It can take up to 10 minutes(!) for ACLs to propagate to specific Apps
	// =======================================
	public void importCommunitiesPRE() {
		// for each user
		for (Map.Entry<String, Profile> entry: Config.PROFILES.entrySet()) {
			Profile profile = (Profile) entry.getValue();
			// if the user is marked for community import
			if (idFound("communities", profile)) {
				NodeList nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/communities/community[@user='" + profile.getUid() + "']");
				importCommunitiesPRE(nodes, profile, null);
			}
		}
	}
	
	private void importCommunitiesPRE(NodeList nodes, Profile profile, Community parentCommunity) {
		for (int x=0; x < nodes.getLength(); x++) {
			Element elem = (Element) nodes.item(x);
					
			// create community
			Community community = getCommunity(elem);
			if (parentCommunity == null) {
				community = svcCommunity.createCommunity(community, profile);
			}
			else {
				community = svcCommunity.createSubCommunity(community, parentCommunity, profile);				
			}
			if (community.getuUid() == "") { return; }
					
			// update logo
			svcCommunity.updateLogo(community, profile);
					
			// default widgets:  Bookmarks, Forums, Files
			// add required non-default widgets
			NodeList nl = xmlUtils.getNodeListByXPath(elem, "./activities/activity");
			if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("Activities"), profile); }
			nl = xmlUtils.getNodeListByXPath(elem, "./blogs/communityblog");
			if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("Blog"), profile); }
			nl = xmlUtils.getNodeListByXPath(elem, "./blogs/ideationblog");
			if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("IdeationBlog"), profile); }
			nl = xmlUtils.getNodeListByXPath(elem, "./calendar/event");
			if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("Calendar"), profile); }
			nl = xmlUtils.getNodeListByXPath(elem, "./feeds/feed");
			if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("Feeds"), profile); }
			nl = xmlUtils.getNodeListByXPath(elem, "./wikis/wiki");
			if (nl.getLength() > 0) { 
				Widget wikiWidget = svcCommunity.addWidget(community, new Widget("Wiki"), profile);
				System.out.println("wiki instance id is " + wikiWidget.getInstanceID());
			}
			
			// subcommunity navigation
			try {
				Element subCommunityElem = (Element) xmlUtils.getNodeByXPath(elem, "./subcommunities");
				if (subCommunityElem.getAttribute("nav").equalsIgnoreCase("true")) { svcCommunity.addWidget(community, new Widget("SubcommunityNav"), profile); }
			} catch (Exception e) {}
					
			// add members to the community
			NodeList memberNodes = xmlUtils.getNodeListByXPath(elem, "./members/member");
			List<Member> members = getMembers(memberNodes, "communities");
			for (Member member : members) {
				svcCommunity.addMember(community, member, profile);
			}
					
			// *** ACTIVITIES ***
			// Note: You must wait 5-10 minutes for community ACL to propagate to Activity before non-owners can create nodes
			nl = xmlUtils.getNodeListByXPath(elem, "./activities/activity");
			List<Activity> activities = new ArrayList<Activity>();
			if (nl.getLength() > 0) { 
				for (int y=0; y < nl.getLength(); y++) {
					// create all required Activities so we only have to wait 10 minutes once (not 10 minutes for each Activity)
					Element elem2 = (Element) nl.item(y);
					Profile activityProfile = Config.PROFILES.get(elem2.getAttribute("user"));
					Activity activity = getActivity(elem2);
					activity = svcActivity.createCommunityActivity(activity, community, activityProfile);
					// store the created activity in a list; we'll come back to this later...
					activities.add(activity);
					System.out.println("  Activity Node creation will resume later.");
				}
			}
					
			// store the community and activities;  we will need these later
			communityActivityMap.put(community, activities);
					
			// *** BLOG ***
			// Note: You must wait 5-10 minutes for community ACL to propagate to Blog before non-owners can create posts
			nl = xmlUtils.getNodeListByXPath(elem, "./blogs/communityblog");
			if (nl.getLength() > 0) {
				// update the blog metadata
				Element elem2 = (Element) nl.item(0);
				Blog blog = getBlog(elem2, elem2.getNodeName());
				blog.setCommunityUuid(community.getuUid());
				blog.setHandle(community.getuUid());
				blog = svcBlog.updateBlog(blog, profile); // must be updated by Community creator or will FAIL
				communityBlogs.add(blog);
				System.out.println("  Community blog post creation will resume later.");
			}
					
			// *** IDEATION BLOGS ***
			// Note: You must wait 5-10 minutes for community ACL to propagate to Blog before non-owners can create ideas
			nl = xmlUtils.getNodeListByXPath(elem, "./blogs/ideationblog");
			if (nl.getLength() > 0) {
				// update the blog metadata
				Element elem2 = (Element) nl.item(0);
				Blog blog = getBlog(elem2, elem2.getNodeName());
				blog.setCommunityUuid(community.getuUid());
				List<Widget> widgets = svcCommunity.getWidgets(community, profile);
				for (Widget widget : widgets) {
					if (widget.getDefId().equalsIgnoreCase("ideationblog")) { blog.setHandle(widget.getInstanceID()); }
				}
				blog = svcBlog.updateBlog(blog, profile);  // must be updated by Community creator or will FAIL
				ideationBlogs.add(blog);
				System.out.println("  Ideation blog idea creation will resume later.");
			}
					
			System.out.println("Content creation for other Apps will resume later.");
			System.out.println("");
			
			/* dpc perhaps the community's description contains links to
			 * the community's apps. Go through the description looking for 
			 * references to a communityUuid and wiki instance ID and change them to 
			 * refer to this community's apps.
			 */
			// first look for a reference to communityUuid; this will address blog URLs too
			String communityContent = community.getContent();
			boolean shouldChange = false;
//			System.out.println("content is: " + communityContent);
			int communityUuidIndex = communityContent.indexOf("communityUuid");
			if ( communityUuidIndex > -1 ) {
				String uuidToReplace = communityContent.substring(communityUuidIndex + 14, communityUuidIndex + 14 + 36);
				communityContent = communityContent.replaceAll(uuidToReplace, community.getuUid());
//				community.setContent(communityContent);
//				svcCommunity.updateCommunity(community, profile);
				shouldChange = true;
			}
//
			int widgetIdIdx = communityContent.indexOf("/wiki/");
			if ( widgetIdIdx > -1 ) {
				String wikiIdToReplace = communityContent.substring(widgetIdIdx + 6, widgetIdIdx + 6 + 36);
				communityContent = communityContent.replaceAll(wikiIdToReplace, community.getWikiInstanceId());
//				page.setContent(wikiContent);
				shouldChange = true;
			}
			
			if ( shouldChange ) {
				community.setContent(communityContent);
				svcCommunity.updateCommunity(community, profile);
			}

//			// dpc replace communityUuid in links to this community's blog
//			int blogsIndex = communityContent.indexOf("/blogs/");
//			if ( blogsIndex > -1 ) {
//				String uuidToReplace = communityContent.substring(blogsIndex + 7, blogsIndex + 7 + 36);
//				communityContent = wikiContent.replaceAll(uuidToReplace, wiki.getCommunityUuid());
//				page.setContent(wikiContent);
//			}
			
		}
	}
	
	// =======================================
	// Communities - with workarounds for Community-to-App ACL propagation issues
	// - Create all Communities and Activities first, then come back later to fill in content...
	// =======================================
	public void importCommunitiesPOST() {
		// for each community
		for (Map.Entry<Community, List<Activity>> entry: communityActivityMap.entrySet()) {
			// get the previously created communities and activities
			Community community = entry.getKey();
			List<Activity> activities = entry.getValue();
			Profile profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
			
			System.out.println("");
			System.out.println("Resuming content creation for community [" + community.getTitle() + "]");
			
			String preExpression = "/icda/communities/community";
			if (stringUtils.isNotBlank(community.getParentUuid())) {
				preExpression = "/icda/communities/community/subcommunities/community";
			}
			Node node = xmlUtils.getNodeByXPath(importDoc, preExpression + "[@user=\"" + profile.getUid() + "\"][title=\"" + community.getTitle() + "\"]");
			Element elem = (Element) node;
			
			NodeList nl = null;
			
			// *** BOOKMARKS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./bookmarks/bookmark");
			if (nl.getLength() > 0) { 
				importBookmarks(nl, community); 
			}
			
			// *** EVENTS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./calendar/event");
			if (nl.getLength() > 0) { 
				for (int i=0; i < nl.getLength(); i++) {
					Element elem2 = (Element) nl.item(i);
					
					// create event
					Profile prof = Config.PROFILES.get(elem2.getAttribute("user"));
					Event event = getEvent(elem2);
					event = svcCommunity.createEvent(event, community, prof);
				} 
			}
			
			// *** FEEDS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./feeds/feed");
			if (nl.getLength() > 0) { 
				for (int i=0; i < nl.getLength(); i++) {
					Element elem2 = (Element) nl.item(i);
					
					// create feed
					Profile prof = Config.PROFILES.get(elem2.getAttribute("user"));
					FeedLink feed = getFeedLink(elem2);
					feed = svcCommunity.createFeedLink(feed, community, prof);
				} 
			}
			
			// *** FORUMS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./forums/forum");
			if (nl.getLength() > 0) { 
				// delete default forum(s)
				List<Forum> forums = svcForum.getCommunityForums(community, profile);
				for (Forum forum : forums) {
					svcForum.deleteForum(forum, profile);
				}
				
				importForums(nl, community);
			}
			
			// *** FILES ***
			nl = xmlUtils.getNodeListByXPath(elem, "./files/file");
			if (nl.getLength() > 0) { 
				importFiles(nl, community); 
			}
			
			// *** WIKIS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./wikis/wiki");
			if (nl.getLength() > 0) { 
				importWiki(nl.item(0), community);
			}
			
			// *** ACTIVITIES ***
			for (Activity activity : activities) {
				System.out.println("  Resuming node creation for activity [" + activity.getTitle() + "]");
				
				Profile prof = Config.PROFILES_UUID.get(activity.getAuthorUuid());
				Node node2 = xmlUtils.getNodeByXPath(elem, "./activities/activity[@user='" + prof.getUid() + "'][title='" + activity.getTitle() + "']");
				Element elem2 = (Element) node2;
				
				// add child nodes to the top level of the activity
				String[] nodeTypes = {"section", "entry", "todo"};
				for (int z = 0; z < nodeTypes.length; z++) {
					NodeList nl2 = xmlUtils.getNodeListByXPath(elem2, "./nodes/" + nodeTypes[z]);
					importActivityNodes(nl2, nodeTypes[z], activity, null);
				}
			}
			
			// *** BLOG ***
			nl = xmlUtils.getNodeListByXPath(elem, "./blogs/communityblog");
			if (nl.getLength() > 0) {
				for (Blog blog : communityBlogs) {
					if (blog.getCommunityUuid().equalsIgnoreCase(community.getuUid())) {
						System.out.println("Resuming blog post creation for communityblog [" + blog.getTitle() + "]");
						Element elem2 = (Element) nl.item(0);
						// add posts to the blog
						NodeList postNodes = xmlUtils.getNodeListByXPath(elem2, "./posts/post");
						importBlogPosts(postNodes, blog);
					}
				}
			}
			
			// *** IDEATION BLOG ***
			nl = xmlUtils.getNodeListByXPath(elem, "./blogs/ideationblog");
			if (nl.getLength() > 0) {
				for (Blog blog : ideationBlogs) {
					if (blog.getCommunityUuid().equalsIgnoreCase(community.getuUid())) {
						System.out.println("Resuming blog post creation for ideationblog [" + blog.getTitle() + "]");
						Element elem2 = (Element) nl.item(0);
						// add posts to the blog
						NodeList postNodes = xmlUtils.getNodeListByXPath(elem2, "./posts/post");
						importBlogPosts(postNodes, blog);
					}
				}
			}
			
			// *** SUBCOMMUNITIES ***
			nl = xmlUtils.getNodeListByXPath(elem, "./subcommunities/community");
			if (nl.getLength() > 0) {
				importCommunitiesPRE(nl, profile, community);
			}			
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	public void importCommunitiesPRE() {
		// for each user
		for (Map.Entry<String, Profile> entry: Config.PROFILES.entrySet()) {
			Profile profile = (Profile) entry.getValue();
			// if the user is marked for community import
			if (idFound("communities", profile)) {
				NodeList nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/communities/community[@user='" + profile.getUid() + "']");
				for (int x=0; x < nodes.getLength(); x++) {
					Element elem = (Element) nodes.item(x);
					
					// create community
					Community community = getCommunity(elem);
					community = svcCommunity.createCommunity(community, profile);
					if (community.getuUid() == "") { return; }
					
					// update logo
					svcCommunity.updateLogo(community, profile);
					
					// default widgets:  Bookmarks, Forums, Files
					// add required non-default widgets
					NodeList nl = xmlUtils.getNodeListByXPath(elem, "./activities/activity");
					if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("Activities"), profile); }
					nl = xmlUtils.getNodeListByXPath(elem, "./blogs/communityblog");
					if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("Blog"), profile); }
					nl = xmlUtils.getNodeListByXPath(elem, "./blogs/ideationblog");
					if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("IdeationBlog"), profile); }
					nl = xmlUtils.getNodeListByXPath(elem, "./calendar/event");
					if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("Calendar"), profile); }
					nl = xmlUtils.getNodeListByXPath(elem, "./feeds/feed");
					if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("Feeds"), profile); }
					nl = xmlUtils.getNodeListByXPath(elem, "./wikis/wiki");
					if (nl.getLength() > 0) { svcCommunity.addWidget(community, new Widget("Wiki"), profile); }
					Element subCommunityElem = (Element) xmlUtils.getNodeByXPath(elem, "./subcommunities");
					if (subCommunityElem.getAttribute("nav").equalsIgnoreCase("true")) { svcCommunity.addWidget(community, new Widget("SubcommunityNav"), profile); }
					
					// add members to the community
					NodeList memberNodes = xmlUtils.getNodeListByXPath(elem, "./members/member");
					List<Member> members = getMembers(memberNodes, "communities");
					for (Member member : members) {
						svcCommunity.addMember(community, member, profile);
					}
					
					// *** ACTIVITIES ***
					// Note: You must wait 5-10 minutes for community ACL to propagate to Activity before non-owners can create nodes
					nl = xmlUtils.getNodeListByXPath(elem, "./activities/activity");
					List<Activity> activities = new ArrayList<Activity>();
					if (nl.getLength() > 0) { 
						for (int y=0; y < nl.getLength(); y++) {
							// create all required Activities so we only have to wait 10 minutes once (not 10 minutes for each Activity)
							Element elem2 = (Element) nl.item(y);
							Profile activityProfile = Config.PROFILES.get(elem2.getAttribute("user"));
							Activity activity = getActivity(elem2);
							activity = svcActivity.createCommunityActivity(activity, community, activityProfile);
							// store the created activity in a list; we'll come back to this later...
							activities.add(activity);
							System.out.println("  Activity Node creation will resume later.");
						}
					}
					
					// store the community and activities;  we will need these later
					communityActivityMap.put(community, activities);
					
					// *** BLOG ***
					// Note: You must wait 5-10 minutes for community ACL to propagate to Blog before non-owners can create posts
					nl = xmlUtils.getNodeListByXPath(elem, "./blogs/communityblog");
					if (nl.getLength() > 0) {
						// update the blog metadata
						Element elem2 = (Element) nl.item(0);
						Blog blog = getBlog(elem2, elem2.getNodeName());
						blog.setCommunityUuid(community.getuUid());
						blog.setHandle(community.getuUid());
						blog = svcBlog.updateBlog(blog, profile); // must be updated by Community creator or will FAIL
						communityBlogs.add(blog);
						System.out.println("  Community blog post creation will resume later.");
					}
					
					// *** IDEATION BLOGS ***
					// Note: You must wait 5-10 minutes for community ACL to propagate to Blog before non-owners can create ideas
					nl = xmlUtils.getNodeListByXPath(elem, "./blogs/ideationblog");
					if (nl.getLength() > 0) {
						// update the blog metadata
						Element elem2 = (Element) nl.item(0);
						Blog blog = getBlog(elem2, elem2.getNodeName());
						blog.setCommunityUuid(community.getuUid());
						List<Widget> widgets = svcCommunity.getWidgets(community, profile);
						for (Widget widget : widgets) {
							if (widget.getDefId().equalsIgnoreCase("ideationblog")) { blog.setHandle(widget.getInstanceID()); }
						}
						blog = svcBlog.updateBlog(blog, profile);  // must be updated by Community creator or will FAIL
						ideationBlogs.add(blog);
						System.out.println("  Ideation blog idea creation will resume later.");
					}
					
					System.out.println("Content creation for other Apps will resume later.");
					System.out.println("");
				}
			}
		}
	}
	
	// =======================================
	// Communities - with workarounds for Community-to-App ACL propagation issues
	// - Create all Communities and Activities first, then come back later to fill in content...
	// =======================================
	public void importCommunitiesPOST() {
		// for each community
		for (Map.Entry<Community, List<Activity>> entry: communityActivityMap.entrySet()) {
			// get the previously created communities and activities
			Community community = entry.getKey();
			List<Activity> activities = entry.getValue();
			Profile profile = Config.PROFILES_UUID.get(community.getAuthorUuid());
			
			System.out.println("");
			System.out.println("Resuming content creation for community [" + community.getTitle() + "]");
			
			Node node = xmlUtils.getNodeByXPath(importDoc, "/icda/communities/community[@user=\"" + profile.getUid() + "\"][title=\"" + community.getTitle() + "\"]");
			//Node node = xmlUtils.getNodeByXPath(importDoc, "/icda/communities/community[title=\"" + community.getTitle() + "\"]");
			Element elem = (Element) node;
			
			NodeList nl = null;
			
			// *** BOOKMARKS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./bookmarks/bookmark");
			if (nl.getLength() > 0) { 
				importBookmarks(nl, community); 
			}
			
			// *** EVENTS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./calendar/event");
			if (nl.getLength() > 0) { 
				for (int i=0; i < nl.getLength(); i++) {
					Element elem2 = (Element) nl.item(i);
					
					// create event
					Profile prof = Config.PROFILES.get(elem2.getAttribute("user"));
					Event event = getEvent(elem2);
					event = svcCommunity.createEvent(event, community, prof);
				} 
			}
			
			// *** FEEDS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./feeds/feed");
			if (nl.getLength() > 0) { 
				for (int i=0; i < nl.getLength(); i++) {
					Element elem2 = (Element) nl.item(i);
					
					// create feed
					Profile prof = Config.PROFILES.get(elem2.getAttribute("user"));
					FeedLink feed = getFeedLink(elem2);
					feed = svcCommunity.createFeedLink(feed, community, prof);
				} 
			}
			
			// *** FORUMS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./forums/forum");
			if (nl.getLength() > 0) { 
				// delete default forum(s)
				List<Forum> forums = svcForum.getCommunityForums(community, profile);
				for (Forum forum : forums) {
					svcForum.deleteForum(forum, profile);
				}
				
				importForums(nl, community);
			}
			
			// *** FILES ***
			nl = xmlUtils.getNodeListByXPath(elem, "./files/file");
			if (nl.getLength() > 0) { 
				importFiles(nl, community); 
			}
			
			// *** WIKIS ***
			nl = xmlUtils.getNodeListByXPath(elem, "./wikis/wiki");
			if (nl.getLength() > 0) { 
				importWiki(nl.item(0), community);
			}
			
			// *** ACTIVITIES ***
			for (Activity activity : activities) {
				System.out.println("  Resuming node creation for activity [" + activity.getTitle() + "]");
				
				Profile prof = Config.PROFILES_UUID.get(activity.getAuthorUuid());
				Node node2 = xmlUtils.getNodeByXPath(elem, "./activities/activity[@user='" + prof.getUid() + "'][title='" + activity.getTitle() + "']");
				Element elem2 = (Element) node2;
				
				// add child nodes to the top level of the activity
				String[] nodeTypes = {"section", "entry", "todo"};
				for (int z = 0; z < nodeTypes.length; z++) {
					NodeList nl2 = xmlUtils.getNodeListByXPath(elem2, "./nodes/" + nodeTypes[z]);
					importActivityNodes(nl2, nodeTypes[z], activity, null);
				}
			}
			
			// *** BLOG ***
			nl = xmlUtils.getNodeListByXPath(elem, "./blogs/communityblog");
			if (nl.getLength() > 0) {
				for (Blog blog : communityBlogs) {
					if (blog.getCommunityUuid().equalsIgnoreCase(community.getuUid())) {
						System.out.println("Resuming blog post creation for communityblog [" + blog.getTitle() + "]");
						Element elem2 = (Element) nl.item(0);
						// add posts to the blog
						NodeList postNodes = xmlUtils.getNodeListByXPath(elem2, "./posts/post");
						importBlogPosts(postNodes, blog);
					}
				}
			}
			
			// *** IDEATION BLOG ***
			nl = xmlUtils.getNodeListByXPath(elem, "./blogs/ideationblog");
			if (nl.getLength() > 0) {
				for (Blog blog : ideationBlogs) {
					if (blog.getCommunityUuid().equalsIgnoreCase(community.getuUid())) {
						System.out.println("Resuming blog post creation for ideationblog [" + blog.getTitle() + "]");
						Element elem2 = (Element) nl.item(0);
						// add posts to the blog
						NodeList postNodes = xmlUtils.getNodeListByXPath(elem2, "./posts/post");
						importBlogPosts(postNodes, blog);
					}
				}
			}
			
			// *** SUBCOMMUNITIES ***
			nl = xmlUtils.getNodeListByXPath(elem, "./subcommunities/community");
			if (nl.getLength() > 0) {
				//importCommunityPRE(nl, parentCommunity);
			}			
		}
	}
	*/
	
	// import files and folders for the specified user
	public void importFiles(Profile profile) {
		printSection("Files", profile);
		NodeList nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/files/folder[@user='" + profile.getUid() + "']");
		importFolders(nodes);
		
		nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/files/file[@user='" + profile.getUid() + "']");
		importFiles(nodes, null);
	}
	
	private void importFolders(NodeList nodes) {
		for (int x=0; x < nodes.getLength(); x++) {
			Element elem = (Element) nodes.item(x);
			
			// create folder
			Profile profile = Config.PROFILES.get(elem.getAttribute("user"));
			Folder folder = getFolder(elem);
			folder = svcFile.createFolder(folder, profile);
			if (folder.getuUid() == "") { return; }
			
			// add files to the folder
			NodeList fileNodes = xmlUtils.getNodeListByXPath(elem, "./files/file");
			for (int y=0; y < fileNodes.getLength(); y++) {
				Element elem2 = (Element) fileNodes.item(y);
					
				// create the file
				Profile fileProfile = Config.PROFILES.get(elem.getAttribute("user"));
				File file = getFile(elem2);
				file = svcFile.createFile(folder, file, fileProfile);
				if (file.getuUid() == "") { return; }
					
				// add comments to the file
				NodeList commentNodes = xmlUtils.getNodeListByXPath(elem2, "./comments/comment");
				importFileComments(commentNodes, file);
			}
		}
	}
	
	private void importFiles(NodeList nodes, Community community) {
		for (int x=0; x < nodes.getLength(); x++) {
			Element elem = (Element) nodes.item(x);
			
			// create the file
			Profile profile = Config.PROFILES.get(elem.getAttribute("user"));
			File file = getFile(elem);
			if (community == null) {
				file = svcFile.createFile(file, profile);
			}
			else {
				String url = svcFile.getCommunityFilePublishURL(community, profile);
				file = svcFile.createCommunityFile(url, file, profile);
			}
			if (file.getuUid() == "") { return; }
			
			// add comments to the file
			NodeList commentNodes = xmlUtils.getNodeListByXPath(elem, "./comments/comment");
			importFileComments(commentNodes, file);
		}
	}
	
	private void importFileComments(NodeList commentNodes, File file) {
		for (int z=0; z < commentNodes.getLength(); z++) {
			Element elem3 = (Element) commentNodes.item(z);
				
			// create comment
			Profile commentProfile = Config.PROFILES.get(elem3.getAttribute("user"));
			Comment comment = new Comment();
			comment.setRefType("file");
			comment.setContent(xmlUtils.getStringByXPath(elem3, "./content"));
			comment = svcFile.createFileComment(file, comment, commentProfile);
		}
	}
	
	// import forums for the specified user
	public void importForums(Profile profile) {
		printSection("Forums", profile);
		NodeList nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/forums/forum[@user='" + profile.getUid() + "']");
		importForums(nodes, null);
	}

	private void importForums(NodeList nodes, Community community) {
		for (int x=0; x < nodes.getLength(); x++) {
			importForum(nodes.item(x), community);
		}
	}
	
	private void importForum(Node node, Community community) {
		Element elem = (Element) node;
		
		// create the forum
		Profile profile = Config.PROFILES.get(elem.getAttribute("user"));
		Forum forum = getForum(elem);
		if (community == null) {
			forum = svcForum.createForum(forum, profile);
		}
		else {
			forum = svcForum.createCommunityForum(forum, community, profile);
		}
		if (forum.getuUid() == "") { return; }
		
		NodeList topicNodes = xmlUtils.getNodeListByXPath(elem, "./topics/topic");
		importForumTopics(topicNodes, forum);
	}
	
	private void importForumTopics(NodeList topicNodes, Forum forum) {
		for (int y=0; y < topicNodes.getLength(); y++) {
			importForumTopic(topicNodes.item(y), forum);
		}
	}
	
	private void importForumTopic(Node topicNode, Forum forum) {
		Element elem2 = (Element) topicNode;
		
		// create forum topic
		Profile topicProfile = Config.PROFILES.get(elem2.getAttribute("user"));
		ForumTopic topic = getForumTopic(elem2);
		topic = svcForum.createForumTopic(topic, forum, topicProfile);
		if (topic.getuUid() == "") { return; }
		
		// create forum topic likes
		NodeList likeNodes = xmlUtils.getNodeListByXPath(elem2, "./likes/like");
		for (int z=0; z < likeNodes.getLength(); z++) {
			Element elem3 = (Element) likeNodes.item(z);
			
			Profile likeProfile = Config.PROFILES.get(elem3.getAttribute("user"));
			svcForum.createForumTopicLike(topic, likeProfile);
		}
		
		// create forum topic replies
		NodeList replyNodes = xmlUtils.getNodeListByXPath(elem2, "./replies/reply");
		importForumTopicReplies(replyNodes, topic.getuUid());
	}
	
	private void importForumTopicReplies(NodeList nodes, String parentUuid) {
		for (int x=0; x < nodes.getLength(); x++) {
			importForumTopicReply(nodes.item(x), parentUuid);
		}
	}

	private void importForumTopicReply(Node replyNode, String parentUuid) {
		Element elem = (Element) replyNode;
		
		// create the forum topic reply
		Profile profile = Config.PROFILES.get(elem.getAttribute("user"));
		ForumTopicReply reply = getForumTopicReply(elem, parentUuid);
		reply = svcForum.createForumTopicReply(reply, profile);
		
		// delete, if necessary
		if (reply.isDeleted()) {
			svcForum.deleteForumTopicReply(reply, profile);
		}
		
		// add likes to the reply
		NodeList likeNodes = xmlUtils.getNodeListByXPath(elem, "./likes/like");
		for (int y=0; y < likeNodes.getLength(); y++) {
			Element elem2 = (Element) likeNodes.item(y);
			
			Profile likeProfile = Config.PROFILES.get(elem2.getAttribute("user"));
			svcForum.createForumTopicReplyLike(reply, likeProfile);
		}
		
		// search for and create any child replies
		NodeList replyNodes = xmlUtils.getNodeListByXPath(elem, "./reply");
		importForumTopicReplies(replyNodes, reply.getuUid()); 
	}
	
	// import profiles data for the specified user
	public void importProfiles(Profile profile) {
		printSection("Profiles", profile);
		// colleagues
		NodeList nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/profiles/colleagues/colleague[@from='" + profile.getUid() + "']");
		for (int x=0; x < nodes.getLength(); x++) {
			Element elem = (Element) nodes.item(x);
			
			Profile target = Config.PROFILES.get(elem.getAttribute("to"));
			
			// create colleague connection
			Colleague colleague = new Colleague();
			colleague.setContent(elem.getAttribute("content"));
			colleague.setStatus("pending");
			colleague = svcProfile.createColleague(colleague, profile, target);
			
			// if necessary, accept the pending invitation
			colleague.setStatus(elem.getAttribute("status"));
			if (colleague.getStatus().equalsIgnoreCase("accepted")) {
				svcProfile.acceptColleague(colleague, target);
			}
		}
		
		// tags
		String tagsStr = xmlUtils.getStringByXPath(importDoc, "/icda/profiles/profile[@user='" + profile.getUid() + "']/tags");
		if (!tagsStr.trim().equalsIgnoreCase("")) {
			List<String> tags = stringUtils.explode(tagsStr, ",");
			svcProfile.addTags(tags, profile);
		}
	}
	
	// import wikis for the specified user
	public void importWikis(Profile profile) {
		printSection("Wikis", profile);
		NodeList nodes = xmlUtils.getNodeListByXPath(importDoc, "/icda/wikis/wiki[@user='" + profile.getUid() + "']");
		importWikis(nodes, null);
	}
	
	private void importWikis(NodeList nodes, Community community) {
		for (int x=0; x < nodes.getLength(); x++) {
			importWiki(nodes.item(x), community);
		}
	}
	
	private void importWiki(Node wikiNode, Community community) {
		Element elem = (Element) wikiNode;
		
		// create wiki
		Profile profile = Config.PROFILES.get(elem.getAttribute("user"));
		Wiki wiki = getWiki(elem);
		if (community == null) {
			// add members to the wiki
			NodeList memberNodes = xmlUtils.getNodeListByXPath(elem, "./members/member");
			List<Member> members = getMembers(memberNodes, "wikis");
			for (Iterator<Member> it = members.iterator(); it.hasNext();) {
			    Member member = it.next();
			    if (member.getuUid().equalsIgnoreCase(profile.getuUid())) {
			    	it.remove();
			    }
			}
			wiki.setMembers(members);
			wiki = svcWiki.createWiki(wiki, profile);
		}
		else {
			wiki.setCommunityUuid(community.getuUid()); // dpc
			String widgetInstanceId = "";
			List<Widget> widgets = svcCommunity.getWidgets(community, profile);
			for (Widget widget : widgets) {
				if (widget.getDefId().equalsIgnoreCase("wiki")) {
					widgetInstanceId = widget.getInstanceID();
				}
				if (widget.getDefId().equalsIgnoreCase("forum")) {
					wiki.setCommunityForumWidgetId(widget.getInstanceID());
				}
			}
			
			community.setWikiInstanceId(widgetInstanceId); // dpc
			wiki.setLabel(widgetInstanceId);
			wiki.setuUid(widgetInstanceId);
			wiki = svcWiki.updateWiki(wiki, profile);
		}
		if (wiki.getuUid() == "") { return; }
		
		// delete the welcome page
		List<WikiPage> pages = svcWiki.getWikiPages(wiki, profile);
		for (WikiPage page : pages) {
			if (page.getTitle().startsWith("Welcome to")) {
				svcWiki.deleteWikiPage(page, profile);
			}
		}
		
		// create pages for this wiki
		NodeList pageNodes = xmlUtils.getNodeListByXPath(elem, "./pages/page");
		importWikiPages(pageNodes, wiki);
	}
	
	private void importWikiPages(NodeList pageNodes, Wiki wiki) {
		for (int y=0; y < pageNodes.getLength(); y++) {
			importWikiPage(pageNodes.item(y), wiki);
		}
	}
	
	private void importWikiPage(Node pageNode, Wiki wiki) {
		Element elem2 = (Element) pageNode;
		
		// create page
		Profile pageProfile = Config.PROFILES.get(elem2.getAttribute("user"));
		WikiPage page = getWikiPage(elem2);
		page.setWikiUuid(wiki.getuUid());
		
		// dpc added section to change communityUuid to the one for this community
		String wikiContent = page.getContent();
		int communityUuidIndex = wikiContent.indexOf("communityUuid");
		if ( communityUuidIndex > -1 ) {
			String uuidToReplace = wikiContent.substring(communityUuidIndex + 14, communityUuidIndex + 14 + 36);
			wikiContent = wikiContent.replaceAll(uuidToReplace, wiki.getCommunityUuid());
			page.setContent(wikiContent);
		}

		// dpc replace communityUuid in links to this community's blog
		int blogsIndex = wikiContent.indexOf("/blogs/");
		if ( blogsIndex > -1 ) {
			String uuidToReplace = wikiContent.substring(blogsIndex + 7, blogsIndex + 7 + 36);
			wikiContent = wikiContent.replaceAll(uuidToReplace, wiki.getCommunityUuid());
			page.setContent(wikiContent);
		}

		// dpc replace widget ID in links to this community's Forum
		int widgetIdIdx = wikiContent.indexOf("fullpageWidgetId");
		if ( widgetIdIdx > -1 ) {
			String widgetIdToReplace = wikiContent.substring(widgetIdIdx + 17, widgetIdIdx + 17 + 36);
			wikiContent = wikiContent.replaceAll(widgetIdToReplace, wiki.getCommunityForumWidgetId());
			page.setContent(wikiContent);
		}
		
		page = svcWiki.createWikiPage(page, pageProfile);
		if (page.getuUid() == "") { return; }		
		
		// create any comments for this page
		NodeList commentNodes = xmlUtils.getNodeListByXPath(elem2, "./comments/comment");
		for (int z=0; z < commentNodes.getLength(); z++) {
			Element elem3 = (Element) commentNodes.item(z);
			
			// create comment
			Profile commentProfile = Config.PROFILES.get(elem3.getAttribute("user"));
			Comment comment = new Comment();
			comment.setRefId(page.getuUid());
			comment.setRefType("wikipage");
			comment.setContent(xmlUtils.getStringByXPath(elem3, "./content"));
			comment = svcWiki.createWikiPageComment(page, comment, commentProfile);
		}		
	}
	
	// import all content for the specified users and applications
	public void importContent() {
		
		// communities workaround
		// The Community ACL needs time to propagate to added Apps: 5-10 minutes for Activities & Blogs, 1-2 minutes for Files & Wikis
		printCommunitySection("Importing Communities for all users.");
		importCommunitiesPRE();
		if (communityActivityMap.size() > 0) {
			printCommunitySection("Communities import paused to allow membership to propagate to Apps.");
		}
		// By creating all Communities and Apps first, we reduce the wait time to ~10 minutes for all content (instead of 10 mins per piece of content)
		// Next, go create all stand-alone content while waiting for the server to update ACLs
		
		// for each user
		for (Map.Entry<String, Profile> entry: Config.PROFILES.entrySet()) {
			Profile profile = (Profile) entry.getValue();
			
			// if the user is listed in the import section of the XML file, add the user's content to the import list
			if (idFound("activities", profile)) { importActivities(profile); }
			//if (idFound("communities", profile)) { importCommunities(profile); }
			if (idFound("files", profile)) { importFiles(profile); }
			if (idFound("profiles", profile)) { importProfiles(profile); }
			
			if (Config.ENVIRONMENT.equalsIgnoreCase("on-premise")) {
				if (idFound("blogs", profile)) { importBlogs(profile); }
				if (idFound("bookmarks", profile)) { importBookmarks(profile); }
				if (idFound("forums", profile)) { importForums(profile); }
				if (idFound("wikis", profile)) { importWikis(profile); }
			}
		
		}
		
		// communities workaround
		// Now that all Communities and Stand-alone content has been created, go back and fill in Communities App data
		if (communityActivityMap.size() > 0) {
			printCommunitySection("Communities import resuming for all users.  Creating content for Apps.");
			importCommunitiesPOST();
		}
	}
	
	private void printCommunitySection(String msg) {
		System.out.println("");
		System.out.println("[" + msg + "]");
	}
	
	private void printSection(String app, Profile profile) {
		System.out.println("");
		System.out.println("[Importing " + app + " for " + profile.getDisplayName() + "]");
	}
	
	// is the uid for the given profile listed delete section of the user XML file
	private boolean idFound(String app, Profile profile) {
		String userids = xmlUtils.getStringByXPath(userDoc, "/icda/import/" + app);
		boolean result = stringUtils.stringContainsUid(userids, profile.getUid());
		return result;
	}
}
