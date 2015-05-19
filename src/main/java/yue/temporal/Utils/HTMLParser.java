package yue.temporal.Utils;

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.safety.Whitelist;

public class HTMLParser {

	
	public static String extractTextFromHTML_br2nl(File file) throws IOException {
	    
		String s = br2nl(file);
		String s_parse = Jsoup.parse(s.trim().toString()).text();	    
	    return s_parse;
	  }
	
	public static String extractTextFromHTML_br2nl(String htmlFileContent) throws IOException {
	    
		String s = br2nl(htmlFileContent);
		String s_parse = Jsoup.parse(s.trim().toString()).text();	    
	    return s_parse;
	  }
	
	public static String br2nl(String html) {
	    if(html==null)
	        return html;
	    Document document = Jsoup.parse(html, "UTF-8");
	    document.select("p").prepend("tags2nl_zy");
	    document.select("div").prepend("tags2nl_zy");
	    
	    String s = document.html();
	    return Jsoup.clean(s, "", Whitelist.relaxed(), new Document.OutputSettings().prettyPrint(false));
	}
	
	public static String br2nl(File htmlFile) throws IOException {

	    Document document = Jsoup.parse(htmlFile, "UTF-8");
	    document.select("p").prepend("tags2nl_zy");
	    document.select("div").prepend("tags2nl_zy");
	    
	    String s = document.html();
	    return Jsoup.clean(s, "", Whitelist.relaxed(), new Document.OutputSettings().prettyPrint(false));	    
	}	
	
	public static List<String> getPfromHTML_br2nl(File htmlFile, int minLength) throws IOException {
		
		List<String> paragraphs = new ArrayList<String>();
		String text = br2nl(htmlFile);
		Pattern p = Pattern.compile("\\s+");
		Matcher m = p.matcher(text);
		String text2 = m.replaceAll(" ");
		
		String[] textList = text2.trim().split("tags2nl_zy");
		for (String textLine: textList) {
			String textLine2 = Jsoup.parse(textLine.trim().toString()).text();
			if((textLine2.trim().length() >= minLength) && 
					(!textLine2.startsWith("WARC")) && 
					(!textLine2.startsWith("Content-Length:")) && 
					(!textLine2.startsWith("Content-Type:"))) {
				paragraphs.add(textLine2.trim());
			}
		}
		return paragraphs;
	}
	
	public static List<String> getPfromHTML_br2nl(String htmlFileContent, int minLength) throws IOException {
		
		List<String> paragraphs = new ArrayList<String>();
		String text = br2nl(htmlFileContent);
		Pattern p = Pattern.compile("\\s+");
		Matcher m = p.matcher(text);
		String text2 = m.replaceAll(" ");
		
		String[] textList = text2.trim().split("tags2nl_zy");
		for (String textLine: textList) {
			String textLine2 = Jsoup.parse(textLine.trim().toString()).text();
			if((textLine2.trim().length() >= minLength) && 
					(!textLine2.startsWith("WARC")) && 
					(!textLine2.startsWith("Content-Length:")) && 
					(!textLine2.startsWith("Content-Type:"))) {
				paragraphs.add(textLine2.trim());
			}
		}
		return paragraphs;
	}
	
	public static int findPosFromHTML_br2nl(String paragraphContent, File file_HTML) throws IOException {
		
		String textOnly = extractTextFromHTML_br2nl(file_HTML);
		
		int startPos = textOnly.indexOf(paragraphContent);
		
		return startPos;
	}
	
	public static int findPosFromHTML_br2nl(String paragraphContent, String htmlFileContent) throws IOException {
		
		String textOnly = extractTextFromHTML_br2nl(htmlFileContent);
		
		int startPos = textOnly.indexOf(paragraphContent);
		
		return startPos;
	}

    public static void main( String[] args ) throws IOException {
    	File file = new File("/Users/yuezhao/Desktop/clueweb12-0000wb-31-12737.html");
    	
    	
    	List<String> testSrings = getPfromHTML_br2nl(file, 1);
    	File resultFile = new File("/Users/yuezhao/Desktop/result2");
    	for (String testString: testSrings) {
    		System.out.println(testString);
    		FileProcess.addLinetoaFile(testString, resultFile.getAbsolutePath());
    	}
    	
    }

}
