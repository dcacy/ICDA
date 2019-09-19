package us.godby.icda.services;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;
import org.apache.abdera.protocol.client.ClientResponse;
import us.godby.icda.app.Config;
import us.godby.icda.ic.Comment;
import us.godby.icda.ic.Community;
import us.godby.icda.ic.File;
import us.godby.icda.ic.Folder;
import us.godby.icda.ic.Profile;
import us.godby.utilities.Countdown;
import us.godby.utilities.RestBroker;

public class FileService {

	// utilities
	private RestBroker restBroker = new RestBroker();
	
	// delete the specified folders
	public void deleteFolders(List<Folder> folders, Profile profile) {
		for (Folder folder : folders) {
			deleteFolder(folder, profile);
		}
	}
	
	// delete specific folder for the user
	public void deleteFolder(Folder folder, Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_deleteFolder");
		url = url.replace("${uUid}", folder.getuUid());
		ClientResponse response = restBroker.doDelete(url, profile);
		
		if (response.getStatus() == 204) {
			System.out.println("Deleted folder [" + folder.getLabel() + "] for user [" + profile.getDisplayName() + "]");
		}
	}

	// delete the specified files
	public void deleteFiles(List<File> files, Profile profile) {
		for (File file : files) {
			deleteFile(file, profile);
		}
	}
	
	// delete specific file for user
	public void deleteFile(File file, Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_deleteFile");
		url = url.replace("${uUid}", file.getuUid());
		ClientResponse response = restBroker.doDelete(url, profile);
		
		if (response.getStatus() == 204) {
			System.out.println("Deleted file [" + file.getLabel() + "] for user [" + profile.getDisplayName() + "]");
		}
	}
	
	// get a list of folders for the user
	public List<Folder> getMyFolders(Profile profile) {	
		String url = Config.URLS.get("files") + Config.URLS.get("files_getMyFolders");
		url = url.replace("${uUid}", profile.getuUid());
		return getFolders(url, profile);
	}
	
	private List<Folder> getFolders(String url, Profile profile) {
		List<Folder> list = new ArrayList<Folder>();

		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {	
				// create folder
				Folder folder = new Folder();
				folder.setLabel(entry.getExtension(Config.QNAME_TD_LABEL).getText());
				folder.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				folder.setSummary(entry.getSummary());
				folder.setuUid(entry.getExtension(Config.QNAME_TD_UUID).getText());
				folder.setVisibility(entry.getExtension(Config.QNAME_TD_VISIBILITY).getText());
			
				list.add(folder);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getFolders(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get a list of community files
	public List<File> getCommunityFiles(Community community, Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_getCommunityFilesAndFolders");
		url = url.replace("${commUuid}", community.getuUid());
		return getFiles(url, profile);
	}
	
	// get my files for the user
	public List<File> getMyFiles(Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_getMyFiles");
		return getFiles(url, profile);
	}
	
	// get files in the specified folder
	public List<File> getFolderFiles(Folder folder, Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_getFolderFiles");
		url = url.replace("${uUid}", folder.getuUid());
		return getFiles(url, profile);
	}
	
	// get a list of specified files for the user
	private List<File> getFiles(String url, Profile profile) {
		List<File> list = new ArrayList<File>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				
				// for each entry in the feed, retrieve the file entry for more info
				String url2 = Config.URLS.get("files") + Config.URLS.get("files_getFile");
				url2 = url2.replace("${libraryUuid}", entry.getExtension(Config.QNAME_TD_LIBRARY).getText());	
				url2 = url2.replace("${documentUuid}", entry.getExtension(Config.QNAME_TD_UUID).getText());
				ClientResponse response2 = restBroker.doGet(url2, profile);
				
				if (response2.getStatus() == 200) {		
					Entry entry2 = (Entry) response2.getDocument().getRoot();
					// create file
					File file = new File();
					file.setFileType(entry2.getLink("enclosure").getAttributeValue("type"));
					file.setLabel(entry2.getExtension(Config.QNAME_TD_LABEL).getText());
					file.setLibraryUuid(entry2.getExtension(Config.QNAME_TD_LIBRARY).getText());
					file.setAuthorUuid(entry2.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
					file.setSummary(entry2.getSummary());
					file.setuUid(entry2.getExtension(Config.QNAME_TD_UUID).getText());
					try { file.setVisibility(entry2.getExtension(Config.QNAME_TD_VISIBILITY).getText()); } catch (Exception e) {}
					// tags
					List<Category> categories = entry2.getCategories();
					for (Category category : categories) {
						if (category.getScheme() == null) {
							file.addTag(category.getTerm());
						}
					}
					
					list.add(file);
				}
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getFiles(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// download the binary file
	public File downloadFile(File file, Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_downloadFile");
		url = url.replace("${libraryUuid}", file.getLibraryUuid());	
		url = url.replace("${documentUuid}", file.getuUid());
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			// save the file to the "files" sub-directory
			try {
				InputStream is = response.getInputStream();
				byte[] buffer = new byte[4096];
				int n = -1;
				
				java.io.File outFile = new java.io.File(Config.DIR_FILES, file.getLabel());
				OutputStream os = new FileOutputStream(outFile, false);
				while ((n = is.read(buffer)) != -1) {
					os.write(buffer, 0, n);
				}
				os.close();
				
				if (outFile.length() > 0) {
					System.out.println("  Downloaded file.");
				}
				else {
					System.out.println("  Downloaded file. Empty file was deleted.");
					outFile.delete();
				}
			} catch (Exception e) {}
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to download file [" + file.getLabel() + "]");
		}
		
		return file;
	}
	
	// get a list of comments for the specified file
	public List<Comment> getFileComments(File file, Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_getFileComments");
		url = url.replace("${libraryUuid}", file.getLibraryUuid());
		url = url.replace("${documentUuid}", file.getuUid());
		return getComments(url, profile);
	}
	
	private List<Comment> getComments(String url, Profile profile) {
		List<Comment> list = new ArrayList<Comment>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf(":") + 1);
				
				// create comment
				Comment comment = new Comment();
				comment.setuUid(uUid);
				comment.setOwnerUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				comment.setContent(entry.getContent());
				
				list.add(comment);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getComments(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}

		return list;
	}
	
	public String getCommunityFilePublishURL(Community community, Profile profile) {
		String link = "";
		
		String url = Config.URLS.get("files") + Config.URLS.get("files_getCommunityIntrospection");
		url = url.replace("${commUuid}", community.getuUid());
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Service service = (Service) response.getDocument().getRoot();
			Workspace workspace = service.getWorkspace("Community Library");
			Collection collection = workspace.getCollection("Documents Feed");
			link = collection.getHref().toString();
		}

		return link;
	}
	
	// create folder
	public Folder createFolder(Folder folder, Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_createFolder");
		ClientResponse response = restBroker.doPost(url, folder.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			folder.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.indexOf("collection/") + 11);
			uUid = uUid.substring(0, uUid.indexOf("/"));
			folder.setuUid(uUid);
			
			System.out.println("Created folder [" + folder.getLabel() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create folder [" + folder.getLabel() + "]");
		}
		
		return folder;
	}
	
	// create file in the specified folder
	public File createFile(Folder folder, File file, Profile profile) {
		// only existing files can be added to a folder, so first upload the actual file
		file = createFile(file, profile);
		
		// now add the file to the folder
		String url = Config.URLS.get("files") + Config.URLS.get("files_addFileToFolder");
		url = url.replace("${folderUuid}", folder.getuUid());
		url = url.replace("${fileUuid}", file.getuUid());
		ClientResponse response = restBroker.doPost(url, profile);
		
		if (response.getStatus() == 204) {
			System.out.println("  Added file [" + file.getLabel() + "] to folder [" + folder.getLabel() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to add file to folder [" + file.getLabel() + "]");
		}
		
		return file;
	}
	
	public File createFile(File file, Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_createFile");
		return createFile(url, file, profile);
	}
	
	public File createCommunityFile(String url, File file, Profile profile) {
		return createFile(url, file, profile);
	}
	
	// create file
	private File createFile(String url, File file, Profile profile) {
		return createFile(url, file, profile, 0);
	}
	
	private File createFile(String url, File file, Profile profile, int retryAttempt) {
		try {
			// get the binary file.  If it does not exist, create a temporary file
			java.io.File attachment = new java.io.File(Config.DIR_FILES, file.getLabel());
			boolean isNewFile = attachment.createNewFile();
			// if this file exists but was previously a temporary file, clean it up
			if ((!isNewFile) && (attachment.length() == 0)) {
				isNewFile = true;
			}
			
			// first, upload the binary file
			ClientResponse response = restBroker.doPost(url, attachment, file.getFileType(), profile);
			
			if (response.getStatus() == 201) {
				file.setAuthorUuid(profile.getuUid());
				
				// retrieve uUid from location header
				String uUid = response.getLocation().toString();
				uUid = uUid.substring(uUid.indexOf("document/") + 9);
				uUid = uUid.substring(0, uUid.indexOf("/"));
				file.setuUid(uUid);
				
				// retrieve library uUid from location header
				String libUuid = response.getLocation().toString();
				libUuid = libUuid.substring(libUuid.indexOf("library/") + 8);
				libUuid = libUuid.substring(0, libUuid.indexOf("/"));
				file.setLibraryUuid(libUuid);
				
				System.out.println("Created file [" + file.getLabel() + "] for user [" + profile.getDisplayName() + "]");
				
				// now that the binary file has been uploaded, update the metadata
				String url2 = response.getLocation().toString();
				ClientResponse response2 = restBroker.doPut(url2, file.getAtomDocument(), profile);
				
				if (response2.getStatus() == 200) {
					System.out.println("  Updated file metadata");
				}
				else {
					System.out.println("  Failed to update file metadata!");
				}
				
			}
			else {
				System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create file [" + file.getLabel() + "]");
				
				// it takes time for community members to propagate to a new community activity
				// if a 403 is received, this could be a community acl issue, so retry
				if (response.getStatus() == 403) {
					if (retryAttempt++ < Config.RETRY_COMMUNITY_FILES) {
						System.out.println("  API request retry " + retryAttempt + " of " + Config.RETRY_COMMUNITY_FILES);
						try {
							Countdown cd = new Countdown(3, Config.SLEEP_COMMUNITY_FILES);
							cd.start();
							return createFile(url, file, profile, retryAttempt);
						} catch (Exception e) {}
					}
				}
			}
			
			// if a temporary file was used, clean it up
			if (isNewFile) { attachment.delete(); }
			
		} catch (Exception e) { e.printStackTrace(); }
		
		return file;
	}
	
	// create comment for the specified file
	public Comment createFileComment(File file, Comment comment, Profile profile) {
		String url = Config.URLS.get("files") + Config.URLS.get("files_createFileComment");
		url = url.replace("${libraryUuid}", file.getLibraryUuid());
		url = url.replace("${documentUuid}", file.getuUid());
		ClientResponse response = restBroker.doPost(url, comment.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.lastIndexOf("/") + 1);
			comment.setuUid(uUid);
			
			System.out.println("  Created comment for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to comment [" + profile.getDisplayName() + "]");
		}
		
		return comment;
	}
}
