package us.godby.icda.ic;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;

import us.godby.icda.app.Config;
import us.godby.utilities.StringUtils;

public class Event {

	private StringUtils stringUtils = new StringUtils();
	
	private Abdera abdera = new Abdera();
	
	private String uUid = "";
	private String title = "";
	private String content = "";
	private String type = "event";
	private String location = "";
	private String frequency = "";
	private String interval = "";
	private String custom = "";
	private String authorUuid = "";
	
	private String dateEnd = "";
	private String dateStart = "";
	private String dateUntil = "";
	private String allDay = "";
	private String byDay = "";
	
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}
	public String getCustom() {
		return custom;
	}
	public void setCustom(String custom) {
		this.custom = custom;
	}
	public String getAuthorUuid() {
		return authorUuid;
	}
	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}
	public String getDateEnd() {
		return dateEnd;
	}
	public void setDateEnd(String dateEnd) {
		this.dateEnd = dateEnd;
	}
	public String getDateStart() {
		return dateStart;
	}
	public void setDateStart(String dateStart) {
		this.dateStart = dateStart;
	}
	public String getDateUntil() {
		return dateUntil;
	}
	public void setDateUntil(String dateUntil) {
		this.dateUntil = dateUntil;
	}
	public String getAllDay() {
		return allDay;
	}
	public void setAllDay(String allDay) {
		this.allDay = allDay;
	}
	public String getByDay() {
		return byDay;
	}
	public void setByDay(String byDay) {
		this.byDay = byDay;
	}
	public Entry getAtomDocument() {
		Entry entry = abdera.newEntry();
		
		if (getInterval().equalsIgnoreCase("null")) { setInterval(""); }
		
		entry.setTitle(getTitle());
		entry.setContentAsHtml(getContent());
				
		Category cat = abdera.getFactory().newCategory();
		cat.setScheme("http://www.ibm.com/xmlns/prod/sn/type");
		cat.setTerm(getType());
		entry.addCategory(cat);
		
		entry.addSimpleExtension(Config.QNAME_SNX_LOCATION, getLocation());
		entry.addSimpleExtension(Config.QNAME_SNX_ALLDAY, getAllDay());
		
		ExtensibleElement elem = entry.addExtension(Config.QNAME_SNX_RECURRENCE);
		elem.setAttributeValue("custom", getCustom());
		
		if (getCustom().equalsIgnoreCase("no")) {
			elem.setAttributeValue("frequency", getFrequency());
			elem.setAttributeValue("interval", getInterval());
			//elem.addSimpleExtension(Config.QNAME_SNX_ALLDAY, getAllDay());
			elem.addSimpleExtension(Config.QNAME_SNX_STARTDATE, getDateStart());
			elem.addSimpleExtension(Config.QNAME_SNX_ENDDATE, getDateEnd());
			elem.addSimpleExtension(Config.QNAME_SNX_UNTIL, getDateUntil());
			if (stringUtils.isNumeric(getByDay())) {
				elem.addSimpleExtension(Config.QNAME_SNX_BYDATE, getByDay());
			}
			else {
				elem.addSimpleExtension(Config.QNAME_SNX_BYDAY, getByDay());
			}
		}
		else {
			ExtensibleElement elem2 = elem.addExtension(Config.QNAME_SNX_PERIOD);
			elem2.addSimpleExtension(Config.QNAME_SNX_STARTDATE, getDateStart());
			elem2.addSimpleExtension(Config.QNAME_SNX_ENDDATE, getDateEnd());
		}
			
		//System.out.println(entry.toString());
		return entry;
	}
}
