

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Example program to list links from a URL.
 */
public class WebCrawlerGui4 {

	public static final int MAX_PAGES = 200;
	private static final String[] highPriorityWords = {"department", "computer", "computing", "electrical", "civil", "chemical", "statistic", "mathematics", "economics", "aeronautics", "anthropology", "architecture", "biology"}; 
	private static final String[] lowPriorityWords = {"academic", "school", "college", "education", "engineering", "research"}; 

	private static class Link {
		private String url;
		private String text = null;
		private int priority = 0;
		private double score = 0d;

		Link(String url, String text) {
			this.url = url;
			this.text = text;
		}
		
		Link(String url, String text, int priority, double score) {
			this.url = url;
			this.text = text;
			this.priority = priority;
			this.score = score;
		}

		public String getUrl() {
			return url;
		}

		public String getText() {
			return text;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}
		
		public String toString() {
			return String.format("Url: %s, Text: %s, Priority: %d, Score: %f", this.getUrl(), this.getText(), this.getPriority(), this.getScore());
		}

	}
	
	private static class LinkPriorityComparator implements Comparator<Link> {

		public int compare(Link link1, Link link2) {
			return link2.getPriority() - link1.getPriority();
		}
		
	}
	
	private static class LinkScoreComparator implements Comparator<Link> {

		public int compare(Link link1, Link link2) {
			if(link2.getScore() > link1.getScore()) {
				return 1;
			} else if (link2.getScore() < link1.getScore()){
				return -1;
			} else {
				return 0;
			}
		}
		
	}
	
	/*
	public static void main2(String[] args) {
		PriorityQueue<Link> searchUrlsList = new PriorityQueue<Link>(50, new LinkPriorityComparator());
		List<Link> probableList = new ArrayList<Link>();
		
		
		Link l1 = new Link("A", null);
		l1.setPriority(2);
		l1.setScore(1.2);
		searchUrlsList.add(l1);
		probableList.add(l1);
		
		Link l2 = new Link("B", null);
		l2.setPriority(1);
		l2.setScore(1.23);
		searchUrlsList.add(l2);
		probableList.add(l2);
		
		System.out.println("Search List...");
		while(!searchUrlsList.isEmpty()) {
			Link link = searchUrlsList.poll();
			System.out.println(String.format("%s, %d, %f", link.getUrl(), link.getPriority(), link.getScore()));
		}
		
		System.out.println("Probable List...");
		Collections.sort(probableList, new LinkScoreComparator());
		for(Link link : probableList){
			System.out.println(String.format("%s, %d, %f", link.getUrl(), link.getPriority(), link.getScore()));
		}
	}*/

	public static void main(String[] args) throws IOException {
		
		//Validate.isTrue(args.length == 1, "usage: supply url to fetch");
        //String url = args[0];
		
		//Queue<Link> searchUrlsList = new LinkedList<Link>();
		PriorityQueue<Link> searchUrlsList = new PriorityQueue<Link>(50, new LinkPriorityComparator());
		Set<String> uniqueUrlsList = new HashSet<String>();
		//List<Link> probableList = new ArrayList<Link>();
		
		String url = "http://www.rice.edu";
		
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		Link startUrl = new Link(url, null);
		//Link startUrl = new Link(url, null);

		searchUrlsList.add(startUrl);
		uniqueUrlsList.add(startUrl.getUrl());

		// Create file
		//BufferedWriter out = new BufferedWriter(new FileWriter("crawl-log.txt"));
		//Pattern pattern = Pattern.compile("Computer Science", Pattern.CASE_INSENSITIVE);
		String patterns[] = {"Computer Science", "Electrical", "Statistics", "Mathematics", "Civil", "Chemical", "Biology", "Chemistry", "Architecture", "Aeronautics", "Anthropology"};
		/*Pattern patterns[] = {Pattern.compile("Computer Science", Pattern.CASE_INSENSITIVE),
								Pattern.compile("Electrical", Pattern.CASE_INSENSITIVE),
								Pattern.compile("Statistics", Pattern.CASE_INSENSITIVE),
								Pattern.compile("Mathematics", Pattern.CASE_INSENSITIVE),
								Pattern.compile("Civil", Pattern.CASE_INSENSITIVE)};*/
		
		Map<String, Pattern> patternMap = new HashMap<String, Pattern>();
		Map<String, List<Link>> probableUrlsMap = new HashMap<String, List<Link>>();
		for(String pattern : patterns){
			patternMap.put(pattern, Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
			probableUrlsMap.put(pattern, new ArrayList<Link>());
			
		}
		

		int crawled = 0;

		while (crawled < MAX_PAGES && !searchUrlsList.isEmpty()) {

			try {

				startUrl = searchUrlsList.poll();

				print("Fetching: url: %s, priority: %d, score: %f", startUrl.getUrl(), startUrl.getPriority(), startUrl.getScore());
				//print("Fetching: (%s)", startUrl.getUrl());
				//out.write(String.format("Fetching: (%s)", startUrl.getUrl()));
				//out.newLine();

				Document doc = Jsoup.connect(startUrl.getUrl()).timeout(5000).get();
				
				//Document doc = Jsoup.parse(downloadHtml(startUrl.getUrl()));

				String bodyText = doc.body().text().trim();

				//if (bodyText == null || bodyText.equalsIgnoreCase("")) {
					//error("Skipped Crawling: url: %s, priority: %d, score: %f - Does not seem to be a valid html document.", startUrl.getUrl(), startUrl.getPriority(), startUrl.getScore());
					//continue;
				//}

				String docTitle = doc.title();
				Elements links = doc.select("a[href]");

				for(String key : patternMap.keySet()){
					
					Pattern pattern = patternMap.get(key);
					
					double score = 0d;

					if (docTitle != null && !docTitle.trim().equalsIgnoreCase("")) {
						Matcher mtitle = pattern.matcher(docTitle);

						if (mtitle.find()) {
							score = 2.0d;
						}
					}

					Matcher matcher = pattern.matcher(bodyText);

					int freq = 0;
					while (matcher.find()) {
						freq++;
					}
					if (freq > 0) {
						score = score + 1 + Math.log10(freq);
					}
					
					if (score > 0) {
						//startUrl.setScore(score);
						//print(">>>>>>>SCORE > 0: pattern: %s, url: %s, priority: %d, score: %f", key, startUrl.getUrl(), startUrl.getPriority(), score);
						//out.write(String.format("\nDocument: (%s), Score: (%f)", startUrl.getUrl(), score));
						//out.newLine();
						
						if (score > 1) {						
							//probableList.add(startUrl);
							probableUrlsMap.get(key).add(new Link(startUrl.getUrl(), startUrl.getText(), startUrl.getPriority(), score));
							
						}
					}
				}
				
				

				crawled++;
				
				//print("\nLinks: (%d)", links.size());
				for (Element link : links) {
					String nextUrl = link.attr("abs:href");
					
					
					if (nextUrl.endsWith("/")) {
						nextUrl = nextUrl.substring(0, nextUrl.length() - 1);
					}
					String nextUrlLcase = nextUrl != null ? nextUrl.toLowerCase() : null;
					
					/*if(!uniqueUrlsList.contains(nextUrl)) {
						uniqueUrlsList.add(nextUrl);
					} else {
						continue;
					}*/

					//print(" * a: <%s>  (%s)", nextUrl, trim(link.text(), 65));
					
					if (nextUrl != null && !nextUrl.trim().equalsIgnoreCase("")
							&& !uniqueUrlsList.contains(nextUrl)
							&& nextUrlLcase.startsWith("http")
							&& nextUrlLcase.contains(".edu")
							&& !nextUrlLcase.contains("?")
							&& !nextUrlLcase.contains("#")
							&& !nextUrlLcase.contains("~")
							&& !nextUrlLcase.endsWith(".jpg")
							&& !nextUrlLcase.endsWith(".png")
							&& !nextUrlLcase.endsWith(".pdf")
							//&& !nextUrlLcase.endsWith(".cfm")
							&& !nextUrlLcase.endsWith(".doc")
							&& !nextUrlLcase.endsWith(".docx")
							&& !nextUrlLcase.endsWith(".xls")
							&& !nextUrlLcase.endsWith(".ppt")
							&& !nextUrlLcase.endsWith(".pptx")
							&& !nextUrlLcase.contains("career")
							&& !nextUrlLcase.contains("calendar")
							&& !nextUrlLcase.contains("faculty")
							&& !nextUrlLcase.contains("staff")
							&& !nextUrlLcase.contains("library")
							&& !nextUrlLcase.contains("parent")
							&& !nextUrlLcase.contains("mail")
							&& !nextUrlLcase.contains("facebook")
							&& !nextUrlLcase.contains("twitter")
							&& !nextUrlLcase.contains("google")
							&& !nextUrlLcase.contains("youtube")
							&& !nextUrlLcase.contains("instagram")
							&& !nextUrlLcase.contains("digg")
							&& !nextUrlLcase.contains("foursquare")
							&& !nextUrlLcase.contains("forum")
							&& !nextUrlLcase.contains("blog")
							&& !nextUrlLcase.contains("search")
							&& !nextUrlLcase.contains("publication")
							&& !nextUrlLcase.contains("volunteer")
							&& !nextUrlLcase.contains("map")
							&& !nextUrlLcase.contains("health")
							&& !nextUrlLcase.contains("news")
							&& !nextUrlLcase.contains("rss")
							&& !nextUrlLcase.contains("community")
							&& !nextUrlLcase.contains("vimeo")
							&& !nextUrlLcase.contains("hotel")
							&& !nextUrlLcase.contains("restaurant")
							&& !nextUrlLcase.contains("housing")
							&& !nextUrlLcase.contains("cinema")
							&& !nextUrlLcase.contains("film")
							&& !nextUrlLcase.contains("sex")
							&& !nextUrlLcase.contains("entertain")
							&& !nextUrlLcase.contains("media")
							&& !nextUrlLcase.contains("event")
							&& !nextUrlLcase.contains("recreation")
							&& !nextUrlLcase.contains("transport")
							&& !nextUrlLcase.contains("travel")
							&& !nextUrlLcase.contains("campaign")
							&& !nextUrlLcase.contains("support")
							&& !nextUrlLcase.contains("magazine")
							&& !nextUrlLcase.contains("ticket")
							&& !nextUrlLcase.contains("photo")
							&& !nextUrlLcase.contains("sport")
							&& !nextUrlLcase.contains("play")
							&& !nextUrlLcase.contains("happen")
							&& !nextUrlLcase.contains("alumni")
							&& !nextUrlLcase.contains("life")
							&& !nextUrlLcase.contains("social")
							&& !nextUrlLcase.contains("group")) {

						//print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 65));

						//out.write(String.format(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 65)));
						//out.newLine();
						
						Link nextUrlLink = new Link(nextUrl, link.text());
						
						for (String word : highPriorityWords) {
					       if (nextUrlLink.getText().toLowerCase().contains(word)) {
					    	   nextUrlLink.setPriority(nextUrlLink.getPriority() + 2);
					       }
					     }
						
						for (String word : lowPriorityWords) {
					       if (nextUrlLink.getText().toLowerCase().contains(word)) {
					    	   nextUrlLink.setPriority(nextUrlLink.getPriority() + 1);
					       }
					    }
						/*
						for (String word : highPriorityWords) {
						       if (nextUrlLcase.contains(word)) {
						    	   nextUrlLink.setPriority(nextUrlLink.getPriority() + 2);
						       }
						     }
							
						for (String word : lowPriorityWords) {
					       if (nextUrlLcase.toLowerCase().contains(word)) {
					    	   nextUrlLink.setPriority(nextUrlLink.getPriority() + 1);
					       }
					    }*/
						
						//uniqueUrlsList.add(nextUrlLink.getUrl());
						
						if(nextUrlLink.getPriority() >= 1) {					
							uniqueUrlsList.add(nextUrl);
							searchUrlsList.add(nextUrlLink);
							//print("Adding: url: %s, priority: %d, score: %f", nextUrlLink.getUrl(), nextUrlLink.getPriority(), nextUrlLink.getScore());
						}
					}
				}

				// print(" * title: %s", title);
				// print(" * body: %s", body);
			} catch (Exception mex) {
				error("Exception Crawling: url: %s, priority: %d, score: %f - Url has errors.", startUrl.getUrl(), startUrl.getPriority(), startUrl.getScore());
				mex.printStackTrace();
			}
		}

		// Close the output stream
		//out.close();

		for(String key : probableUrlsMap.keySet()){
			List<Link> probableList = probableUrlsMap.get(key);
			print("\nPattern : %s", key);
			// Display the list of Documents with scores > 1
			Collections.sort(probableList, new LinkScoreComparator());
			for (Link link : probableList) {
				print("url: %s\tpriority: %d\tscore: %f", link.getUrl(), link.getPriority(), link.getScore());
				//print("\nDocument: (%s), Score: (%f)", link.getUrl(), link.getScore());
			}
		}
	}

	private static void print(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}
	
	private static void error(String msg, Object... args) {
		System.err.println(String.format(msg, args));
	}

	private static String trim(String s, int width) {
		if (s.length() > width)
			return s.substring(0, width - 1) + ".";
		else
			return s;
	}
	
	private static String downloadHtml(String path) throws Exception {
	    InputStream is = null;
	    BufferedReader br = null;
	    try {
	        //String result = "";
	        String line;
	        StringBuilder contentBuffer = new StringBuilder();

	        URL url = new URL(path);
	        is = url.openStream();  // throws an IOException
	        br = new BufferedReader(new InputStreamReader(is));	        

	        while ((line = br.readLine()) != null) {
	        	contentBuffer.append(line).append("\n");
	            //result += line;
	        }
	        //return result;
	        return contentBuffer.toString();
	    } catch (IOException ioe) {
	        ioe.printStackTrace();
	        throw ioe;
	    } finally {
	        try {
	            if (is != null) is.close();
	            if (br != null) br.close();
	        } catch (IOException ioe) {
	            // nothing to see here
	        	
	        }
	    }
	    //return "";
	}
}
