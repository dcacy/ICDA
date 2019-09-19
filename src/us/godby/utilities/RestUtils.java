package us.godby.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMCategories;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;

import us.godby.icda.app.Config;

public class RestUtils {
	
	public RestUtils() {
		// turn off some of the verbose logging
		Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.WARNING);
		Logger.getLogger("httpclient.wire.header").setLevel(Level.WARNING);
		Logger.getLogger("httpclient.wire.content").setLevel(Level.WARNING);
		// ssl
		acceptSelfSignedCertificates();
	}
	
	// SSL: accept self signed certificates
	private void acceptSelfSignedCertificates() {
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() { return null; }
				public void checkClientTrusted(X509Certificate[] certs, String authType) {}
				public void checkServerTrusted(X509Certificate[] certs, String authType) {}
			}
		};
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {}
	}
	
	// get a new HTTP client
	private AbderaClient getClient() {
		// set up the abdera client
		Abdera abdera = new Abdera();
		AbderaClient client = new AbderaClient(abdera);
		AbderaClient.registerTrustManager();
		return client;
	}
	
	// set and retrieve some default HTTP request options
	private RequestOptions getOptions(AbderaClient client) {
		RequestOptions options = client.getDefaultRequestOptions();
		options.setUseChunked(false);
		options.setFollowRedirects(true);
		return options;
	}
	
	// add HTTP Basic Authentication to the given client
	private AbderaClient addBasicAuthentication(AbderaClient client, String urlStr, String username, String password) {
		// set up authentication for the request
		try {
			URL u = new URL(urlStr);
			String realm = u.getProtocol() + "://" + u.getHost();
			client.usePreemptiveAuthentication(true);
			client.addCredentials(realm, null, null, new UsernamePasswordCredentials(username, password));
		}
		catch (Exception e) { e.printStackTrace(); }
		return client;
	}
	
	// HTTP DELETE with Basic Authentication
	public ClientResponse doDeleteBasic(String urlStr, String username, String password) {
		try { Thread.sleep(Config.SLEEP_HTTP); } catch (Exception e) {}
		
		AbderaClient client = getClient();
		client = addBasicAuthentication(client, urlStr, username, password);
		ClientResponse response = client.delete(urlStr);
		
		return response;
	}
	
	// HTTP GET
	public ClientResponse doGet(String urlStr) {
		try { Thread.sleep(Config.SLEEP_HTTP); } catch (Exception e) {}
		
		AbderaClient client = getClient();
		ClientResponse response = client.get(urlStr);
		
		return response;
	}
	
	// HTTP GET with Basic Authentication
	public ClientResponse doGetBasic(String urlStr, String username, String password) {
		try { Thread.sleep(Config.SLEEP_HTTP); } catch (Exception e) {}
		
		AbderaClient client = getClient();
		client = addBasicAuthentication(client, urlStr, username, password);
		ClientResponse response = client.get(urlStr);
		
		return response;
	}
	
	// HTTP POST with Basic Authentication and Atom Entry
	public ClientResponse doPostBasic(String urlStr, Entry entry, String username, String password) {
		try { Thread.sleep(Config.SLEEP_HTTP); } catch (Exception e) {}
		
		AbderaClient client = getClient();
		client = addBasicAuthentication(client, urlStr, username, password);
		RequestOptions options = getOptions(client);
		options.setContentType("application/atom+xml");
		ClientResponse response = client.post(urlStr, entry, options);
		
		return response;
	}
	
	// HTTP POST with Basic Authentication and binary file
	public ClientResponse doPostBasic(String urlStr, File file, String username, String password) {
		String fileType = getFileType(file.getName());
		return doPostBasic(urlStr, file, fileType, username, password);
	}
	
	// HTTP POST with Basic Authentication and binary file
	public ClientResponse doPostBasic(String urlStr, File file, String fileType, String username, String password) {
		try { Thread.sleep(Config.SLEEP_HTTP); } catch (Exception e) {}
				
		FileInputStream resource = null;
		try { resource = new FileInputStream(file.getAbsolutePath()); } catch (Exception e) {}
		RequestEntity entity = new InputStreamRequestEntity(resource, fileType);
		
		AbderaClient client = getClient();
		client = addBasicAuthentication(client, urlStr, username, password);
		RequestOptions options = getOptions(client);
		options.setSlug(file.getAbsolutePath());
		ClientResponse response = client.post(urlStr, entity, options);
		
		return response;
	}
	
	// HTTP PUT with Basic Authentication and Atom Entry
	public ClientResponse doPutBasic(String urlStr, Entry entry, String username, String password) {
		try { Thread.sleep(Config.SLEEP_HTTP); } catch (Exception e) {}
		
		AbderaClient client = getClient();
		client = addBasicAuthentication(client, urlStr, username, password);
		RequestOptions options = getOptions(client);
		options.setContentType("application/atom+xml");
		ClientResponse response = client.put(urlStr, entry, options);
		
		return response;
	}
	
	// HTTP PUT with Basic Authentication and binary file
	public ClientResponse doPutBasic(String urlStr, File file, String username, String password) {
		try { Thread.sleep(Config.SLEEP_HTTP); } catch (Exception e) {}
				
		FileInputStream resource = null;
		try { resource = new FileInputStream(file.getAbsolutePath()); } catch (Exception e) {}
		RequestEntity entity = new InputStreamRequestEntity(resource, getFileType(file.getName()));
		
		AbderaClient client = getClient();
		client = addBasicAuthentication(client, urlStr, username, password);
		RequestOptions options = getOptions(client);
		options.setSlug(file.getAbsolutePath());
		ClientResponse response = client.put(urlStr, entity, options);
		
		return response;
	}
	
	// HTTP PUT with Basic Authentication and Atom Categories
	public ClientResponse doPutBasic(String urlStr, FOMCategories categories, String username, String password) {
		try { Thread.sleep(Config.SLEEP_HTTP); } catch (Exception e) {}
		
		AbderaClient client = getClient();
		client = addBasicAuthentication(client, urlStr, username, password);
		RequestOptions options = getOptions(client);
		options.setContentType("application/atom+xml");
		ClientResponse response = client.put(urlStr, categories, options);
		
		return response;
	}
	
	// return file type based off extension
	private String getFileType(String filename) {	
		Map<String,String> fileExtensionMap = new HashMap<String, String>();
		// MS Office
        fileExtensionMap.put("doc", "application/msword");
        fileExtensionMap.put("dot", "application/msword");
        fileExtensionMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        fileExtensionMap.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        fileExtensionMap.put("docm", "application/vnd.ms-word.document.macroEnabled.12");
        fileExtensionMap.put("dotm", "application/vnd.ms-word.template.macroEnabled.12");
        fileExtensionMap.put("xls", "application/vnd.ms-excel");
        fileExtensionMap.put("xlt", "application/vnd.ms-excel");
        fileExtensionMap.put("xla", "application/vnd.ms-excel");
        fileExtensionMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        fileExtensionMap.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        fileExtensionMap.put("xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
        fileExtensionMap.put("xltm", "application/vnd.ms-excel.template.macroEnabled.12");
        fileExtensionMap.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        fileExtensionMap.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        fileExtensionMap.put("ppt", "application/vnd.ms-powerpoint");
        fileExtensionMap.put("pot", "application/vnd.ms-powerpoint");
        fileExtensionMap.put("pps", "application/vnd.ms-powerpoint");
        fileExtensionMap.put("ppa", "application/vnd.ms-powerpoint");
        fileExtensionMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        fileExtensionMap.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        fileExtensionMap.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        fileExtensionMap.put("ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
        fileExtensionMap.put("pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        fileExtensionMap.put("potm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        fileExtensionMap.put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        // Open Office
        fileExtensionMap.put("odt", "application/vnd.oasis.opendocument.text");
        fileExtensionMap.put("ott", "application/vnd.oasis.opendocument.text-template");
        fileExtensionMap.put("oth", "application/vnd.oasis.opendocument.text-web");
        fileExtensionMap.put("odm", "application/vnd.oasis.opendocument.text-master");
        fileExtensionMap.put("odg", "application/vnd.oasis.opendocument.graphics");
        fileExtensionMap.put("otg", "application/vnd.oasis.opendocument.graphics-template");
        fileExtensionMap.put("odp", "application/vnd.oasis.opendocument.presentation");
        fileExtensionMap.put("otp", "application/vnd.oasis.opendocument.presentation-template");
        fileExtensionMap.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
        fileExtensionMap.put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
        fileExtensionMap.put("odc", "application/vnd.oasis.opendocument.chart");
        fileExtensionMap.put("odf", "application/vnd.oasis.opendocument.formula");
        fileExtensionMap.put("odb", "application/vnd.oasis.opendocument.database");
        fileExtensionMap.put("odi", "application/vnd.oasis.opendocument.image");
        fileExtensionMap.put("oxt", "application/vnd.openofficeorg.extension");
		// application
        fileExtensionMap.put("json", "application/json");
        fileExtensionMap.put("js", "application/javascript");
        fileExtensionMap.put("pdf", "application/pdf");
        fileExtensionMap.put("zip", "application/zip");
        fileExtensionMap.put("gz", "application/gzip");
        // audio
        fileExtensionMap.put("mp3", "audio/mp3");
        fileExtensionMap.put("wav", "audio/vnd.wave");
        // image
        fileExtensionMap.put("gif", "image/gif");
        fileExtensionMap.put("jpg", "image/jpg");
        fileExtensionMap.put("jpeg", "image/jpeg");
        fileExtensionMap.put("png", "image/png");
        // text
        fileExtensionMap.put("cmd", "text/cmd");
        fileExtensionMap.put("css", "text/css");
        fileExtensionMap.put("csv", "text/csv");
        fileExtensionMap.put("html", "text/html");
        fileExtensionMap.put("txt", "text/plain");
        fileExtensionMap.put("rtf", "text/rtf");
        fileExtensionMap.put("xml", "text/xml");
        // video
        fileExtensionMap.put("avi", "video/avi");
        fileExtensionMap.put("mpeg", "video/mpeg");
        fileExtensionMap.put("mp4", "video/mp4");
        fileExtensionMap.put("ogg", "video/ogg");
        fileExtensionMap.put("wmv", "video/x-ms-wmv");
        fileExtensionMap.put("flv", "x-flv");
        // vnd
        fileExtensionMap.put("apk", "application/vnd.android.package-archive");
        // prefix
        fileExtensionMap.put("7z", "application/x-7z-compressed");
        fileExtensionMap.put("rar", "application/x-rar-compressed");
        fileExtensionMap.put("swf", "application/x-shockwave-flash");
        fileExtensionMap.put("tar", "application/x-tar");
        // misc
        fileExtensionMap.put("dat", "application/binary");
        
		String ext = filename.substring(filename.lastIndexOf(".") + 1);
		String type = fileExtensionMap.get(ext);
		if (type == null) {
			type = "";
		}
		//System.out.println(type);
		return type;
	}

}
