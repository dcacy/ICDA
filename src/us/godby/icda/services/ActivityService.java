package us.godby.icda.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.protocol.client.ClientResponse;

import us.godby.icda.app.Config;
import us.godby.icda.ic.Activity;
import us.godby.icda.ic.ActivityNode;
import us.godby.icda.ic.Community;
import us.godby.icda.ic.Member;
import us.godby.icda.ic.Profile;
import us.godby.utilities.Countdown;
import us.godby.utilities.RestBroker;

public class ActivityService {

	// utilities
	private RestBroker restBroker = new RestBroker();

	// delete the specified activities
	public void deleteActivities(List<Activity> activities, Profile profile) {
		for (Activity activity : activities) {
			deleteActivity(activity, profile);
		}
	}
	
	// delete a specific activity for the user
	public void deleteActivity(Activity activity, Profile profile) {
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_deleteActivity");
		url = url.replace("${uUid}", activity.getuUid());
		ClientResponse response = restBroker.doDelete(url, profile);
		
		if (response.getStatus() == 204) {
			System.out.println("Deleted activity [" + activity.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
	}
	
	// get all activities for the user
	public List<Activity> getAllMyActivities(Profile profile) {
		List<Activity> activities = getMyActivities(profile);
		List<Activity> completedActivities = getMyCompletedActivities(profile);
		activities.addAll(completedActivities);
		return activities;
	}
	
	// get active activities for the user
	public List<Activity> getMyActivities(Profile profile) {
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_getMyActivities");
		return getActivities(url, profile, false);
	}
	
	// get completed activities for the user
	public List<Activity> getMyCompletedActivities(Profile profile) {
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_getMyCompletedActivities");
		return getActivities(url, profile, true);
	}
	
	// get community activities
	public List<Activity> getCommunityActivities(Community community, Profile profile) {
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_getCommunityActivities");
		url = url.replace("${commUuid}", community.getuUid());
		return getActivities(url, profile, false);
	}
	
	// get a list of the specified activities for the user
	private List<Activity> getActivities(String url, Profile profile, boolean isComplete) {
		List<Activity> list = new ArrayList<Activity>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// create activity
				Activity activity = new Activity();
				activity.setuUid(entry.getExtension(Config.QNAME_SNX_ACTIVITY).getText());
				activity.setTitle(entry.getTitle());
				try { activity.setCommunityUuid(entry.getExtension(Config.QNAME_SNX_COMMUNITYUUID).getText()); } catch (Exception e) {}
				try { activity.setContent(entry.getContent().trim()); } catch (Exception e) {}
				activity.setComplete(isComplete);
				activity.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				// tags
				List<Category> categories = entry.getCategories();
				for (Category category : categories) {
					if (category.getLabel() == null) {
						activity.addTag(category.getTerm());
					}
				}
				
				list.add(activity);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getActivities(link.getHref().toString(), profile, isComplete));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of the members for the activity
	public List<Member> getMembers(Activity activity, Profile profile) {
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_getMembers");
		url = url.replace("${uUid}", activity.getuUid());
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
				member.setComponent("activities");
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
	
	// get a list of the content nodes for the activity
	public List<ActivityNode> getNodes(Activity activity, Profile profile) {
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_getNodes");
		url = url.replace("${uUid}", activity.getuUid());
		return getNodes(url, activity, profile);
	}
	
	private List<ActivityNode> getNodes(String url, Activity activity, Profile profile) {
		List<ActivityNode> list = new ArrayList<ActivityNode>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf(":") + 1);
				
				String sectionUuid = entry.getExtension(Config.QNAME_THR_REPLYTO).getAttributeValue("ref");
				sectionUuid = sectionUuid.substring(sectionUuid.lastIndexOf(":") + 1);
				
				// create activity node
				ActivityNode node = new ActivityNode();
				node.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				node.setType(entry.getCategories("http://www.ibm.com/xmlns/prod/sn/type").get(0).getTerm());
				node.setTitle(entry.getTitle());
				node.setContent(entry.getContent().trim());
				node.setActivityUuid(activity.getuUid());
				node.setSectionUuid(sectionUuid);
				node.setuUid(uUid);
				try { node.setCompleted(Boolean.valueOf(entry.getExtension(Config.QNAME_SNX_ASSIGNEDTO).getAttributeValue("iscompleted"))); } catch (Exception e) {}
				try { node.setAssignedToUuid(entry.getExtension(Config.QNAME_SNX_ASSIGNEDTO).getAttributeValue("userid")); } catch (Exception e) {}
					
				list.add(node);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getNodes(link.getHref().toString(), activity, profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// create community activity
	public Activity createCommunityActivity(Activity activity, Community community, Profile profile) {
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_createCommunityActivity");
		url = url.replace("${commUuid}", community.getuUid());
		activity.setCommunityUuid(community.getuUid());
		return createActivity(url, activity, profile);
	}
	
	// create stand-alone activity
	public Activity createActivity(Activity activity, Profile profile) {
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_createActivity");
		return createActivity(url, activity, profile);
	}
	
	// create activity
	private Activity createActivity(String url, Activity activity, Profile profile) {
		ClientResponse response = restBroker.doPost(url, activity.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			activity.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.indexOf("=") + 1);
			activity.setuUid(uUid);
			
			System.out.println("Created activity [" + activity.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create activity [" + activity.getTitle() + "]");
		}
		
		return activity;
	}
	
	// add a member to the specified activity
	public void addMember(Activity activity, Member member, Profile profile) {		
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_addMember");
		url = url.replace("${uUid}", activity.getuUid());
		ClientResponse response = restBroker.doPost(url, member.getAtomDocument(), profile);
		
		// used for output only...
		Profile memberProfile = Config.PROFILES_UUID.get(member.getuUid());
		
		if (response.getStatus() == 201) {
			System.out.println("  Added " + member.getRole() + " [" + memberProfile.getDisplayName() + "]");
		}
	}
	
	// create activity node
	public ActivityNode createActivityNode(ActivityNode node, Profile profile) {
		return createActivityNode(node, profile, 0);
	}
	
	private ActivityNode createActivityNode(ActivityNode node, Profile profile, int retryAttempt) {
		String url = Config.URLS.get("activities") + Config.URLS.get("activities_createNode");
		url = url.replace("${uUid}", node.getActivityUuid());
		ClientResponse response = restBroker.doPost(url, node.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			node.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.indexOf("=") + 1);
			node.setuUid(uUid);
			
			System.out.println("  Created " + node.getType() + " [" + node.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to add " + node.getType() + " [" + node.getTitle()  + "]");
			
			// it takes time for community members to propagate to a new community activity
			// if a 403 is received, this could be a community acl issue, so retry
			if (response.getStatus() == 403) {
				if (retryAttempt++ < Config.RETRY_COMMUNITY_ACTIVITIES) {
					System.out.println("  API request retry " + retryAttempt + " of " + Config.RETRY_COMMUNITY_ACTIVITIES);
					try {
						Countdown cd = new Countdown(3, Config.SLEEP_COMMUNITY_ACTIVITIES);
						cd.start();
						return createActivityNode(node, profile, retryAttempt);
					} catch (Exception e) {}
				}
			}
		}
		
		return node;
	}
	
}
