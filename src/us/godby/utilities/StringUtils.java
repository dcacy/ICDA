package us.godby.utilities;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StringUtils {
	
	// see if a CSV string contains the specified XML file uid
	public boolean stringContainsUid(String str, String uid) {
		boolean result = false;
		String[] uids = str.split(",");
		for (int x=0; x < uids.length; x++) {
			if ((uids[x].equalsIgnoreCase("*")) || (uids[x].equalsIgnoreCase(uid))) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	// join a string with default delimiter
	public String join(List<String> list) {
		return join(list, ",");
	}
	
	// join a string with the specified delimiter
	public String join(List<String> list, String delimiter) {
		// remove any blank values that might be in the list
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			String str = it.next();
			if (str.trim().equalsIgnoreCase("")) {
				it.remove();
			}
		}
		
		// join!
		return org.apache.commons.lang.StringUtils.join(list, delimiter);
	}

	// return a list of strings from a source string with the specified delimiter
	public List<String> explode(String str, String delimiter) {
		return Arrays.asList(str.split("\\s*" + delimiter + "\\s*"));
	}
	
	// return a URL encoded version of the specified string
	public String urlEncode(String str) {
		try {
			str = URLEncoder.encode(str, "UTF-8").replace("+", "%20");
		} catch (Exception e) {}
		
		return str;
	}
	
	public String repeat(String str, int count) {
		return org.apache.commons.lang.StringUtils.repeat(str, count);
	}
	
	public String removeHTML(String str) {
		str = str.replaceAll("\\<.*?\\>", "");
		return str;
	}

	public String removeEntities(String str) {
		str = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(str);
		return str;
	}
	
	public boolean isNotBlank(String str) {
		return org.apache.commons.lang.StringUtils.isNotBlank(str);
	}
	
	public boolean isNumeric(String str) {
		return org.apache.commons.lang.StringUtils.isNumeric(str);
	}
}