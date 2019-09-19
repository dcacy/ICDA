package us.godby.utilities;

import java.io.File;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMCategories;
import org.apache.abdera.protocol.client.ClientResponse;

import us.godby.icda.app.Config;
import us.godby.icda.ic.Profile;

public class RestBroker {

	private Abdera abdera = new Abdera();
	private RestUtils restUtils = new RestUtils();
	
	// These functions sit between the Connections Services classes and the REST Utilities class.
	// Switches for different login attributes should be made here.
	// Switches for custom authentication schemes (Cookie, SAML, OAuth, etc.) should also be made here.
	
	public ClientResponse doDelete(String urlStr, Profile profile) {
		String username = (Config.AUTH_LOGIN_ATTR.equalsIgnoreCase("mail")) ? profile.getEmail() : profile.getUid();
		return restUtils.doDeleteBasic(urlStr, username, profile.getPassword());
	}
	
	public ClientResponse doGet(String urlStr) {
		return restUtils.doGet(urlStr);
	}
	
	public ClientResponse doGet(String url, Profile profile) {
		String username = (Config.AUTH_LOGIN_ATTR.equalsIgnoreCase("mail")) ? profile.getEmail() : profile.getUid();
		return restUtils.doGetBasic(url, username, profile.getPassword());
	}
	
	public ClientResponse doPost(String url, Profile profile) {
		String username = (Config.AUTH_LOGIN_ATTR.equalsIgnoreCase("mail")) ? profile.getEmail() : profile.getUid();
		return restUtils.doPostBasic(url, abdera.newEntry(), username, profile.getPassword());
	}
	
	public ClientResponse doPost(String url, Entry entry, Profile profile) {
		String username = (Config.AUTH_LOGIN_ATTR.equalsIgnoreCase("mail")) ? profile.getEmail() : profile.getUid();
		return restUtils.doPostBasic(url, entry, username, profile.getPassword());
	}
	
	public ClientResponse doPost(String url, File file, Profile profile) {
		String username = (Config.AUTH_LOGIN_ATTR.equalsIgnoreCase("mail")) ? profile.getEmail() : profile.getUid();
		return restUtils.doPostBasic(url, file, username, profile.getPassword());
	}
	
	public ClientResponse doPost(String url, File file, String fileType, Profile profile) {
		String username = (Config.AUTH_LOGIN_ATTR.equalsIgnoreCase("mail")) ? profile.getEmail() : profile.getUid();
		return restUtils.doPostBasic(url, file, fileType, username, profile.getPassword());
	}
	
	public ClientResponse doPut(String url, Entry entry, Profile profile) {
		String username = (Config.AUTH_LOGIN_ATTR.equalsIgnoreCase("mail")) ? profile.getEmail() : profile.getUid();
		return restUtils.doPutBasic(url, entry, username, profile.getPassword());
	}
	
	public ClientResponse doPut(String url, File file, Profile profile) {
		String username = (Config.AUTH_LOGIN_ATTR.equalsIgnoreCase("mail")) ? profile.getEmail() : profile.getUid();
		return restUtils.doPutBasic(url, file, username, profile.getPassword());
	}
	
	public ClientResponse doPut(String url, FOMCategories categories, Profile profile) {
		String username = (Config.AUTH_LOGIN_ATTR.equalsIgnoreCase("mail")) ? profile.getEmail() : profile.getUid();
		return restUtils.doPutBasic(url, categories, username, profile.getPassword());
	}
}
