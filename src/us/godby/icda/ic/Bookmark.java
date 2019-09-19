package us.godby.icda.ic;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;

public class Bookmark {

private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String content = "";
	private String link = "";
	private List<String> tags = new ArrayList<String>();
	private boolean important = false;
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
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
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
	public boolean isImportant() {
		return important;
	}
	public void setImportant(boolean important) {
		this.important = important;
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
		cat.setTerm("bookmark");
		entry.addCategory(cat);
		
		entry.setTitle(getTitle());
		entry.setContentAsHtml(getContent());
		entry.addLink(getLink());
		for (String tag : tags) {
			entry.addCategory(tag);
		}
		
		if (isImportant()) {
			cat = abdera.getFactory().newCategory();
			cat.setScheme("http://www.ibm.com/xmlns/prod/sn/flags");
			cat.setTerm("important");
			entry.addCategory(cat);
		}
		
		//System.out.println(entry.toString());
		return entry;
	}	
	
}
