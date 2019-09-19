package us.godby.icda.ic;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;

public class Colleague {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String toUuid = "";
	private String fromUuid = "";
	private String status = "";
	private String content = "";
	private String ownerUuid = "";
	
	public String getuUid() {
		return uUid;
	}
	public void setuUid(String uUid) {
		this.uUid = uUid;
	}
	public String getToUuid() {
		return toUuid;
	}
	public void setToUuid(String toUuid) {
		this.toUuid = toUuid;
	}
	public String getFromUuid() {
		return fromUuid;
	}
	public void setFromUuid(String fromUuid) {
		this.fromUuid = fromUuid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getOwnerUuid() {
		return ownerUuid;
	}
	public void setOwnerUuid(String ownerUuid) {
		this.ownerUuid = ownerUuid;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		Category cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/type");
		cat.setTerm("connection");
		entry.addCategory(cat);
		
		cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/connection/type");
		cat.setTerm("colleague");
		entry.addCategory(cat);
		
		cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/status");
		cat.setTerm(getStatus());
		entry.addCategory(cat);

		entry.setContent(getContent());	
	
		//System.out.println(entry.toString());
		return entry;
	}
	
}
