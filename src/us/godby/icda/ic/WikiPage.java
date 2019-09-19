package us.godby.icda.ic;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;

import us.godby.icda.app.Config;

public class WikiPage {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String content = "";
	private String label = "";
	private List<String> tags = new ArrayList<String>();
	private String authorUuid = "";
	private String wikiUuid = "";
	
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
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
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
	public String getWikiUuid() {
		return wikiUuid;
	}
	public void setWikiUuid(String wikiUuid) {
		this.wikiUuid = wikiUuid;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		Category cat = abdera.getFactory().newCategory();
		cat.setScheme("tag:ibm.com,2006:td/type");
		cat.setTerm("page");
		cat.setLabel("page");
		entry.addCategory(cat);
		
		entry.setTitle(getTitle());
		entry.setContent(getContent());
		entry.getContentElement().setAttributeValue("type", "text/html");
		for (String tag : tags) {
			entry.addCategory(tag);
		}
		
		Element elem = entry.addExtension(Config.QNAME_TD_LABEL);
		elem.setText(getLabel());
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
