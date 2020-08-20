package ie.gmit.sw;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ie.gmit.sw.ai.cloud.LogarithmicSpiralPlacer;
import ie.gmit.sw.ai.cloud.WeightedFont;
import ie.gmit.sw.ai.cloud.WordFrequency;
import ie.gmit.sw.parser.FileParser;
import ie.gmit.sw.parser.NodeWorker;
import ie.gmit.sw.parser.Parseable;

/*
 * -------------------------------------------------------------------------------------------------------------------
 * PLEASE READ THE FOLLOWING CAREFULLY. MOST OF THE "ISSUES" STUDENTS HAVE WITH DEPLOYMENT ARISE FROM NOT READING
 * AND FOLLOWING THE INSTRUCTIONS BELOW.
 * -------------------------------------------------------------------------------------------------------------------
 *
 * To compile this servlet, open a command prompt in the web application directory and execute the following commands:
 *
 * Linux/Mac													Windows
 * ---------													---------	
 * cd WEB-INF/classes/											cd WEB-INF\classes\
 * javac -cp .:$TOMCAT_HOME/lib/* ie/gmit/sw/*.java				javac -cp .:%TOMCAT_HOME%/lib/* ie/gmit/sw/*.java
 * cd ../../													cd ..\..\
 * jar -cf wcloud.war *											jar -cf wcloud.war *
 * 
 * Drag and drop the file ngrams.war into the webapps directory of Tomcat to deploy the application. It will then be 
 * accessible from http://localhost:8080. The ignore words file at res/ignorewords.txt will be located using the
 * IGNORE_WORDS_FILE_LOCATION mapping in web.xml. This works perfectly, so don't change it unless you know what
 * you are doing...
 * 
*/

public class ServiceHandler extends HttpServlet {
	
	
	private static final long serialVersionUID = 1L;
	private File ignoreWords, fcl;
	private int numSelect;
	private int threadPoolSize;
	private Parseable parser;
	private ExecutorService es;

    // Called once 
	public void init() throws ServletException {

		// Get a handle on the application context
		ServletContext ctx = getServletContext();

		// Reads the value from the <context-param> in web.xml
		threadPoolSize = Integer.parseInt(ctx.getInitParameter("THREAD_POOL_SIZE"));
		
		ignoreWords = new File(getServletContext().getRealPath(File.separator),ctx.getInitParameter("IGNORE_WORDS_FILE_LOCATION"));
		
		fcl = new File(getServletContext().getRealPath(File.separator), ctx.getInitParameter("FUZZY_CONTROL_LANG"));
		
		es = Executors.newFixedThreadPool(threadPoolSize);
		
		
		
		try {
			parser = new FileParser(ignoreWords);
			parser.parse();
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html"); // Output the MIME type
		PrintWriter out = resp.getWriter(); // Write out text. We can write out binary too and change the MIME type...

		// Initialise some request variables with the submitted form info. These are
		// local to this method and thread safe...
		String option = req.getParameter("cmbOptions"); // Change options to whatever you think adds value to your
														// assignment...
		numSelect = Integer.parseInt(option); // get number from options
		
		String searchTerm = req.getParameter("query");


		out.print("<html><head><title>Artificial Intelligence Assignment</title>");
		out.print("<link rel=\"stylesheet\" href=\"includes/style.css\">");

		out.print("</head>");
		out.print("<body>");
		out.print(
				"<div style=\"font-size:48pt; font-family:arial; color:#990000; font-weight:bold\">Web Opinion Visualiser</div>");

		out.print("<p><h2>Please read the following carefully</h2>");
		out.print(
				"<p>The &quot;ignore words&quot; file is located at <font color=red><b>" + ignoreWords.getAbsolutePath()
						+ "</b></font> and " + "is <b><u>" + ignoreWords.length() + "</u></b> bytes in size.");
		out.print(
				"You must place any additional files in the <b>res</b> directory and access them in the same way as the set of "
						+ "ignore words.");
		out.print(
				"<p>Place any additional JAR archives in the WEB-INF/lib directory. This will result in Tomcat adding the library "
						+ "of classes ");
		out.print(
				"to the CLASSPATH for the web application context. Please note that the JAR archives <b>jFuzzyLogic.jar</b>, "
						+ "<b>encog-core-3.4.jar</b> and ");
		out.print("<b>jsoup-1.12.1.jar</b> have already been added to the project.");

		out.print("<p><fieldset><legend><h3>Result</h3></legend>");

		WordFrequency[] words = new WeightedFont().getFontSizes(getWordFrequencyKeyValue(searchTerm));// use this method
																							// for
		// heuristic search
		Arrays.sort(words, Comparator.comparing(WordFrequency::getFrequency, Comparator.reverseOrder()));
		

		// Spira Mirabilis
		LogarithmicSpiralPlacer placer = new LogarithmicSpiralPlacer(800, 600);
		
		for (WordFrequency word : words) {
			placer.place(word); // Place each word on the canvas starting with the largest
		}

		BufferedImage cloud = placer.getImage(); // Get a handle on the word cloud graphic
		out.print("<img src=\"data:image/png;base64," + encodeToString(cloud) + "\" alt=\"Word Cloud\">");

		out.print("</fieldset>");
		out.print("<P>Maybe output some search stats here, e.g. max search depth, effective branching factor.....<p>");
		out.print("<a href=\"./\">Return to Start Page</a>");
		out.print("</body>");
		out.print("</html>");
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	private WordFrequency[] getWordFrequencyKeyValue(String searchTerm ) {

		WordFrequency[] words = null;	

		try {
			// if a user left input box empty fill up with default values
			if(searchTerm.isEmpty()) {
								
				words = new WordFrequency[3];
				words[0] = new WordFrequency("Empty",1000);
				words[1] = new WordFrequency("search term...",600);
				words[2] = new WordFrequency("Please try again...",200);
						
				return words;
			}
		    // Submit work for a thread
			Future<WordFrequency[]> wf = es.submit(new NodeWorker(searchTerm, parser, numSelect, fcl));

			System.out.println("Future done ? " + wf.isDone());
			// blocking operation until result gets back
			words = wf.get();

			System.out.println("Future done ? " + wf.isDone());


		} catch (Exception e) {
			
			e.printStackTrace();
		}

		return words;

	}

	private String encodeToString(BufferedImage image) {
		String s = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, "png", bos);
			byte[] bytes = bos.toByteArray();

			Base64.Encoder encoder = Base64.getEncoder();
			s = encoder.encodeToString(bytes);
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}


}