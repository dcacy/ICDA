package us.godby.icda.ic;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;

import us.godby.icda.app.Config;

public class ActivityNode {

	private Abdera abdera = new Abdera();
	
	private String type = "";
	private String title = "";
	private String content = "";
	private String activityUuid = "";
	private String sectionUuid = "";
	private String uUid = "";
	private String authorUuid = "";
	private boolean isCompleted = false;
	private String assignedToUuid = "";
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
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
	public String getActivityUuid() {
		return activityUuid;
	}
	public void setActivityUuid(String activityUuid) {
		this.activityUuid = activityUuid;
	}
	public String getSectionUuid() {
		return sectionUuid;
	}
	public void setSectionUuid(String sectionUuid) {
		this.sectionUuid = sectionUuid;
	}
	public String getuUid() {
		return uUid;
	}
	public void setuUid(String uUid) {
		this.uUid = uUid;
	}
	public boolean isCompleted() {
		return isCompleted;
	}
	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}
	public String getAssignedToUuid() {
		return assignedToUuid;
	}
	public void setAssignedToUuid(String assignedToUuid) {
		this.assignedToUuid = assignedToUuid;
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
		cat.setTerm(getType());
		cat.setLabel(getType());
		entry.addCategory(cat); 
		
		entry.setTitle(getTitle());
		entry.setContentAsHtml(getContent());
		
		if (isCompleted()) {
			cat = abdera.getFactory().newCategory();
			cat.setScheme("http://www.ibm.com/xmlns/prod/sn/flags");
			cat.setTerm("completed");
			entry.addCategory(cat);
		}
		
		if (!getAssignedToUuid().equalsIgnoreCase("")) {
			Element elem = entry.addExtension(Config.QNAME_SNX_ASSIGNEDTO);
			elem.setAttributeValue("userid", getAssignedToUuid());
		}
		
		if (!getSectionUuid().equalsIgnoreCase("")) {
			String url = Config.URLS.get("activities") + Config.URLS.get("activities_editNode");
			url = url.replace("${uUid}", getSectionUuid());
			
			Element elem = entry.addExtension(Config.QNAME_THR_REPLYTO);
			elem.setAttributeValue("ref", "urn:lsid:ibm.com:oa:" + getSectionUuid());
			elem.setAttributeValue("type", "application/atom+xml");
			elem.setAttributeValue("href", url);
			elem.setAttributeValue("source", "urn:lsid:ibm.com:oa:" + getActivityUuid());
		}
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
