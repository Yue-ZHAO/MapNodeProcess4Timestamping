package yue.temporal.MapProcess;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import yue.temporal.MapProcess.Page;
import yue.temporal.MapProcess.Paragraph;
import yue.temporal.Utils.CluewebFileProcess;
import yue.temporal.Utils.FileProcess;
import yue.temporal.Utils.HTMLParser;

public class CluewebPage extends Page {
		
	//	the name of the original target file
	public String filename_CluewebPage;
	
	//	implement the Class Paragraph
	public List<Paragraph> paragraphs = new ArrayList<Paragraph>();
	
	public int threshold_length = 50;
	
	public double threshold_similarity = 0.7;
	
	public CluewebPage(String cluewebFileContent, int lenThreshold, double simThreshold) throws NoSuchAlgorithmException, IOException {
		
		String urlString = CluewebFileProcess.readURLFromCluewebFileString(cluewebFileContent);
		String timestamp = CluewebFileProcess.readTimeFromCluewebFileString(cluewebFileContent);
		String trecID = CluewebFileProcess.readTrecIDFromCluewebFileString(cluewebFileContent);
		
		this.URL = urlString;
		this.currentTimestamp = timestamp;
		this.MD5Code = FileProcess.stringTrans_MD5(urlString);
		this.filename_CluewebPage = trecID;
		
		this.threshold_length = lenThreshold;
		this.threshold_similarity = simThreshold;
		
		List<String> ps_FromHTML = HTMLParser.getPfromHTML_br2nl(cluewebFileContent, lenThreshold);
		
		//	2. init List<Paragraphs>
		//		for each paragraph, find the position of it in the original file
		for (String p_FromHTML: ps_FromHTML) {
			
			Paragraph paragraph = new Paragraph();
			int startPos = HTMLParser.findPosFromHTML_br2nl(p_FromHTML, cluewebFileContent);
			if (startPos != -1) {
				paragraph.setContent(p_FromHTML);
				paragraph.setStartPoint(startPos);
				paragraph.setEndPoint(startPos + p_FromHTML.length() - 1);
				//	For a clueweb page, we do not know the creation time of its paragraphs 
				//	when we extract info from the clueweb page file. 
				paragraphs.add(paragraph);
			}
		}
		
		this.num_Paragraphs = paragraphs.size();		
	}
	
	
	
	public CluewebPage(File file_Page, int lenThreshold, double simThreshold) throws NoSuchAlgorithmException, IOException {
		
		String urlString = CluewebFileProcess.readURLFromCluewebFile(file_Page);
		String timestamp = CluewebFileProcess.readTimeFromCluewebFile(file_Page);
	
		this.URL = urlString;
		this.currentTimestamp = timestamp;
		this.MD5Code = FileProcess.stringTrans_MD5(urlString);
		this.filename_CluewebPage = file_Page.getName();
		
		this.threshold_length = lenThreshold;
		this.threshold_similarity = simThreshold;
		
		List<String> ps_FromHTML = HTMLParser.getPfromHTML_br2nl(file_Page, lenThreshold);
		
		//	2. init List<Paragraphs>
		//		for each paragraph, find the position of it in the original file
		for (String p_FromHTML: ps_FromHTML) {
			
			Paragraph paragraph = new Paragraph();
			int startPos = HTMLParser.findPosFromHTML_br2nl(p_FromHTML, file_Page);
			if (startPos != -1) {
				paragraph.setContent(p_FromHTML);
				paragraph.setStartPoint(startPos);
				paragraph.setEndPoint(startPos + p_FromHTML.length() - 1);
				//	For a clueweb page, we do not know the creation time of its paragraphs 
				//	when we extract info from the clueweb page file. 
				paragraphs.add(paragraph);
			}
		}
		
		this.num_Paragraphs = paragraphs.size();
	}
	
}