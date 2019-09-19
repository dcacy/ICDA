package us.godby.icda.ic;

import java.util.ArrayList;
import java.util.List;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;

import us.godby.icda.app.Config;

public class File {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String summary = "";
	private String visibility = "private";
	private String label = "";
	private List<String> tags = new ArrayList<String>();
	private String fileType = "";
	private String authorUuid = "";
	private String libraryUuid = "";
	
	public String getuUid() {
		return uUid;
	}
	public void setuUid(String uUid) {
		this.uUid = uUid;
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
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getAuthorUuid() {
		return authorUuid;
	}
	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}
	public String getLibraryUuid() {
		return libraryUuid;
	}
	public void setLibraryUuid(String libraryUuid) {
		this.libraryUuid = libraryUuid;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		entry.setSummary(getSummary());
		for (String tag : tags) {
			entry.addCategory(tag);
		}
		
		entry.addSimpleExtension(Config.QNAME_TD_VISIBILITY, getVisibility());
		entry.addSimpleExtension(Config.QNAME_TD_LABEL, getLabel());
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
