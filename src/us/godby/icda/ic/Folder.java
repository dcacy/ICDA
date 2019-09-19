package us.godby.icda.ic;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;

import us.godby.icda.app.Config;

public class Folder {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String label = "";
	private String summary = "";
	private String visibility = "private";		// public, private
	private String authorUuid = "";
	
	public String getuUid() {
		return uUid;
	}
	public void setuUid(String uUid) {
		this.uUid = uUid;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getVisibility() {
		return visibility;
	}
	public void setVisibility(String visibility) {
		this.visibility = visibility;
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
		cat.setScheme("tag:ibm.com,2006:td/type");
		cat.setTerm("collection");
		cat.setLabel("collection");
		entry.addCategory(cat);
		
		entry.setSummary(getSummary());
		
		entry.addSimpleExtension(Config.QNAME_TD_LABEL, getLabel());
		entry.addSimpleExtension(Config.QNAME_TD_VISIBILITY, getVisibility());
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
