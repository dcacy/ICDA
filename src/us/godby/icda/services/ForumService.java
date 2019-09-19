package us.godby.icda.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.protocol.client.ClientResponse;

import us.godby.icda.app.Config;
import us.godby.icda.ic.Community;
import us.godby.icda.ic.Forum;
import us.godby.icda.ic.ForumTopic;
import us.godby.icda.ic.ForumTopicReply;
import us.godby.icda.ic.Profile;
import us.godby.icda.ic.Recommendation;
import us.godby.utilities.RestBroker;

public class ForumService {
	
	// utilities
	private RestBroker restBroker = new RestBroker();
	
	// delete the specified forums
	public void deleteForums(List<Forum> forums, Profile profile) {
		for (Forum forum : forums) {
			deleteForum(forum, profile);
		}
	}
	
	// delete a specific forum for the user
	public void deleteForum(Forum forum, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_deleteForum");
		url = url.replace("${uUid}", forum.getuUid());
		
		ClientResponse response = restBroker.doDelete(url, profile);
		if (response.getStatus() == 204) {
			System.out.println("Deleted forum [" + forum.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
	}
	
	// delete a specific forum topic reply
	public void deleteForumTopicReply(ForumTopicReply reply, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_deleteForumTopicReply");
		url = url.replace("${uUid}", reply.getuUid());
		
		ClientResponse response = restBroker.doDelete(url, profile);
		if (response.getStatus() == 204) {
			System.out.println("    Deleted topic reply [" + reply.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
	}
	
	// get forums for specified community
	public List<Forum> getCommunityForums(Community community, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_getCommunityForums");
		url = url.replace("${commUuid}", community.getuUid());
		return getForums(url, profile, "community");
	}
	
	// get all forums for the user
	public List<Forum> getMyForums(Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_getMyForums");
		return getForums(url, profile, "standalone");
	}
	
	// get a list of forums
	private List<Forum> getForums(String url, Profile profile, String forumType) {
		List<Forum> list = new ArrayList<Forum>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// only return the requested forums
				boolean isValid = false;
				// if requesting community forums, no restrictions
				if (forumType.equalsIgnoreCase("community")) {
					isValid = true;
				}
				// if requesting stand-alone forums, you must filter out community entries from the list...
				if ((forumType.equalsIgnoreCase("standalone")) && (entry.getExtension(Config.QNAME_SNX_COMMUNITYUUID) == null)) {
					isValid = true;
				}
				
				if (isValid) {	
					// get refined data from entry
					String uUid = entry.getId().toString();
					uUid = uUid.substring(uUid.lastIndexOf(":") + 1);
					
					// create forum
					Forum forum = new Forum();
					try { forum.setCommunityUuid(entry.getExtension(Config.QNAME_SNX_COMMUNITYUUID).getText()); } catch (Exception e) {}
					forum.setContent(entry.getContent());
					forum.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
					forum.setTitle(entry.getTitle());
					forum.setuUid(uUid);
					// tags
					List<Category> categories = entry.getCategories();
					for (Category category : categories) {
						if (category.getScheme() == null) {
							forum.addTag(category.getTerm());
						}
					}
				
					list.add(forum);
				}
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getForums(link.getHref().toString(), profile, forumType));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of the topics for the forum
	public List<ForumTopic> getTopics(Forum forum, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_getTopics");
		url = url.replace("${uUid}", forum.getuUid());
		return getTopics(url, profile);
	}
	
	private List<ForumTopic> getTopics(String url, Profile profile) {
		List<ForumTopic> list = new ArrayList<ForumTopic>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf(":") + 1);
				
				// create topic
				ForumTopic topic = new ForumTopic();
				topic.setContent(entry.getContent());
				topic.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				topic.setTitle(entry.getTitle());
				topic.setuUid(uUid);
				// flags and tags
				List<Category> categories = entry.getCategories();
				for (Category category : categories) {
					String term = category.getTerm();
					if (term.equalsIgnoreCase("answered")) { topic.setAnswered(true); }
					if (term.equalsIgnoreCase("locked")) { topic.setLocked(true); }
					if (term.equalsIgnoreCase("pinned")) { topic.setPinned(true); }
					if (term.equalsIgnoreCase("question")) { topic.setQuestion(true); }
				}
				
				list.add(topic);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getTopics(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of replies for the topic
	public List<ForumTopicReply> getForumTopicReplies(ForumTopic topic, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_getTopicReplies"); 
		url = url.replace("${uUid}", topic.getuUid());
		return getForumTopicReplies(url, profile);
	}
	
	private List<ForumTopicReply> getForumTopicReplies(String url, Profile profile) {
		List<ForumTopicReply> list = new ArrayList<ForumTopicReply>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf(":") + 1);
				
				String replyToUuid = entry.getExtension(Config.QNAME_THR_REPLYTO).getAttributeValue("ref");
				replyToUuid = replyToUuid.substring(replyToUuid.lastIndexOf(":") + 1);
				
				// create reply
				ForumTopicReply reply = new ForumTopicReply();
				// deleted replies do not have a content element
				try { reply.setContent(entry.getContent().trim()); } catch (Exception e) {}
				reply.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				reply.setTitle(entry.getTitle());
				reply.setTopicUuid(replyToUuid);
				reply.setuUid(uUid);
				// flags and tags
				List<Category> categories = entry.getCategories();
				for (Category category : categories) {
					String term = category.getTerm();
					if (term.equalsIgnoreCase("answer")) { 
						reply.setAnswered(true); 
					}
					if (term.equalsIgnoreCase("deleted")) { 
						reply.setDeleted(true);
						reply.setContent("This entry has been deleted.");
					}
				}
				
				list.add(reply);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getForumTopicReplies(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of likes for the topic
	public List<Recommendation> getForumTopicLikes(ForumTopic topic, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_getPostLikes");
		url = url.replace("${uUid}", topic.getuUid());
		return getPostLikes(url, profile);
	}
	
	// get a list of likes for the reply
	public List<Recommendation> getForumTopicReplyLikes(ForumTopicReply reply, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_getPostLikes");
		url = url.replace("${uUid}", reply.getuUid());
		return getPostLikes(url, profile);
	}
	
	private List<Recommendation> getPostLikes(String url, Profile profile) {
		List<Recommendation> list = new ArrayList<Recommendation>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// create like
				Recommendation rec = new Recommendation();
				rec.setOwnerUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				
				list.add(rec);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getPostLikes(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	public Forum createCommunityForum(Forum forum, Community community, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_createCommunityForum");
		url = url.replace("${commUuid}", community.getuUid());
		return createForum(url, forum, profile);
	}
	
	public Forum createForum(Forum forum, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_createForum");
		return createForum(url, forum, profile);
	}
	
	// create forum
	private Forum createForum(String url, Forum forum, Profile profile) {
		ClientResponse response = restBroker.doPost(url, forum.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			forum.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.indexOf("=") + 1);
			forum.setuUid(uUid);
			
			System.out.println("Created forum [" + forum.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create forum [" + forum.getTitle() + "]");
		}
		
		return forum;
	}
	
	// create topic in forum
	public ForumTopic createForumTopic(ForumTopic topic, Forum forum, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_createForumTopic");
		url = url.replace("${uUid}", forum.getuUid());
		ClientResponse response = restBroker.doPost(url, topic.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			topic.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.indexOf("=") + 1);
			topic.setuUid(uUid);
			
			System.out.println("  Created topic [" + topic.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create topic [" + topic.getTitle() + "]");
		}
		
		return topic;
	}
	
	// create a like for the topic
	public void createForumTopicLike(ForumTopic topic, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_createPostLike");
		url = url.replace("${uUid}", topic.getuUid());
		createPostLike(url, profile, "    ");
	}
	
	// create a like for the topic reply
	public void createForumTopicReplyLike(ForumTopicReply reply, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_createPostLike");
		url = url.replace("${uUid}", reply.getuUid());
		createPostLike(url, profile, "      ");
	}
	
	// create a like for the specified content
	private void createPostLike(String url, Profile profile, String prefix) {
		Recommendation like = new Recommendation();

		ClientResponse response = restBroker.doPost(url, like.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			System.out.println(prefix + "Liked by [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to like post [" + profile.getDisplayName() + "]");
		}
	}
	
	// create forum topic reply
	public ForumTopicReply createForumTopicReply(ForumTopicReply reply, Profile profile) {
		String url = Config.URLS.get("forums") + Config.URLS.get("forums_createForumTopicReply");
		url = url.replace("${uUid}", reply.getTopicUuid());
		ClientResponse response = restBroker.doPost(url, reply.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			reply.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.indexOf("=") + 1);
			reply.setuUid(uUid);
			
			System.out.println("    Created topic reply [" + reply.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create topic reply [" + reply.getTitle() + "]");
		}
		
		return reply;
	}
}
