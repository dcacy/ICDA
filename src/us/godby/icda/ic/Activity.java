package us.godby.icda.ic;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;

import us.godby.icda.app.Config;

public class Activity {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String content = "";
	private String communityUuid = "";
	private List<String> tags = new ArrayList<String>();
	private boolean isComplete = false;
	private String authorUuid = "";
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getCommunityUuid() {
		return communityUuid;
	}
	public void setCommunityUuid(String communityUuid) {
		this.communityUuid = communityUuid;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public void addTag(String tag) {
		tags.add(tag);
	}
	public String getuUid() {
		return uUid;
	}
	public void setuUid(String uuid) {
		this.uUid = uuid;
	}
	public boolean isComplete() {
		return isComplete;
	}
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	public String getAuthorUuid() {
		return authorUuid;
	}
	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		Category cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/type");
		cat.setTerm("activity");
		cat.setLabel("Activity");
		entry.addCategory(cat);		
		
		entry.setTitle(getTitle());
		entry.setContentAsHtml(getContent());
		for (String tag : tags) {
			entry.addCategory(tag);
		}
		
		if (isComplete()) {
			cat = abdera.getFactory().newCategory();
			cat.setScheme("http://www.ibm.com/xmlns/prod/sn/flags");
			cat.setTerm("completed");
			entry.addCategory(cat);
		}
		
		if (getCommunityUuid() != "") {
			cat = abdera.getFactory().newCategory();
			cat.setScheme("http://www.ibm.com/xmlns/prod/sn/type");
			cat.setTerm("community_activity");
			cat.setLabel("Community Activity");
			entry.addCategory(cat);
			
			entry.addSimpleExtension(Config.QNAME_SNX_COMMUNITYUUID, getCommunityUuid());
		}
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
