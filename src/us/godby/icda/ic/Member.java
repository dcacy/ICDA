package us.godby.icda.ic;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Person;

import us.godby.icda.app.Config;

public class Member {

	private Abdera abdera = new Abdera();
	
	private String role = "";
	private String component = "";
	private String uUid = "";
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getComponent() {
		return component;
	}
	public void setComponent(String component) {
		this.component = component;
	}
	public String getuUid() {
		return uUid;
	}
	public void setuUid(String uUid) {
		this.uUid = uUid;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
	
		Person p = abdera.getFactory().newContributor();
		Element elem = p.addExtension(Config.QNAME_SNX_USER);
		elem.setText(getuUid());
		entry.addContributor(p);
		
		elem = entry.addExtension(Config.QNAME_SNX_ROLE);
		elem.setAttributeValue("component", "http://www.ibm.com/xmlns/prod/sn/" + getComponent());
		elem.setText(getRole());
		entry.addExtension(elem);
		
		//System.out.println(entry.toString());	
		return entry;
	}
	
}
