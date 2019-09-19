package us.godby.icda.app;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import us.godby.icda.ic.Profile;
import us.godby.icda.services.CommonService;
import us.godby.icda.services.ProfileService;
import us.godby.utilities.StringUtils;
import us.godby.utilities.XmlUtils;

public class ICDA {

	// utilities
	private StringUtils stringUtils = new StringUtils();
	private XmlUtils xmlUtils = new XmlUtils();
	
	// connections
	private CommonService svcCommon = new CommonService();
	private ProfileService svcProfile = new ProfileService();
	
	// data
	private String exportFileName = "";
	private String importFileName = "";
	private String userFileName = "";
	
	// export specific community?
	private boolean exportSpecificCommunity = false;
	private String exportCommunityUuid = "";
	private String exportCommunityOwnerUid = "";
	
	// perform any required initialization, read data from properties file, etc.
	public void init(String[] args) {
		try {
			// properties file
			Properties props = new Properties();
			File file = new File(System.getProperty("user.dir"), "icda.properties");
			FileInputStream fis = new FileInputStream(file);
			props.load(fis);
			fis.close();
			
			// properties
			Config.URLS.put("host", props.getProperty("connections.url"));
			
			Config.AUTH_TYPE = props.getProperty("auth.type");
			Config.AUTH_LOGIN_ATTR = props.getProperty("auth.login.attr");
			
			Config.SLEEP_HTTP = Integer.valueOf(props.getProperty("sleep.http"));
			Config.SLEEP_COMMUNITY_ACTIVITIES = Integer.valueOf(props.getProperty("community.activities.retry.sleep"));
			Config.SLEEP_COMMUNITY_BLOG = Integer.valueOf(props.getProperty("community.blog.retry.sleep"));
			Config.SLEEP_COMMUNITY_FILES = Integer.valueOf(props.getProperty("community.files.retry.sleep"));
			Config.SLEEP_COMMUNITY_WIKIS = Integer.valueOf(props.getProperty("community.wikis.retry.sleep"));
			
			Config.RETRY_COMMUNITY_ACTIVITIES = Integer.valueOf(props.getProperty("community.activities.retry.attempts"));
			Config.RETRY_COMMUNITY_BLOG = Integer.valueOf(props.getProperty("community.blog.retry.attempts"));
			Config.RETRY_COMMUNITY_FILES = Integer.valueOf(props.getProperty("community.files.retry.attempts"));
			Config.RETRY_COMMUNITY_WIKIS = Integer.valueOf(props.getProperty("community.wikis.retry.attempts"));
			
			// dpc added feature to look for an import file to use
			if ( args[0].equalsIgnoreCase("export")) {
				if ( args.length > 1 && args[1] != null ) {
					exportFileName = args[1];
				} else {
					exportFileName = props.getProperty("file.data.export");		
				}
			} else if ( args[0].equalsIgnoreCase("import")) {
				if ( args.length > 1 && args[1] != null ) {
					importFileName = args[1];
				} else {
					importFileName = props.getProperty("file.data.import");
				}
			}

//			exportFileName = props.getProperty("file.data.export");
//			importFileName = props.getProperty("file.data.import");
			userFileName = props.getProperty("file.data.users");
			
			exportSpecificCommunity = Boolean.valueOf(props.getProperty("export.community.single"));
			exportCommunityUuid = props.getProperty("export.community.uUid");
			exportCommunityOwnerUid = props.getProperty("export.community.owner.uid");
			
		} catch (Exception e) {}
		
		// connections; pull URLs for applications from service document (because they might reside on different servers)
		printSection("Getting URLs from service document");
		svcCommon.getServiceConfigs();
	}
	
	// load users from the XML input file
	public void loadUsers(String action) {
		printSection("Loading users [" + userFileName + "]");
		
		// should we load all users in the XML file? (there could be a lot of them)
		boolean loadAll = false;
		
		// map of found uids; must load any found or required by XML
		Map<String,String> uids = new TreeMap<String, String>();
		
		// obtain required uids from XML files
		if (action.equalsIgnoreCase("import")){
			// load ONLY users found in XML data input file
			Document doc = xmlUtils.getXmlFromFile(Config.DIR_DATA, importFileName);
			NodeList nodes = xmlUtils.getNodeListByXPath(doc, "//*[@user]");
			for (int x=0; x < nodes.getLength(); x++) {
				Element elem = (Element) nodes.item(x);
				uids.put(elem.getAttribute("user"), "");
			}
			// also check for Profile network connections
			nodes = xmlUtils.getNodeListByXPath(doc, "//*[@from]");
			for (int x=0; x < nodes.getLength(); x++) {
				Element elem = (Element) nodes.item(x);
				uids.put(elem.getAttribute("from"), "");
			}
			nodes = xmlUtils.getNodeListByXPath(doc, "//*[@to]");
			for (int x=0; x < nodes.getLength(); x++) {
				Element elem = (Element) nodes.item(x);
				uids.put(elem.getAttribute("to"), "");
			}
		}
		else if (action.equalsIgnoreCase("export")) {
			// load all users, because we have no way to know who has done what with content...
			loadAll = true;
		}
		else if (action.equalsIgnoreCase("delete")) {
			// load ONLY users required from XML user input file
			Document doc = xmlUtils.getXmlFromFile(Config.DIR_DATA, userFileName);
			Node node = xmlUtils.getNodeByXPath(doc, "/icda/delete");
			NodeList nodes = node.getChildNodes();
			
			// load only required users, but check for wildcards!
			for (int x=0; x < nodes.getLength(); x++) {
				if (nodes.item(x).getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) nodes.item(x);
					List<String> users = stringUtils.explode(elem.getTextContent(), ",");
					for (String user : users) {
						// check for * wildcard, indicating delete for all users
						if (user.trim().equalsIgnoreCase("*")) {
							loadAll = true;
						}
						else if (!user.trim().equalsIgnoreCase("")) {
							uids.put(user, "");
						}
					}
				}
			}
		}
		
		// if all users need to be loaded
		if (loadAll) {
			// reset the list
			uids.clear();
			
			// load ALL users from XML user file
			Document doc = xmlUtils.getXmlFromFile(Config.DIR_DATA, userFileName);
			NodeList nodes = xmlUtils.getNodeListByXPath(doc, "/icda/users/user");
			for (int x=0; x < nodes.getLength(); x++) {
				Element elem = (Element) nodes.item(x);
				uids.put(elem.getAttribute("uid"), "");
			}
		}
		
		// convert the map into a more manageable list
		List<String> users = new ArrayList<String>();
		for (Map.Entry<String, String> entry: uids.entrySet()) {
			users.add(entry.getKey());
		}
		
		// load required profiles
		Document doc = xmlUtils.getXmlFromFile(Config.DIR_DATA, userFileName);
		svcProfile.loadProfiles(users, doc);
	}
	
	public void importContent() {
		printSection("Importing content [" + importFileName + "]");
		Importer importer = new Importer(importFileName, userFileName);
		importer.importContent();
	}
	
	public void exportContent() {
		printSection("Exporting content [" + exportFileName + "]");
		Exporter exporter = new Exporter(exportFileName, userFileName);
		
		if (exportSpecificCommunity) {
			if (stringUtils.isNotBlank(exportCommunityUuid) && (stringUtils.isNotBlank(exportCommunityOwnerUid))) {
				try {				
					Profile profile = Config.PROFILES.get(exportCommunityOwnerUid);
					exporter.exportCommunity(exportCommunityUuid, profile);
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		else {
			exporter.exportContent();
		}
	}
	
	public void deleteContent() {
		printSection("Deleting content");
		Cleaner cleaner = new Cleaner(userFileName);
		cleaner.cleanContent();
	}
	
	public void printSection(String text) {
		System.out.println("");
		System.out.println("* ========================================");
		System.out.println("*  " + text);
		System.out.println("* ========================================");
		System.out.println("");
	}
	
	public static void main(String[] args) {
	
		System.out.println("");
		System.out.println("* ========================================");
		System.out.println("*  Name: " + Config.APP_NAME);
		System.out.println("*  Author: " + Config.APP_AUTHOR);
		System.out.println("*  Build: " + Config.APP_VERSION + "_" + Config.APP_VERSION_DATE);
		System.out.println("* ========================================");
		
		ICDA app = new ICDA();
		
		// initialize
		long start = System.nanoTime();
		app.init(args);
		
		// FOR TESTING ONLY (no need to supply args, can chain together operations)
		if (args.length == 0) {
			//app.loadUsers("export");
			//app.exportContent();
			
			//app.loadUsers("delete");
			//app.deleteContent();
			
			//app.loadUsers("import");
			//app.importContent();
		}
		
		// application control
		if (args.length == 0) {
			app.printSection("You must supply a valid argument: import, export, or delete");
		}
		else if (args[0].equalsIgnoreCase("import")) {
			app.loadUsers("import");
			app.importContent();
		}
		else if (args[0].equalsIgnoreCase("export")) {
			app.loadUsers("export");
			app.exportContent();
		}
		else if (args[0].equalsIgnoreCase("delete")) {
			app.loadUsers("delete");
			app.deleteContent();
		}
		else {
			app.printSection("You must supply a valid argument: import, export, or delete");
		}
		
		long stop = System.nanoTime();
		long seconds = TimeUnit.SECONDS.convert((stop - start), TimeUnit.NANOSECONDS);
		long minutes = seconds / 60;
		seconds -= minutes * 60;
		app.printSection("Total elapsed time: " + minutes + " minutes " + seconds + " seconds");
	}
	
}
