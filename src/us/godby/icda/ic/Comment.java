package us.godby.icda.ic;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;

public class Comment {

	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String content = "";
	private String refId = "";
	private String refType = "";
	private String ownerUuid = "";
	
	public String getuUid() {
		return uUid;
	}
	public void setuUid(String uUid) {
		this.uUid = uUid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getRefId() {
		return refId;
	}
	public void setRefId(String refId) {
		this.refId = refId;
	}
	public String getRefType() {
		return refType;
	}
	public void setRefType(String refType) {
		this.refType = refType;
	}
	public String getOwnerUuid() {
		return ownerUuid;
	}
	public void setOwnerUuid(String ownerUuid) {
		this.ownerUuid = ownerUuid;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
				
		entry.setContent(getContent());
		
		if (getRefType().equalsIgnoreCase("blogpost")) {
			QName qName = new QName("http://purl.org/syndication/thread/1.0", "in-reply-to", "thr");
			Element elem = entry.addExtension(qName);
			elem.setAttributeValue("ref", "urn:lsid:ibm.com:blogs:entry-" + getRefId());
		}
		else if (getRefType().equalsIgnoreCase("file")) {
			Category cat = abdera.getFactory().newCategory();
			cat.setScheme("tag:ibm.com,2006:td/type");
			cat.setTerm("comment");
			cat.setLabel("comment");
			entry.addCategory(cat);
		}
		else if (getRefType().equalsIgnoreCase("wikipage")) {
			Category cat = abdera.getFactory().newCategory();
			cat.setScheme("tag:ibm.com,2006:td/type");
			cat.setTerm("comment");
			cat.setLabel("comment");
			entry.addCategory(cat);
		}
		
		//System.out.println(entry.toString());
		return entry;
	}
	
}
