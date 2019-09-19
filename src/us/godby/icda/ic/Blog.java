package us.godby.icda.ic;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;

import us.godby.icda.app.Config;

public class Blog {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String handle = "";
	private String summary = "";
	private List<String> tags = new ArrayList<String>();
	private String authorUuid = "";
	private String type = "blog";	// blog, communityblog, ideationblog
	private String communityUuid = "";
	private String widgetUuid = "";
	
	public String getuUid() {
		return uUid;
	}
	public void setuUid(String uUid) {
		this.uUid = uUid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAuthorUuid() {
		return authorUuid;
	}
	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}
	public String getCommunityUuid() {
		return communityUuid;
	}
	public void setCommunityUuid(String communityUuid) {
		this.communityUuid = communityUuid;
	}
	public String getWidgetUuid() {
		return widgetUuid;
	}
	public void setWidgetUuid(String widgetUuid) {
		this.widgetUuid = widgetUuid;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		entry.addSimpleExtension(Config.QNAME_SNX_HANDLE, getHandle());
		
		Category cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/type");
		cat.setTerm(getType());
		entry.addCategory(cat);		
		
		entry.setTitle(getTitle());
		entry.setSummary(getSummary());
		for (String tag : tags) {
			entry.addCategory(tag);
		}
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
