package us.godby.icda.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.protocol.client.ClientResponse;

import us.godby.icda.app.Config;
import us.godby.icda.ic.Blog;
import us.godby.icda.ic.BlogPost;
import us.godby.icda.ic.Comment;
import us.godby.icda.ic.Community;
import us.godby.icda.ic.Profile;
import us.godby.icda.ic.Recommendation;
import us.godby.utilities.Countdown;
import us.godby.utilities.RestBroker;

public class BlogService {

	private RestBroker restBroker = new RestBroker();
	
	// delete the specified blogs
	public void deleteBlogs(List<Blog> blogs, Profile profile) {
		for (Blog blog : blogs) {
			deleteBlog(blog, profile);
		}
	}
	
	// delete a specific blog for the user
	public void deleteBlog(Blog blog, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_deleteBlog");
		url = url.replace("${uUid}", blog.getuUid());
		ClientResponse response = restBroker.doDelete(url, profile);
		
		if (response.getStatus() == 204) {
			System.out.println("Deleted " + blog.getType() + " [" + blog.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
	}
	
	// get a list of regular blogs for the user
	public List<Blog> getMyBlogs(Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getMyBlogs");
		return getBlogs(url, profile, "blog");
	}
	/*
	// get a list of community blogs for the user
	public List<Blog> getMyCommunityBlogs(Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getMyBlogs");
		return getBlogs(url, profile, "communityblog");
	}
	*/
	// get a list of ideation blogs for the user
	public List<Blog> getMyIdeationBlogs(Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getMyBlogs");
		return getBlogs(url, profile, "ideationblog");
	}
	
	private List<Blog> getBlogs(String url, Profile profile, String blogType) {
		List<Blog> list = new ArrayList<Blog>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// only return blogs of the specified type
				String curType = entry.getCategories("http://www.ibm.com/xmlns/prod/sn/type").get(0).getTerm();
				if (curType.equalsIgnoreCase(blogType)) {
					// get refined data from entry
					String uUid = entry.getId().toString();
					uUid = uUid.substring(uUid.lastIndexOf(":blog-") + 6);
					
					// create a new blog
					Blog blog = new Blog();
					try { blog.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText()); } catch (Exception e) {}
					try { blog.setCommunityUuid(entry.getExtension(Config.QNAME_SNX_COMMUNITYUUID).getText()); } catch (Exception e) {}
					blog.setHandle(entry.getExtension(Config.QNAME_SNX_HANDLE).getText());
					blog.setSummary(entry.getSummary());
					blog.setTitle(entry.getTitle());
					blog.setType(curType);
					blog.setuUid(uUid);
					// tags and type
					List<Category> categories = entry.getCategories();
					for (Category category : categories) {
						if (category.getScheme() == null) {
							blog.addTag(category.getTerm());
						}
					}
					
					list.add(blog);
				}
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getBlogs(link.getHref().toString(), profile, blogType));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	// get the specified blog
	public Blog getBlog(Blog blog, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getBlog");
		url = url.replace("${handle}", blog.getHandle());
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			try {
				// pull from the feed to get the proper id for import...
				Feed feed = (Feed) response.getDocument().getRoot();
				String uUid = feed.getId().toString();
				uUid = uUid.substring(uUid.indexOf("entries-") + 8);
				blog.setuUid(uUid);
				
				blog.setTitle(feed.getTitle());
				
				/*
				// this might throw exception on import...
				// this part needed for export
				Entry entry = feed.getEntries().get(0);
				uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.indexOf("entry-") + 6);
				
				try { blog.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText()); } catch (Exception e) {}
				try { blog.setCommunityUuid(entry.getExtension(Config.QNAME_SNX_COMMUNITYUUID).getText()); } catch (Exception e) {}
				blog.setSummary(entry.getSummary());
				blog.setuUid(uUid);
				*/
			} catch (Exception e) {}
		}
		return blog;
	}
	
	// get community ideation blogs
	public List<Blog> getIdeationBlogs(Community community, Profile profile) {
		List<Blog> list = new ArrayList<Blog>();
		
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getIdeationBlogs");
		url = url.replace("${commUuid}", community.getuUid());
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf(":blog-") + 6);
					
				// create a new blog
				Blog blog = new Blog();
				try { blog.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText()); } catch (Exception e) {}
				try { blog.setCommunityUuid(entry.getExtension(Config.QNAME_SNX_COMMUNITYUUID).getText()); } catch (Exception e) {}
				blog.setHandle(entry.getExtension(Config.QNAME_SNX_HANDLE).getText());
				blog.setTitle(entry.getTitle());
				blog.setType("ideationblog");
				blog.setuUid(uUid);
				// tags and type
				List<Category> categories = entry.getCategories();
				for (Category category : categories) {
					if (category.getScheme() == null) {
						blog.addTag(category.getTerm());
					}
				}
					
				list.add(blog);
			}
		}
		
		return list;
				
		/*
		List<Blog> list = new ArrayList<Blog>();
		
		List<Blog> blogs = getMyIdeationBlogs(profile);
		for (Blog blog : blogs) {
			if (blog.getCommunityUuid().equalsIgnoreCase(community.getuUid())) {
				list.add(blog);
			}
		}
		*/
	}
	
	// get a list of blog posts for the specified blog
	public List<BlogPost> getBlogPosts(Blog blog, Profile profile) {	
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getBlogPosts");
		url = url.replace("${handle}", blog.getHandle());
		return getBlogPosts(url, profile, blog);
	}
	
	private List<BlogPost> getBlogPosts(String url, Profile profile, Blog blog) {
		List<BlogPost> list = new ArrayList<BlogPost>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf(":entry-") + 7);
				
				// create blog post
				BlogPost post = new BlogPost();
				post.setAuthorUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				post.setuUid(uUid);
				post.setTitle(entry.getTitle());
				post.setContent(entry.getContent());
				post.setBlogHandle(blog.getHandle());
				// tags
				List<Category> categories = entry.getCategories();
				for (Category category : categories) {
					post.addTag(category.getTerm());
				}
				
				list.add(post);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getBlogPosts(link.getHref().toString(), profile, blog));
				}
			} catch (Exception e) {}
		}
		
		return list;
	}
	
	public boolean votedForIdea(BlogPost post, Profile profile) {
		boolean voted = false;
		
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getMyVotes");
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				if (entry.getId().toString().contains(post.getuUid())) {
					voted = true;
				}
			}
		}
		else {
			System.out.println(response.getStatus() + " " + profile.getDisplayName());
		}
		
		return voted;
	}
	
	// get a list of likes for the specified blog post
	// Ideation blogs:  Likes = Votes.  Will return a 405 if you attempt to retrieve them.  Are votes confidential? 
	public List<Recommendation> getBlogPostLikes(BlogPost post, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getBlogPostLikes");
		url = url.replace("${handle}", post.getBlogHandle());
		url = url.replace("${uUid}", post.getuUid());
		return getLikes(url, profile);
	}
	
	// get a list of likes for the specified blog post comment
	public List<Recommendation> getBlogPostCommentLikes(Comment comment, BlogPost post, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getBlogPostCommentLikes");
		url = url.replace("${handle}", post.getBlogHandle());
		url = url.replace("${uUid}", comment.getuUid());
		return getLikes(url, profile);
	}
	
	// get a list of likes for the specified content
	private List<Recommendation> getLikes(String url, Profile profile) {
		List<Recommendation> list = new ArrayList<Recommendation>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// create like
				Recommendation rec = new Recommendation();
				rec.setOwnerUuid(entry.getContributors().get(0).getExtension(Config.QNAME_SNX_USER).getText());
				
				list.add(rec);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getLikes(link.getHref().toString(), profile));
				}
			} catch (Exception e) {}
		}

		return list;
	}
	
	// get a list of comments for the specified blog post
	public List<Comment> getBlogPostComments(BlogPost post, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_getBlogPostComments");
		url = url.replace("${handle}", post.getBlogHandle());
		url = url.replace("${uUid}", post.getuUid());
		return getBlogPostComments(url, profile, post);
	}
	
	private List<Comment> getBlogPostComments(String url, Profile profile, BlogPost post) {
		List<Comment> list = new ArrayList<Comment>();
		
		ClientResponse response = restBroker.doGet(url, profile);
		
		if (response.getStatus() == 200) {
			Feed feed = (Feed) response.getDocument().getRoot();
			List<Entry> entries = feed.getEntries();
			for (Entry entry : entries) {
				// get refined data from entry
				String uUid = entry.getId().toString();
				uUid = uUid.substring(uUid.lastIndexOf(":comment-") + 9);
				
				// create comment
				Comment comment = new Comment();
				comment.setuUid(uUid);
				comment.setOwnerUuid(entry.getAuthor().getExtension(Config.QNAME_SNX_USER).getText());
				comment.setContent(entry.getContent());
				comment.setRefId(post.getuUid());
				comment.setRefType("blogpost");
				
				list.add(comment);
			}
			
			// get the next page (if available)
			try {
				Link link = feed.getLinks("next").get(0);
				if (link != null) {
					list.addAll(getBlogPostComments(link.getHref().toString(), profile, post));
				}
			} catch (Exception e) {}
		}

		return list;
	}
	
	// create blog
	public Blog createBlog(Blog blog, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_createBlog");
		return createBlog(url, blog, profile);
	}
	
	public Blog createIdeationBlog(Blog blog, Community community, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_createIdeationBlog");
		url = url.replace("${commUuid}", community.getuUid());
		return createBlog(url, blog, profile);
	}
	
	private Blog createBlog(String url, Blog blog, Profile profile) {
		ClientResponse response = restBroker.doPost(url, blog.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			blog.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.lastIndexOf("/") + 1);
			blog.setuUid(uUid);
			
			System.out.println("Created " + blog.getType() + " [" + blog.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create blog [" + blog.getTitle() + "]");
		}
		
		return blog;
	}
	
	// create blog post
	public BlogPost createBlogPost(BlogPost post, Profile profile) {
		return createBlogPost(post, profile, 0);
	}
	
	private BlogPost createBlogPost(BlogPost post, Profile profile, int retryAttempt) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_createBlogPost");
		url = url.replace("${handle}", post.getBlogHandle());
		ClientResponse response = restBroker.doPost(url, post.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			post.setAuthorUuid(profile.getuUid());
			
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.lastIndexOf("/") + 1);
			post.setuUid(uUid);
			
			System.out.println("  Created blog post [" + post.getTitle() + "] for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to create blog post [" + post.getTitle() + "]");
			
			// it takes time for community members to propagate to a new community activity
			// if a 401 is received, this could be a community acl issue, so retry
			if (response.getStatus() == 401) {
				if (retryAttempt++ < Config.RETRY_COMMUNITY_BLOG) {
					System.out.println("  API request retry " + retryAttempt + " of " + Config.RETRY_COMMUNITY_BLOG);
					try {
						Countdown cd = new Countdown(3, Config.SLEEP_COMMUNITY_BLOG);
						cd.start();
						return createBlogPost(post, profile, retryAttempt);
					} catch (Exception e) {}
				}
			}
		}
		
		return post;
	}
	
	// create a like for the specified blog post
	public void createBlogPostLike(BlogPost post, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_createBlogPostLikes");
		url = url.replace("${handle}", post.getBlogHandle());
		url = url.replace("${uUid}", post.getuUid());
		ClientResponse response = restBroker.doPost(url, profile);
		
		if (response.getStatus() == 200) {
			System.out.println("    Liked by [" + profile.getDisplayName() + "]");
		}
		else if (response.getStatus() == 204) {
			System.out.println("    Voted by [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to like blog post [" + profile.getDisplayName() + "]");
		}
	}
	
	// create a comment for the specified blog post
	public Comment createBlogPostComment(BlogPost post, Comment comment, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_createBlogPostComments");
		url = url.replace("${handle}", post.getBlogHandle());
		ClientResponse response = restBroker.doPost(url, comment.getAtomDocument(), profile);
		
		if (response.getStatus() == 201) {
			// retrieve uUid from location header
			String uUid = response.getLocation().toString();
			uUid = uUid.substring(uUid.lastIndexOf("/") + 1);
			comment.setuUid(uUid);
			
			System.out.println("    Created comment for user [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to comment [" + profile.getDisplayName() + "]");
		}
		
		return comment;
	}
	
	// create a like for the specified blog post comment
	public void createBlogPostCommentLike(BlogPost post, Comment comment, Profile profile) {
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_createBlogPostCommentLikes");
		url = url.replace("${handle}", post.getBlogHandle());
		url = url.replace("${uUid}", comment.getuUid());
		ClientResponse response = restBroker.doPost(url, profile);
		
		if (response.getStatus() == 200) {
			System.out.println("      Liked by [" + profile.getDisplayName() + "]");
		}
		else {
			System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to like blog post comment [" + profile.getDisplayName() + "]");
		}
	}
	
	// update the metadata for a ideation blog
	public Blog updateIdeationBlog(Blog ideationBlog, Profile profile) {
		List<Blog> blogs = this.getMyIdeationBlogs(profile);
		for (Blog blog : blogs) {
			if (blog.getHandle().equalsIgnoreCase(ideationBlog.getHandle())) {
				ideationBlog.setuUid(blog.getuUid());
				ideationBlog = updateBlog(ideationBlog, profile);
			}
		}
		return ideationBlog;
	}
	
	// update the metadata for the specified blog
	public Blog updateBlog(Blog blog, Profile profile) {
		blog = getBlog(blog, profile);
		
		String url = Config.URLS.get("blogs") + Config.URLS.get("blogs_updateBlog");
		url = url.replace("${uUid}", blog.getuUid());
		ClientResponse response = restBroker.doPut(url, blog.getAtomDocument(), profile);
				
		if (response.getStatus() == 200) {
			System.out.println("Updated " + blog.getType() + " metadata [" + blog.getTitle() + "]");
		}
		else {
				System.out.println("[" + response.getStatus() + ": " + response.getStatusText() +"] Failed to update blog [" + blog.getTitle() + "]");
		}
		return blog;
	}
	
}
