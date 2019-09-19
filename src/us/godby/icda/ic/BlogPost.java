package us.godby.icda.ic;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;

public class BlogPost {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String content = "";
	private List<String> tags = new ArrayList<String>();
	private String authorUuid = "";
	private String blogHandle = "";
	
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
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public void addTag(String tag) {
		tags.add(tag);
	}	
	public String getBlogHandle() {
		return blogHandle;
	}
	public void setBlogHandle(String blogHandle) {
		this.blogHandle = blogHandle;
	}
	public String getAuthorUuid() {
		return authorUuid;
	}
	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		entry.setTitle(getTitle());
		entry.setSummary(getContent());
		for (String tag : tags) {
			entry.addCategory(tag);
		}
		
		//System.out.println(entry.toString());
		return entry;
	}
}
