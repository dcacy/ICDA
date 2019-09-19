package us.godby.icda.ic;

import java.util.Map;
import java.util.TreeMap;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;

import us.godby.icda.app.Config;

public class Widget {

	private Abdera abdera = new Abdera();
	
	private String title = "";
	private String defId = "";
	private String instanceID = "";
	private String location = "";
	public static Map<String,String> properties = new TreeMap<String, String>();
	
	public Widget() {}
	public Widget(String defId) {
		this.defId = defId;
	}
	
	public String getDefId() {
		return defId;
	}
	public void setDefId(String defId) {
		this.defId = defId;
	}
	public String getInstanceID() {
		return instanceID;
	}
	public void setInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getProperty(String key) {
		return properties.get(key);
	}
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		Category cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/type");
		cat.setTerm("widget");
		entry.addCategory(cat);
		
		entry.addSimpleExtension(Config.QNAME_SNX_WIDGETDEFID, getDefId());
		
		//System.out.println(entry.toString());
		return entry;
	}
}
