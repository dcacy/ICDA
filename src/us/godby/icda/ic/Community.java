package us.godby.icda.ic;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;

import us.godby.icda.app.Config;

public class Community {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String content = "";
	private String type = "";		// private, public, publicInviteOnly
	private boolean isExternal = false;		// can only be true if community is private
	private List<String> tags = new ArrayList<String>();
	private String authorUuid = "";
	private String logo = "";
	private String parentUuid = "";
	private String wikiInstanceId = ""; // dpc
	
	public String getWikiInstanceId() {
		return wikiInstanceId;
	}
	public void setWikiInstanceId(String wikiInstanceId) {
		this.wikiInstanceId = wikiInstanceId;
	}
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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
	public boolean isExternal() {
		return isExternal;
	}
	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}
	public String getAuthorUuid() {
		return authorUuid;
	}
	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getParentUuid() {
		return parentUuid;
	}
	public void setParentUuid(String parentUuid) {
		this.parentUuid = parentUuid;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		Category cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/type");
		cat.setTerm("community");
		entry.addCategory(cat);
		
		entry.setTitle(getTitle());
		entry.setContentAsHtml(getContent());
		entry.addSimpleExtension(Config.QNAME_SNX_COMMUNITYTYPE, getType());
		entry.addSimpleExtension(Config.QNAME_SNX_ISEXTERNAL, String.valueOf(isExternal()));
		
		for (String tag : tags) {
			if (!tag.trim().equalsIgnoreCase("")) {
				entry.addCategory(tag);
			}
		}
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
