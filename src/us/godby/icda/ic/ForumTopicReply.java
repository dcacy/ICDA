package us.godby.icda.ic;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;

import us.godby.icda.app.Config;

public class ForumTopicReply {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String content = "";
	private boolean answered = false;
	private boolean deleted = false;
	private String topicUuid = "";
	private String authorUuid = "";
	
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
	public boolean isAnswered() {
		return answered;
	}
	public void setAnswered(boolean answered) {
		this.answered = answered;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public String getTopicUuid() {
		return topicUuid;
	}
	public void setTopicUuid(String topicUuid) {
		this.topicUuid = topicUuid;
	}
	public String getAuthorUuid() {
		return authorUuid;
	}
	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		Category cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/type");
		cat.setTerm("forum-reply");
		entry.addCategory(cat);
		
		Element elem = entry.addExtension(Config.QNAME_THR_REPLYTO);
		elem.setAttributeValue("ref", "urn:lsid:ibm.com:forum:" + getTopicUuid());
		
		entry.setTitle(getTitle());
		entry.setContentAsHtml(getContent());
		
		if (isAnswered()) {
			cat = abdera.getFactory().newCategory();
			cat.setScheme("http://www.ibm.com/xmlns/prod/sn/flags");
			cat.setTerm("answer");
			entry.addCategory(cat);
		}
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
