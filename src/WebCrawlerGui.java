import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import java.util.*;


public class WebCrawlerGui extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;

	private javax.swing.JLabel lblStartUrl;
	private javax.swing.JLabel lblMaxPages;
	private javax.swing.JLabel lblSearchResults;
	private javax.swing.JLabel lblStatus;
	private javax.swing.JTextField txtStartUrl;
	private javax.swing.JTextField txtMaxPages;
	private javax.swing.JTextArea txtAreaSearchResults;
	private javax.swing.JScrollPane scrollPaneSearchResults;
	private javax.swing.JButton btnStart;
	private javax.swing.JButton btnStop;

	private class SearchThread extends Thread {
		private boolean execute = true;

		public void run() {
			System.out.println("Started Crawling...");
			lblStatus.setText("Crawling...");
			
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
			
			this.crawl();
			
			if (execute == true) {
				lblStatus.setText("Done!!");
			} else {
				lblStatus.setText("Stopped!!");
			}
			
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
		}

		public void crawl() {
			Queue<String> searchUrl = new LinkedList<String>();
			Set<String> uniqueUrl = new HashSet<String>();

			int MAX_PAGES = Integer.parseInt(txtMaxPages.getText());
			int crawled = 0;

			Pattern httpPattern = Pattern.compile("<a\\s+href\\s*=\\s*\"http(.*?)[\"|>]|<a\\s+href\\s*=\\s*'http(.*?)['|>]", Pattern.CASE_INSENSITIVE);
			// String startUrl = getValidURL("http://web.mit.edu/");
			String startUrl = getValidURL(txtStartUrl.getText());

			searchUrl.add(startUrl);
			uniqueUrl.add(startUrl);
			while (execute == true && !searchUrl.isEmpty() && crawled < MAX_PAGES) {
				try {

					startUrl = searchUrl.poll();
					URL url = new URL(startUrl);

					// Open the address and create a BufferedReader with the
					// source code.
					BufferedReader source = new BufferedReader(new InputStreamReader(url.openStream()));
					txtAreaSearchResults.append(startUrl + "\n");
					String sourceLine;
					StringBuilder contentBuffer = new StringBuilder();

					// Append each new HTML line into one string. Add a tab
					// character.
					while ((sourceLine = source.readLine()) != null) {
						contentBuffer.append(sourceLine).append("\t");
					}

					String content = contentBuffer.toString();

					// Match all the http:// and https:// links
					Matcher matcher = httpPattern.matcher(content);
					while (matcher.find()) {
						String[] splitArray = matcher.group().split("href\\s*=");
						//System.out.println("Matcher.group=" + matcher.group() + " || Array=" +Arrays.toString(splitArray));
						String nextUrl = splitArray[1].trim();
						nextUrl = getValidURL(nextUrl.substring(1, nextUrl.length() - 1));
						
						if (!uniqueUrl.contains(nextUrl)) {
							uniqueUrl.add(nextUrl);
							searchUrl.add(nextUrl);
						}
					}

					crawled++;
					System.out.println("Crawled: " + startUrl);

					// Dumping the text to a file
					parseHtml(content, getValidFileName(startUrl), crawled);

				} catch (MalformedURLException mex) {
					//System.err.println("Url has errors. Could not crawl: " + startUrl);
					continue;
				} catch (IOException ex) {
					//System.err.println("Url has errors. Could not crawl: " + startUrl);
					continue;
				}
			}
			System.out.println("Total number of Web sites Crawled : " + crawled);
		}

		public void killIt() {
			System.out.println("Stopping search...");
			this.execute = false;
		}
	}

	private SearchThread searchThread;

	public WebCrawlerGui() {
		initComponents();
	}

	private String getValidURL(String url) {
		url = url.replaceAll("www.", "");
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		return url;
	}

	private String getValidFileName(String url) {
		String fileName = new String(url);
		fileName = fileName.replaceAll("http://", "");
		fileName = fileName.replaceAll("https://", "");
		fileName = fileName.replaceAll("\\.", "_");
		fileName = fileName.replaceAll("\\?", "_");
		fileName = fileName.replaceAll("\\=", "_");
		fileName = fileName.replaceAll("\\/", "_");
		fileName = fileName + ".txt";
		return fileName;
	}

	private void parseHtml(String content, String fileName, int docID) {
		// Remove style tags & inclusive content
		Pattern style = Pattern.compile("<style.*?>.*?</style>");
		Matcher mstyle = style.matcher(content);
		while (mstyle.find()) {
			content = mstyle.replaceAll(" ");
		}

		// Remove script tags & inclusive content
		Pattern script = Pattern.compile("<script.*?>.*?</script>");
		Matcher mscript = script.matcher(content);
		while (mscript.find()) {
			content = mscript.replaceAll(" ");
		}

		// Remove script tags & inclusive content
		Pattern span = Pattern.compile("<span.*?>.*?</span>");
		Matcher mspan = span.matcher(content);
		while (mspan.find()) {
			content = mspan.replaceAll(" ");
		}

		// Remove primary HTML tags
		Pattern tag = Pattern.compile("<.*?>");
		Matcher mtag = tag.matcher(content);
		while (mtag.find()) {
			content = mtag.replaceAll(" ");
		}

		// Remove comment tags & inclusive content
		Pattern comment = Pattern.compile("<!--.*?-->");
		Matcher mcomment = comment.matcher(content);
		while (mcomment.find()) {
			content = mcomment.replaceAll(" ");
		}

		// Remove special characters, such as &nbsp;
		Pattern sChar = Pattern.compile("&.*?;");
		Matcher msChar = sChar.matcher(content);
		while (msChar.find()) {
			content = msChar.replaceAll(" ");
		}
		
		// Remove special characters, such as |;
/*		Pattern pChar = Pattern.compile("|");
		Matcher mpChar = pChar.matcher(content);
		while (mpChar.find()) {
			content = mpChar.replaceAll(" ");
		}*/

		// Remove the tab characters.
		Pattern nLineChar = Pattern.compile("\\s+");
		Matcher mnLine = nLineChar.matcher(content);
		while (mnLine.find()) {
			content = mnLine.replaceAll(" ");
		}

		try {
			// Create file
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			//out.write("<docid>" + docID + "</docid>");
			//out.newLine();
			//out.write("<text>");
			//out.newLine();
			out.write(content.trim());
			//out.newLine();
			//out.write("</text>");
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("WebCrawler");

		lblStartUrl = new javax.swing.JLabel("Start Url");
		lblMaxPages = new javax.swing.JLabel("Max Pages");
		lblSearchResults = new javax.swing.JLabel("Search Results");
		lblStatus = new javax.swing.JLabel("");

		txtStartUrl = new javax.swing.JTextField("http://engineering.tamu.edu/cse/people/ajiang");
		txtMaxPages = new javax.swing.JTextField("20");

		txtAreaSearchResults = new javax.swing.JTextArea(5, 20);
		scrollPaneSearchResults = new javax.swing.JScrollPane(txtAreaSearchResults);

		btnStart = new javax.swing.JButton("Search");
		btnStart.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				System.out.println("Clicked Start");
				txtAreaSearchResults.setText("");
				
				if(txtStartUrl == null || txtStartUrl.getText().trim().equals("")){
				JOptionPane.showMessageDialog(getParent(),
					    "Starting Url is mandatory.",
					    "Input Error",
					    JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(txtMaxPages == null || txtMaxPages.getText().trim().equals("")){
					JOptionPane.showMessageDialog(getParent(),
						    "Maxium pages is mandatory.",
						    "Input Error",
						    JOptionPane.ERROR_MESSAGE);
						return;
					}
				
				searchThread = new SearchThread();
				searchThread.start();
			}
		});

		btnStop = new javax.swing.JButton("Stop");		
		btnStop.setEnabled(false);
		btnStop.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				System.out.println("Clicked Stop");
				if (searchThread != null) {
					searchThread.killIt();
					searchThread = null;
				}
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(23, 23, 23)
								.addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
																		javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap())
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(lblSearchResults, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
																		javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap())
												.addGroup(
														javax.swing.GroupLayout.Alignment.TRAILING,
														layout.createSequentialGroup()
																.addGroup(
																		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
																				.addComponent(scrollPaneSearchResults,
																						javax.swing.GroupLayout.Alignment.LEADING,
																						javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
																				.addGroup(
																						layout.createSequentialGroup()
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING,
																												false)
																												.addComponent(
																														lblMaxPages,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														Short.MAX_VALUE)
																												.addComponent(
																														lblStartUrl,
																														javax.swing.GroupLayout.PREFERRED_SIZE,
																														64, Short.MAX_VALUE))
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addGroup(
																														layout.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																																.addComponent(
																																		txtMaxPages,
																																		javax.swing.GroupLayout.PREFERRED_SIZE,
																																		75,
																																		javax.swing.GroupLayout.PREFERRED_SIZE)
																																.addComponent(
																																		txtStartUrl,
																																		javax.swing.GroupLayout.Alignment.TRAILING,
																																		javax.swing.GroupLayout.DEFAULT_SIZE,
																																		260,
																																		Short.MAX_VALUE))
																												.addGroup(
																														layout.createSequentialGroup()
																																.addGap(29, 29, 29)
																																.addComponent(
																																		btnStart)
																																.addGap(18, 18, 18)
																																.addComponent(btnStop)))))
																.addGap(49, 49, 49)))));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(34, 34, 34)
								.addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(txtStartUrl, javax.swing.GroupLayout.PREFERRED_SIZE, 27,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(lblStartUrl, javax.swing.GroupLayout.PREFERRED_SIZE, 21,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(txtMaxPages, javax.swing.GroupLayout.PREFERRED_SIZE, 28,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(lblMaxPages, javax.swing.GroupLayout.PREFERRED_SIZE, 21,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(21, 21, 21)
								.addComponent(lblSearchResults, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(scrollPaneSearchResults, javax.swing.GroupLayout.PREFERRED_SIZE, 107,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(lblStatus,
												javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
								.addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(btnStop)
												.addComponent(btnStart)).addGap(29, 29, 29)));

		pack();
	}

	public static void main(String args[]) {
		//Behind a firewall set your proxy and port here!

		/*Properties props = new Properties(System.getProperties());
		props.put("http.proxySet", "true");
		props.put("http.proxyHost", "144.16.192.213");
		props.put("http.proxyPort", "8080");

		Properties newprops = new Properties(props);
		System.setProperties(newprops);
		*/
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new WebCrawlerGui().setVisible(true);
			}
		});
	}
}
