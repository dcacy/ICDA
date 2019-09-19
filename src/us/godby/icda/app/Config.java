package us.godby.icda.app;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import us.godby.icda.ic.Profile;

public class Config {

	// application information
	public static String APP_NAME = "IBM Connections Demo Assistant";
	public static String APP_AUTHOR = "Paul Godby (paul_godby@us.ibm.com) and Darren Cacy (dcacy@us.ibm.com)";
	public static String APP_VERSION = "2.0.2";
	public static String APP_VERSION_DATE = "20170619";
	
	// authentication
	public static String AUTH_TYPE = "basic";
	public static String AUTH_LOGIN_ATTR = "uid";
	
	// server environment
	public static String ENVIRONMENT = "on-premise";	// on-premise OR ibm-smart-cloud
	
	// directories
	public static String DIR_DATA = System.getProperty("user.dir") + File.separator + "data";
	public static String DIR_FILES = System.getProperty("user.dir") + File.separator + "files";
	
	// users (2 copies:  lookup via XML uid OR lookup by Connections uUid)
	public static Map<String,Profile> PROFILES = new TreeMap<String,Profile>();
	public static Map<String,Profile> PROFILES_UUID = new TreeMap<String,Profile>();
		
	// timers
	public static int SLEEP_HTTP = 0;
	public static int SLEEP_COMMUNITY_ACTIVITIES = 0;
	public static int SLEEP_COMMUNITY_BLOG = 0;
	public static int SLEEP_COMMUNITY_FILES = 0;
	public static int SLEEP_COMMUNITY_IDEATIONBLOGS = 0;
	public static int SLEEP_COMMUNITY_WIKIS = 0;
	
	// retry attempts for timers
	public static int RETRY_COMMUNITY_ACTIVITIES = 0;
	public static int RETRY_COMMUNITY_BLOG = 0;
	public static int RETRY_COMMUNITY_FILES = 0;
	public static int RETRY_COMMUNITY_IDEATIONBLOGS = 0;
	public static int RETRY_COMMUNITY_WIKIS = 0;
	
	// urls
	public static Map<String,String> URLS = new TreeMap<String, String>();
	static {
		// activities
		URLS.put("activities_addMember", "/service/atom2/acl?activityUuid=${uUid}");
		URLS.put("activities_createActivity", "/service/atom2/activities");
		URLS.put("activities_createCommunityActivity", "/service/atom2/activities?commUuid=${commUuid}");
		URLS.put("activities_createNode", "/service/atom2/activity?activityUuid=${uUid}");
		URLS.put("activities_deleteActivity", "/service/atom2/activitynode?activityNodeUuid=${uUid}");
		URLS.put("activities_getMembers", "/service/atom2/acl?activityUuid=${uUid}");
		URLS.put("activities_getNodes", "/service/atom2/activity?activityUuid=${uUid}&sortBy=createdby&sortOrder=asc");
		URLS.put("activities_getCommunityActivities", "/service/atom2/activities?commUuid=${commUuid}");
		URLS.put("activities_getMyActivities", "/service/atom2/activities?sortBy=createdby&sortOrder=asc");
		URLS.put("activities_getMyCompletedActivities", "/service/atom2/completed?sortBy=createdby&sortOrder=asc");		
		
		// blogs
		URLS.put("blogs_createBlog", "/homepage/api/blogs");
		URLS.put("blogs_createBlogPost", "/${handle}/api/entries");
		URLS.put("blogs_createBlogPostCommentLikes", "/${handle}/api/recommend/comments/${uUid}");
		URLS.put("blogs_createBlogPostComments", "/${handle}/api/comments");
		URLS.put("blogs_createBlogPostLikes", "/${handle}/api/recommend/entries/${uUid}");
		URLS.put("blogs_createIdeationBlog", "/homepage/api/blogs?commUuid=${commUuid}&blogType=ideationblog");
		URLS.put("blogs_deleteBlog", "/homepage/api/blogs/${uUid}");
		URLS.put("blogs_getBlog", "/${handle}/api/entries");
		URLS.put("blogs_getBlogPosts", "/${handle}/api/entries");
		URLS.put("blogs_getBlogPostCommentLikes", "/${handle}/api/recommend/comments/${uUid}");
		URLS.put("blogs_getBlogPostComments", "/${handle}/api/entrycomments/${uUid}");
		URLS.put("blogs_getBlogPostLikes", "/${handle}/api/recommend/entries/${uUid}");
		URLS.put("blogs_getIdeationBlogs", "/homepage/feed/ideationblogs/atom?commUuid=${commUuid}&blogType=ideationblog");
		URLS.put("blogs_getMyBlogs", "/homepage/api/blogs");
		URLS.put("blogs_getMyVotes", "/homepage/feed/myvotes/atom?lang=en_us");
		URLS.put("blogs_updateBlog", "/homepage/api/blogs/${uUid}");
		
		// bookmarks
		URLS.put("dogear_createBookmark", "/api/app");
		URLS.put("dogear_deleteBookmark", "/api/app?url=${url}");
		URLS.put("dogear_getMyBookmarks", "/api/app?userid=${uUid}");
		
		// common
		URLS.put("serviceconfigs", "/activities/serviceconfigs");
		
		// communities
		URLS.put("communities_addMember", "/service/atom/community/members?communityUuid=${uUid}");
		URLS.put("communities_addWidget", "/service/atom/community/widgets?communityUuid=${uUid}");
		URLS.put("communities_createBookmark", "/service/atom/community/bookmarks?communityUuid=${uUid}");
		URLS.put("communities_createCommunity", "/service/atom/communities/my");
		URLS.put("communities_createEvent", "/calendar/atom/calendar/event?calendarUuid=${uUid}");
		URLS.put("communities_createFeedLink", "/service/atom/community/feeds?communityUuid=${uUid}");
		URLS.put("communities_createSubCommunity", "/service/atom/community/subcommunities?communityUuid=${uUid}");
		URLS.put("communities_deleteCommunity", "/service/atom/community/instance?communityUuid=${uUid}");
		URLS.put("communities_getBookmarks", "/service/atom/community/bookmarks?communityUuid=${uUid}");
		URLS.put("communities_getCommunity", "/service/atom/community/instance?communityUuid=${uUid}");
		URLS.put("communities_updateCommunity", "/service/atom/community/instance?communityUuid=${uUid}"); // dpc
		URLS.put("communities_getEvents", "/calendar/atom/calendar/event?calendarUuid=${uUid}&type=event");
		URLS.put("communities_getFeedLinks", "/service/atom/community/feeds?communityUuid=${uUid}");
		URLS.put("communities_getLogo", "/service/html/image?communityUuid=${uUid}");
		URLS.put("communities_getMembers", "/service/atom/community/members?communityUuid=${uUid}");
		URLS.put("communities_getMyCommunities", "/service/atom/communities/my");
		URLS.put("communities_getRemoteApplications", "/service/atom/community/remoteApplications?communityUuid=${uUid}");
		URLS.put("communities_getSubCommunities", "/service/atom/community/subcommunities?communityUuid=${uUid}");
		URLS.put("communities_getWidgets", "/service/atom/community/widgets?communityUuid=${uUid}");
		URLS.put("communities_updateLogo", "/service/html/image?communityUuid=${uUid}");
		
		// files
		URLS.put("files_addFileToFolder", "/basic/api/collection/${folderUuid}/feed?itemId=${fileUuid}");
		URLS.put("files_getCommunityIntrospection", "/basic/api/community/${commUuid}/introspection");
		URLS.put("files_createFile", "/basic/api/myuserlibrary/feed");
		URLS.put("files_createFileComment", "/basic/api/library/${libraryUuid}/document/${documentUuid}/feed");
		URLS.put("files_createFolder", "/basic/api/collections/feed");
		URLS.put("files_deleteFile", "/basic/api/myuserlibrary/document/${uUid}/entry");
		URLS.put("files_deleteFolder", "/basic/api/collection/${uUid}/entry");
		URLS.put("files_downloadFile", "/basic/api/library/${libraryUuid}/document/${documentUuid}/media");
		URLS.put("files_getCommunityFilesAndFolders", "/basic/api/communitycollection/${commUuid}/feed");
		URLS.put("files_getFile", "/basic/api/library/${libraryUuid}/document/${documentUuid}/entry?includeTags=true");
		URLS.put("files_getFileComments", "/basic/api/library/${libraryUuid}/document/${documentUuid}/feed");
		URLS.put("files_getFolderFiles", "/basic/api/collection/${uUid}/feed");
		URLS.put("files_getMyFiles", "/basic/api/myuserlibrary/feed");
		URLS.put("files_getMyFolders", "/basic/api/collections/feed?creator=${uUid}");
		
		// forums
		URLS.put("forums_createCommunityForum", "/atom/forums?communityUuid=${commUuid}");
		URLS.put("forums_createForum", "/atom/forums");
		URLS.put("forums_createForumTopic", "/atom/topics?forumUuid=${uUid}");
		URLS.put("forums_createForumTopicReply", "/atom/replies?topicUuid=${uUid}");
		URLS.put("forums_createPostLike", "/atom/recommendation/entries?postUuid=${uUid}");
		URLS.put("forums_deleteForum", "/atom/forum?forumUuid=${uUid}");
		URLS.put("forums_deleteForumTopicReply", "/atom/reply?replyUuid=${uUid}");
		URLS.put("forums_getCommunityForums", "/atom/forums?communityUuid=${commUuid}");
		URLS.put("forums_getMyForums", "/atom/forums/my?view=owner&sortBy=created&sortOrder=asc");
		URLS.put("forums_getPostLikes", "/atom/recommendation/entries?postUuid=${uUid}");
		URLS.put("forums_getTopics", "/atom/topics?forumUuid=${uUid}&sortBy=created&sortOrder=asc");
		URLS.put("forums_getTopicReplies", "/atom/replies?topicUuid=${uUid}&sortBy=created&sortOrder=asc");
		
		// profiles
		URLS.put("profiles_acceptColleague", "/atom/connection.do?connectionId=${uUid}");
		URLS.put("profiles_createColleague", "/atom/connections.do?key=${key}&connectionType=colleague");
		URLS.put("profiles_createTags", "/atom/profileTags.do?targetKey=${targetKey}&sourceKey=${sourceKey}");
		URLS.put("profiles_deleteColleague", "/atom/connection.do?connectionId=${uUid}");
		URLS.put("profiles_getColleagues", "/atom/connections.do?connectionType=colleague&inclMessage=true&status=${status}&userid=${uUid}");
		URLS.put("profiles_getProfile", "/atom/profile.do?userid=${uUid}");
		URLS.put("profiles_getTags", "/atom/profileTags.do?targetKey=${key}");
		
		// wikis
		URLS.put("wikis_addMembers", "/basic/api/wiki/${uUid}/members");
		URLS.put("wikis_addWikiPageTags", "/basic/api/wiki/${wikiUuid}/page/${pageUuid}/entry");
		URLS.put("wikis_createWiki", "/basic/api/wikis/feed");
		URLS.put("wikis_createWikiPage", "/basic/api/wiki/${uUid}/feed");
		URLS.put("wikis_createWikiPageComment", "/basic/api/wiki/${wikiUuid}/page/${pageUuid}/feed");
		URLS.put("wikis_deleteWiki", "/basic/api/wiki/${uUid}/entry");
		URLS.put("wikis_deleteWikiPage", "/basic/api/wiki/${wikiUuid}/page/${pageUuid}/entry");
		URLS.put("wikis_getCommunityWikis", "/basic/api/community/${commUuid}/wikis/feed");
		URLS.put("wikis_getMembers", "/basic/api/wiki/${uUid}/members");
		URLS.put("wikis_getMyWikis", "/basic/api/mywikis/feed?role=manager&sortBy=created&sortOrder=asc");
		URLS.put("wikis_getWiki", "/basic/api/wiki/${uUid}/entry");
		URLS.put("wikis_getWikiPageComments", "/basic/api/wiki/${wikiUuid}/page/${pageUuid}/feed?sortOrder=asc");
		URLS.put("wikis_getWikiPageMedia", "/basic/api/wiki/${wikiUuid}/page/${pageUuid}/media");
		URLS.put("wikis_getWikiPages", "/basic/anonymous/api/wiki/${uUid}/feed?includeTags=true&sortBy=created&sortOrder=asc");
		URLS.put("wikis_updateWiki", "/basic/api/wiki/${uUid}/entry");
	}
	
	// xml namespaces and extensions
	public static QName QNAME_CA_MEMBER = new QName("http://www.ibm.com/xmlns/prod/composite-applications/v1.0", "member", "ca");
	public static QName QNAME_CA_ID = new QName("http://www.ibm.com/xmlns/prod/composite-applications/v1.0", "id", "ca");
	public static QName QNAME_CA_ROLE = new QName("http://www.ibm.com/xmlns/prod/composite-applications/v1.0", "role", "ca");
	public static QName QNAME_CA_TYPE = new QName("http://www.ibm.com/xmlns/prod/composite-applications/v1.0", "type", "ca");
	
	public static QName QNAME_SNX_ALLDAY = new QName("http://www.ibm.com/xmlns/prod/sn", "allday", "snx");
	public static QName QNAME_SNX_ACTIVITY = new QName("http://www.ibm.com/xmlns/prod/sn", "activity", "snx");
	public static QName QNAME_SNX_ASSIGNEDTO = new QName("http://www.ibm.com/xmlns/prod/sn", "assignedto", "snx");
	public static QName QNAME_SNX_BYDAY = new QName("http://www.ibm.com/xmlns/prod/sn", "byDay", "snx");
	public static QName QNAME_SNX_BYDATE = new QName("http://www.ibm.com/xmlns/prod/sn", "byDate", "snx");
	public static QName QNAME_SNX_COMMUNITYSTARTPAGE = new QName("http://www.ibm.com/xmlns/prod/sn", "communityStartPage", "snx");
	public static QName QNAME_SNX_COMMUNITYTYPE = new QName("http://www.ibm.com/xmlns/prod/sn", "communityType", "snx");
	public static QName QNAME_SNX_COMMUNITYUUID = new QName("http://www.ibm.com/xmlns/prod/sn", "communityUuid", "snx");
	public static QName QNAME_SNX_CONNECTION = new QName("http://www.ibm.com/xmlns/prod/sn", "connection", "snx");
	public static QName QNAME_SNX_ENDDATE = new QName("http://www.ibm.com/xmlns/prod/sn", "endDate", "snx");
	public static QName QNAME_SNX_ENVIRONMENT = new QName("http://www.ibm.com/xmlns/prod/sn", "environment", "snx");
	public static QName QNAME_SNX_FREQUENCY = new QName("http://www.ibm.com/xmlns/prod/sn", "frequency", "snx");
	public static QName QNAME_SNX_HANDLE = new QName("http://www.ibm.com/xmlns/prod/sn", "handle", "snx");
	public static QName QNAME_SNX_INTENSITYBIN = new QName("http://www.ibm.com/xmlns/prod/sn", "intensityBin", "snx");
	public static QName QNAME_SNX_ISEXTERNAL = new QName("http://www.ibm.com/xmlns/prod/sn", "isExternal", "snx");
	public static QName QNAME_SNX_LINK = new QName("http://www.ibm.com/xmlns/prod/sn", "link", "snx");
	public static QName QNAME_SNX_LOCATION = new QName("http://www.ibm.com/xmlns/prod/sn", "location", "snx");
	public static QName QNAME_SNX_PERIOD = new QName("http://www.ibm.com/xmlns/prod/sn", "period", "snx");
	public static QName QNAME_SNX_RECURRENCE = new QName("http://www.ibm.com/xmlns/prod/sn", "recurrence", "snx");
	public static QName QNAME_SNX_REL = new QName("http://www.ibm.com/xmlns/prod/sn", "rel", "snx");
	public static QName QNAME_SNX_ROLE = new QName("http://www.ibm.com/xmlns/prod/sn", "role", "snx");
	public static QName QNAME_SNX_STARTDATE = new QName("http://www.ibm.com/xmlns/prod/sn", "startDate", "snx");
	public static QName QNAME_SNX_TYPE = new QName("http://www.ibm.com/xmlns/prod/sn", "type", "snx");
	public static QName QNAME_SNX_UNTIL = new QName("http://www.ibm.com/xmlns/prod/sn", "until", "snx");
	public static QName QNAME_SNX_USER = new QName("http://www.ibm.com/xmlns/prod/sn", "userid", "snx");
	public static QName QNAME_SNX_XMLNS = new QName("http://www.ibm.com/xmlns/prod/sn", "xmlns", "snx");
	public static QName QNAME_SNX_VISIBILITYBIN = new QName("http://www.ibm.com/xmlns/prod/sn", "visibilityBin", "snx");
	public static QName QNAME_SNX_WIDGETDEFID = new QName("http://www.ibm.com/xmlns/prod/sn", "widgetDefId", "snx");
	public static QName QNAME_SNX_WIDGETINSTANCEID = new QName("http://www.ibm.com/xmlns/prod/sn", "widgetInstanceId", "snx");
	public static QName QNAME_SNX_WIDGETPROPERTY = new QName("http://www.ibm.com/xmlns/prod/sn", "widgetProperty", "snx");
	
	public static QName QNAME_TD_ITEM = new QName("urn:ibm.com/td", "itemId", "td");
	public static QName QNAME_TD_LABEL = new QName("urn:ibm.com/td", "label", "td");
	public static QName QNAME_TD_LIBRARY = new QName("urn:ibm.com/td", "libraryId", "td");
	public static QName QNAME_TD_LIBRARYTYPE = new QName("urn:ibm.com/td", "libraryType", "td");
	public static QName QNAME_TD_SHARE = new QName("urn:ibm.com/td", "sharePermission", "td");
	public static QName QNAME_TD_SHAREWITH = new QName("urn:ibm.com/td", "sharedWith", "td");
	public static QName QNAME_TD_UUID = new QName("urn:ibm.com/td", "uuid", "td");
	public static QName QNAME_TD_VISIBILITY = new QName("urn:ibm.com/td", "visibility", "td");
	public static QName QNAME_TD_VISIBILITYCOMPUTED = new QName("urn:ibm.com/td", "visibilityComputed", "td");
	
	public static QName QNAME_THR_REPLYTO = new QName("http://purl.org/syndication/thread/1.0", "in-reply-to", "thr");
	/*
	public static Map<String, String> NAMESPACES = new TreeMap<String, String>();
	static {
		NAMESPACES.put("atom", "http://www.w3.org/2005/Atom");
		NAMESPACES.put("app", "http://www.w3.org/2007/app");
	}
	*/
}
