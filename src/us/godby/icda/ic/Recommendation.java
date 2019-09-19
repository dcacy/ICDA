package us.godby.icda.ic;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;

public class Recommendation {

	private Abdera abdera = new Abdera();
	
	private String title = "Like";
	private String ownerUuid = "";
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
		cat.setTerm("recommendation");
		entry.addCategory(cat);
		
		entry.setTitle(getTitle());
		
		//System.out.println(entry.toString());
		return entry;
	}
}
