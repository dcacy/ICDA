package us.godby.icda.ic;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;

import us.godby.icda.app.Config;

public class Wiki {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String summary = "";
	private String label = "";
	private String authorUuid = "";
	private String visibility = "";
	private List<Member> members = new ArrayList<Member>();
	private String communityUuid = ""; // dpc
	private String communityForumWidgetId = ""; // dpc
	
	public String getCommunityForumWidgetId() {
		return communityForumWidgetId;
	}
	public void setCommunityForumWidgetId(String communityForumWidgetId) {
		this.communityForumWidgetId = communityForumWidgetId;
	}
	public String getCommunityUuid() {
		return communityUuid;
	}
	public void setCommunityUuid(String communityUuid) {
		this.communityUuid = communityUuid;
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
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getAuthorUuid() {
		return authorUuid;
	}
	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}
	public String getVisibility() {
		return visibility;
	}
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	public List<Member> getMembers() {
		return members;
	}
	public void setMembers(List<Member> members) {
		this.members = members;
	}
	public void addMember(Member member) {
		members.add(member);
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		Category cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/type");
		cat.setTerm("wiki");
		cat.setLabel("wiki");
		entry.addCategory(cat);		
		
		entry.setTitle(getTitle());
		entry.setSummary(getSummary());
		
		Element elem = entry.addExtension(Config.QNAME_TD_LABEL);
		elem.setText(getLabel());
		
		ExtensibleElement exElem = (ExtensibleElement) abdera.getFactory().newElement(Config.QNAME_TD_SHAREWITH);
		entry.addExtension(exElem);
		
		// ACL Matrix:  value stores in "visibility" attribute
		//   PUBLIC_TO_MODIFIERS:  all-authenticated-users
		//   PUBLIC_TO_VIEWERS:  all-anonymous-users
		//   SHARE:  members added
		//   PRIVATE:  no members added
		
		// overall ACL
		if (getVisibility().startsWith("PUBLIC")) {
			String id = "anonymous-user";
			if (getVisibility().endsWith("MODIFIERS")) {
				id = "all-authenticated-users";
			}
			
			Element elem2 = exElem.addExtension(Config.QNAME_CA_MEMBER);
			elem2.setAttributeValue(Config.QNAME_CA_ID, id);
			elem2.setAttributeValue(Config.QNAME_CA_TYPE, "virtual");
			elem2.setAttributeValue(Config.QNAME_CA_ROLE, "reader");
		}
		
		// add individual members to ACL
		for (Member member : members) {
			Element elem2 = exElem.addExtension(Config.QNAME_CA_MEMBER);
			elem2.setAttributeValue(Config.QNAME_CA_ID, member.getuUid());
			elem2.setAttributeValue(Config.QNAME_CA_TYPE, "user");
			elem2.setAttributeValue(Config.QNAME_CA_ROLE, member.getRole());
		}
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
