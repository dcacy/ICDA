package us.godby.icda.ic;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;

public class ForumTopic {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String content = "";
	private List<String> tags = new ArrayList<String>();
	private boolean pinned = false;
	private boolean locked = false;
	private boolean question = false;
	private boolean answered = false;
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public boolean isPinned() {
		return pinned;
	}
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public boolean isQuestion() {
		return question;
	}
	public void setQuestion(boolean question) {
		this.question = question;
	}
	public boolean isAnswered() {
		return answered;
	}
	public void setAnswered(boolean answered) {
		this.answered = answered;
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
		cat.setTerm("forum-topic");
		entry.addCategory(cat);
		
		entry.setTitle(getTitle());		
		entry.setContent(getContent());
		for (String tag : tags) {
			entry.addCategory(tag);
		}
		
		if (isPinned()) {
			cat = abdera.getFactory().newCategory();
			cat.setScheme("http://www.ibm.com/xmlns/prod/sn/flags");
			cat.setTerm("pinned");
			entry.addCategory(cat);
		}
		if (isLocked()) {
			cat = abdera.getFactory().newCategory();
			cat.setScheme("http://www.ibm.com/xmlns/prod/sn/flags");
			cat.setTerm("locked");
			entry.addCategory(cat);
		}
		if (isQuestion()) {
			cat = abdera.getFactory().newCategory();
			cat.setScheme("http://www.ibm.com/xmlns/prod/sn/flags");
			cat.setTerm("question");
			entry.addCategory(cat);
		}
		if (isAnswered()) {
			cat = abdera.getFactory().newCategory();
			cat.setScheme("http://www.ibm.com/xmlns/prod/sn/flags");
			cat.setTerm("answered");
			entry.addCategory(cat);
		}
		
		//System.out.println(entry.toString());
		return entry;
	}
}
